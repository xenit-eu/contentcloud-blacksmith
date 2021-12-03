package eu.xenit.contentcloud.blacksmith.service;

import eu.xenit.contentcloud.blacksmith.model.BuildRequestId;
import eu.xenit.contentcloud.blacksmith.model.ArtifactBuildRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArtifactsService {

    @NonNull
    private final ApplicationEventPublisher applicationEventPublisher;

    public BuildRequestId requestArtifact(ArtifactBuildRequest request) {

        // assign an id (should not be under client control)
        var buildRequestId = BuildRequestId.randomId();
        request.setId(buildRequestId);

        this.applicationEventPublisher.publishEvent(request);

        return buildRequestId;
    }
}
