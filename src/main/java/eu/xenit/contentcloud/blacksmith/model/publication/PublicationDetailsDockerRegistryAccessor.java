package eu.xenit.contentcloud.blacksmith.model.publication;

import java.net.URI;
import java.util.Optional;

public class PublicationDetailsDockerRegistryAccessor implements DockerImageRegistry {

    private static final String TYPE = "docker";

    private final PublicationDetails publicationDetails;


    public PublicationDetailsDockerRegistryAccessor(PublicationDetails details) {
        this.publicationDetails = details;

        if (!TYPE.equalsIgnoreCase(details.getType())) {
            var msg = String.format("Publication type '%s' is not supported, expected '%s'", details.getType(), TYPE);
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public String getUsername() {
        return publicationDetails.getProperty("username");
    }

    @Override
    public String getPassword() {
        return publicationDetails.getProperty("password");
    }

    @Override
    public String getToken() {
        return publicationDetails.getProperty("token");
    }

    @Override
    public String getName() {
        return publicationDetails.getProperty("registry");
    }

    @Override
    public URI getUrl() {
        return Optional.ofNullable(publicationDetails.getProperty("url"))
                .map(URI::create)
                .orElse(DockerImageRegistry.super.getUrl());
    }
}
