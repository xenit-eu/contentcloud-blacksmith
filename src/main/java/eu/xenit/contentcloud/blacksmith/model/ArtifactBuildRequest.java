package eu.xenit.contentcloud.blacksmith.model;

import eu.xenit.contentcloud.blacksmith.model.publication.PublicationDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ArtifactBuildRequest {

    private BuildRequestId id;

    private URI changeset;
    private String service;
    private String type;

    private PublicationDetails repository;

    private Map<String, Object> labels = new HashMap<>();

}
