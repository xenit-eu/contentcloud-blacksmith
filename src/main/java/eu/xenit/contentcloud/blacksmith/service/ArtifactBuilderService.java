package eu.xenit.contentcloud.blacksmith.service;

import eu.xenit.contentcloud.blacksmith.infra.build.gradle.GradleBuildExecutor;
import eu.xenit.contentcloud.blacksmith.model.ArtifactBuildRequest;
import eu.xenit.contentcloud.blacksmith.model.ArtifactBuildFailed;
import eu.xenit.contentcloud.blacksmith.model.ArtifactBuildSuccess;
import eu.xenit.contentcloud.blacksmith.model.publication.PublicationDetailsDockerRegistryAccessor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

@Slf4j
@Service
public class ArtifactBuilderService {

    private final RestTemplate restTemplate;

    @NonNull
    private final ApplicationEventPublisher applicationEventPublisher;

    private final GradleBuildExecutor gradleBuildExecutor = new GradleBuildExecutor();

    ArtifactBuilderService(RestTemplateBuilder restTemplateBuilder, ApplicationEventPublisher applicationEventPublisher) {
        this.restTemplate = restTemplateBuilder.build();
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Async
    @EventListener
    public void handleBuildRequest(ArtifactBuildRequest buildRequest) throws IOException {
        log.info("Handling artifact request: {}", buildRequest);

        if ("api".equals(buildRequest.getService())) {
            this.buildApiArtifact(buildRequest);
        } else {
            throw new UnsupportedOperationException("service '" + buildRequest.getService() + "' not supported");
        }
    }

    private void buildApiArtifact(ArtifactBuildRequest buildRequest) throws IOException {

        if ("docker-image".equals(buildRequest.getType())) {
            this.buildApiDockerArtifact(buildRequest);
        }else {
            throw new UnsupportedOperationException("service '" + buildRequest.getService() + "' not supported");
        }
    }

    void buildApiDockerArtifact(ArtifactBuildRequest buildRequest) throws IOException {
        // 1. create a new local working directory
        // 2. get the generated code from scribe and unzip
        // 3. build the artifact + (publish the artifact)
        // 4. send notification
        this.withTempDirectory(workingDirectory -> {
            var uri = UriComponentsBuilder.fromUriString("http://localhost:8081/starter.zip")
                    .queryParam("changeset", buildRequest.getChangeset()).build().toUri();

            Path zip = downloadProjectZip(workingDirectory, uri);
            extractZip(zip, workingDirectory);

            // should we 'detect' which build method to use ?

            var registry = new PublicationDetailsDockerRegistryAccessor(buildRequest.getRepository());
            gradleBuildExecutor.publishDockerImage(workingDirectory, registry)
                    .whenComplete((artifactDescriptor, throwable) -> {
                        try {
                            if (throwable != null) {
                                log.warn("build failed:", throwable);
                                var event = ArtifactBuildFailed.from(buildRequest, throwable);
                                this.applicationEventPublisher.publishEvent(event);
                            } else {
                                log.info("artifact ready: {}", artifactDescriptor);
                                var event = ArtifactBuildSuccess.from(buildRequest, artifactDescriptor);
                                this.applicationEventPublisher.publishEvent(event);
                            }
                        } catch (RuntimeException re) {
                            log.warn("Publishing event failed:", re);
                        }
                    });


        });
    }

    private void extractZip(Path zip, Path workingDirectory) {
        try {
            new ZipFile(zip.toFile())
                    .extractAll(workingDirectory.toFile().getAbsolutePath());
        } catch (ZipException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path downloadProjectZip(Path workingDirectory, URI uri) {
        log.info("Fetching project scribe: {}", uri);
        return restTemplate.execute(uri, HttpMethod.GET, null, response -> {
            // check status code + mimetype + filename ?
            Path zipPath = workingDirectory.resolve("api.zip");
            Files.copy(response.getBody(), zipPath);
            log.info("Downloaded {}", zipPath);
            return zipPath;
        });
    }

    void withTempDirectory(Consumer<Path> callback) throws IOException {

        Path workingDirectory = Files.createTempDirectory("smith-");

        try {
            callback.accept(workingDirectory);
        } finally {
//            try {
//                Files.delete(workingDirectory);
//            } catch (IOException ignored) {
//            }
        }
    }

    static <T> T unchecked(Callable<T> callable) {
        try {
            return callable.call();
        } catch (IOException ioe) {
            throw new UncheckedIOException(ioe);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}

