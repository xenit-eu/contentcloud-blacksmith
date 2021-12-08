package eu.xenit.contentcloud.blacksmith.infra.scribe;

import eu.xenit.contentcloud.blacksmith.model.ArtifactBuildRequest;
import eu.xenit.contentcloud.blacksmith.spi.ScribeGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@RequiredArgsConstructor
public class DefaultScribeGateway implements ScribeGateway {

    private final ScribeProperties properties;
    private final RestTemplate restTemplate;

    public void getProjectSources(ArtifactBuildRequest buildRequest, Path workingDirectory) throws IOException {
        var uri = UriComponentsBuilder.fromUriString("http://localhost:8081/starter.zip")
                .queryParam("changeset", buildRequest.getChangeset()).build().toUri();

        Path zip = downloadProjectZip(workingDirectory, uri);
        extractZip(zip, workingDirectory);
    }

    private void extractZip(Path zip, Path workingDirectory) throws IOException {
        try {
            new ZipFile(zip.toFile())
                    .extractAll(workingDirectory.toFile().getAbsolutePath());
        } catch (ZipException e) {
            throw e;
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
}
