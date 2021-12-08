package eu.xenit.contentcloud.blacksmith.spi;

import eu.xenit.contentcloud.blacksmith.model.ArtifactBuildRequest;

import java.io.IOException;
import java.nio.file.Path;

public interface ScribeGateway {

    void getProjectSources(ArtifactBuildRequest buildRequest, Path workingDirectory) throws IOException;

}
