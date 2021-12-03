package eu.xenit.contentcloud.blacksmith.model;

import lombok.Value;

import java.net.URI;

@Value
public class RequestDetails {
    URI changeset;
    String service;
    String type;

    public static RequestDetails from(ArtifactBuildRequest buildRequest) {
        return new RequestDetails(buildRequest.getChangeset(), buildRequest.getService(), buildRequest.getType());
    }
}
