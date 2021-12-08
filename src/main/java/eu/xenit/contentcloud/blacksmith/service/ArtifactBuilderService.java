package eu.xenit.contentcloud.blacksmith.service;

import eu.xenit.contentcloud.blacksmith.infra.build.gradle.GradleBuildExecutor;
import eu.xenit.contentcloud.blacksmith.model.ArtifactBuildFailed;
import eu.xenit.contentcloud.blacksmith.model.ArtifactBuildRequest;
import eu.xenit.contentcloud.blacksmith.model.ArtifactBuildSuccess;
import eu.xenit.contentcloud.blacksmith.model.artifact.ArtifactDescriptor;
import eu.xenit.contentcloud.blacksmith.model.publication.PublicationDetailsDockerRegistryAccessor;
import eu.xenit.contentcloud.blacksmith.spi.EventPublisher;
import eu.xenit.contentcloud.blacksmith.spi.ScribeGateway;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;


@Slf4j
@RequiredArgsConstructor
public class ArtifactBuilderService {

    @NonNull
    private final EventPublisher eventPublisher;

    @NonNull
    private final ScribeGateway scribeGateway;

    @NonNull
    private final GradleBuildExecutor gradleBuildExecutor;

    @Async
    @EventListener
    public CompletableFuture<ArtifactDescriptor> handleBuildRequest(ArtifactBuildRequest buildRequest) {
        log.info("Handling artifact request: {}", buildRequest);

        try {
            return this.withTempDirectory(workingDirectory -> {
                        if ("api".equals(buildRequest.getService())) {
                            return this.buildApiArtifact(buildRequest, workingDirectory);
                        } else {
                            throw new UnsupportedOperationException("service '" + buildRequest.getService() + "' not supported");
                        }
                    })
                    .whenComplete((descriptor, throwable) -> publishBuildCompletedEvents(buildRequest, descriptor, throwable));
        } catch (Exception ex) {
            log.warn("Build {} failed: {}", buildRequest, ex);
            this.eventPublisher.publishEvent(ArtifactBuildFailed.from(buildRequest, ex));
            return CompletableFuture.failedFuture(ex);
        }
    }

    private void publishBuildCompletedEvents(ArtifactBuildRequest buildRequest, ArtifactDescriptor artifactDescriptor, Throwable throwable) {
        try {
            if (throwable != null) {
                log.warn("build failed:", throwable);
                var event = ArtifactBuildFailed.from(buildRequest, throwable);
                this.eventPublisher.publishEvent(event);
            } else {
                log.info("artifact ready: {}", artifactDescriptor);
                var event = ArtifactBuildSuccess.from(buildRequest, artifactDescriptor);
                this.eventPublisher.publishEvent(event);
            }
        } catch (RuntimeException re) {
            log.warn("Publishing event failed:", re);
        }
    }

    private CompletableFuture<ArtifactDescriptor> buildApiArtifact(ArtifactBuildRequest buildRequest, Path workingDirectory) {

        if ("docker-image".equals(buildRequest.getType())) {
            return this.buildApiDockerImage(buildRequest, workingDirectory);
        }

        var ex = new UnsupportedOperationException("service '" + buildRequest.getService() + "' not supported");
        ex.fillInStackTrace();
        return CompletableFuture.failedFuture(ex);
    }

    CompletableFuture<ArtifactDescriptor> buildApiDockerImage(ArtifactBuildRequest buildRequest, Path workingDirectory) {
        // 1. create a new local working directory
        // 2. get the generated code from scribe and unzip
        // 3. build the artifact + (publish the artifact)
        // 4. send notification
        try {
            this.scribeGateway.getProjectSources(buildRequest, workingDirectory);
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }

        // should we 'detect' which build method to use ?
        var registry = new PublicationDetailsDockerRegistryAccessor(buildRequest.getRepository());
        return gradleBuildExecutor.publishDockerImage(workingDirectory, registry);
    }


    <R> CompletableFuture<R> withTempDirectory(Function<Path, CompletableFuture<R>> callback) {

        Path workingDirectory;

        try {
            workingDirectory = Files.createTempDirectory("smith-");
            var future = callback.apply(workingDirectory);
            future.whenComplete((success, fail) -> {
                log.info("Cleanup temp directory now ? {}", workingDirectory);
//            try {
//                Files.delete(workingDirectory);
//            } catch (IOException ignored) {
//            }
            });
            return future;
        } catch (IOException | RuntimeException exception) {
            return CompletableFuture.failedFuture(exception);
        }
    }
}

