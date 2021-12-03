package eu.xenit.contentcloud.blacksmith.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import eu.xenit.contentcloud.blacksmith.util.exceptions.ExceptionUtils;
import lombok.NonNull;
import lombok.Value;

import java.util.Map;

@Value
public class ArtifactBuildFailed {

    @NonNull
    @JsonUnwrapped
    BuildRequestId id;

    RequestDetails request;

    @NonNull
    String reason;
    String[] causes;

    boolean success = false;

    Map<String, Object> labels;

    public static ArtifactBuildFailed from(ArtifactBuildRequest request, Throwable throwable) {
        return new ArtifactBuildFailed(
                request.getId(),
                RequestDetails.from(request),
                throwable.getMessage(),
                ExceptionUtils.collectCauses(throwable),
                request.getLabels());
    }


}
