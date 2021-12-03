package eu.xenit.contentcloud.blacksmith.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import eu.xenit.contentcloud.blacksmith.model.artifact.ArtifactDescriptor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Map;

@Value
@RequiredArgsConstructor
public class ArtifactBuildSuccess {

    @NonNull
    @JsonUnwrapped
    BuildRequestId id;

    boolean success = true;
    String artifact;
    String type;

    RequestDetails request;
    Map<String, Object> labels;

    public static ArtifactBuildSuccess from(ArtifactBuildRequest request, ArtifactDescriptor descriptor) {
        return new ArtifactBuildSuccess(
                request.getId(),
                descriptor.getCoordinates(),
                descriptor.getType(),
                RequestDetails.from(request),
                request.getLabels());
    }
}
