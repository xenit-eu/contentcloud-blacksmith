package eu.xenit.contentcloud.blacksmith.service;

import eu.xenit.contentcloud.blacksmith.infra.build.BuildFailedException;
import eu.xenit.contentcloud.blacksmith.infra.build.gradle.GradleBuildExecutor;
import eu.xenit.contentcloud.blacksmith.model.ArtifactBuildFailed;
import eu.xenit.contentcloud.blacksmith.model.ArtifactBuildRequest;
import eu.xenit.contentcloud.blacksmith.model.ArtifactBuildSuccess;
import eu.xenit.contentcloud.blacksmith.model.BuildRequestId;
import eu.xenit.contentcloud.blacksmith.model.artifact.ArtifactDescriptor;
import eu.xenit.contentcloud.blacksmith.model.publication.PublicationDetails;
import eu.xenit.contentcloud.blacksmith.spi.EventPublisher;
import eu.xenit.contentcloud.blacksmith.spi.ScribeGateway;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ArtifactBuilderServiceTest {

    @Test
    void handleBuildRequest_apiDockerImage_success() throws ExecutionException, InterruptedException {
        var descriptor = new ArtifactDescriptor("docker", "hub.docker.io/public/nginx");

        var events = mock(EventPublisher.class);
        var gradleRunner = mock(GradleBuildExecutor.class);
        given(gradleRunner.publishDockerImage(any(), any()))
                .willReturn(CompletableFuture.completedFuture(descriptor));

        var service = new ArtifactBuilderService(events, mock(ScribeGateway.class), gradleRunner);
        service.handleBuildRequest(apiDockerImageRequest()).get();

        verify(events).publishEvent(any(ArtifactBuildSuccess.class));
        verify(events, never()).publishEvent(any(ArtifactBuildFailed.class));
    }

    @Test
    void handleBuildRequest_apiDockerImage_gradleFailed()  {
        var events = mock(EventPublisher.class);
        var gradleRunner = mock(GradleBuildExecutor.class);
        given(gradleRunner.publishDockerImage(any(), any()))
                .willReturn(CompletableFuture.failedFuture(new BuildFailedException("Oh noes!")));

        try {
            var service = new ArtifactBuilderService(events, mock(ScribeGateway.class), gradleRunner);
            service.handleBuildRequest(apiDockerImageRequest()).get();
        } catch (InterruptedException | ExecutionException e) {
            // expected
        }

        verify(events).publishEvent(any(ArtifactBuildFailed.class));
        verify(events, never()).publishEvent(any(ArtifactBuildSuccess.class));
    }

    @Test
    void handleBuildRequest_invalidService()  {
        var events = mock(EventPublisher.class);

        try {
            var service = new ArtifactBuilderService(events, mock(ScribeGateway.class), mock(GradleBuildExecutor.class));
            service.handleBuildRequest(buildRequest("invalid-service", "docker-image")).get();
        } catch (InterruptedException | ExecutionException e) {
            // expected
        }

        verify(events).publishEvent(any(ArtifactBuildFailed.class));
        verify(events, never()).publishEvent(any(ArtifactBuildSuccess.class));
    }

    @Test
    void handleBuildRequest_invalidType()  {
        var events = mock(EventPublisher.class);

        try {
            var service = new ArtifactBuilderService(events, mock(ScribeGateway.class), mock(GradleBuildExecutor.class));
            service.handleBuildRequest(buildRequest("api", "zip")).get();
        } catch (InterruptedException | ExecutionException e) {
            // expected
        }

        verify(events).publishEvent(any(ArtifactBuildFailed.class));
        verify(events, never()).publishEvent(any(ArtifactBuildSuccess.class));
    }

    private static ArtifactBuildRequest apiDockerImageRequest() {
        return buildRequest("api", "docker-image");
    }

    private static ArtifactBuildRequest buildRequest(String service, String type) {
        return new ArtifactBuildRequest(
                BuildRequestId.randomId(),
                URI.create("http://localhost:8080/orgs/holmes/projects/dcm/changesets/a470d440-3519-4802-8576-650237c9151f"),
                service,
                type,
                publicationDetails(type),
                Map.of("foo", "bar"));
    }

    private static PublicationDetails publicationDetails(String type) {
        if ("docker-image".equals(type)) {
            return new PublicationDetails("docker",
                    Map.of("registry", "hub.xenit.eu",
                            "username", "my-user",
                            "password", "my-password"));
        } else {
            return new PublicationDetails(type, Map.of());
        }
    }
}