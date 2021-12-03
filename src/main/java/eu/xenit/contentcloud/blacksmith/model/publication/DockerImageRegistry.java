package eu.xenit.contentcloud.blacksmith.model.publication;

import java.net.URI;

public interface DockerImageRegistry {
    String getName();

    String getUsername();
    String getPassword();
    String getToken();

    default URI getUrl() {
        return URI.create("https://" + this.getName() + "/v2/");
    }
}
