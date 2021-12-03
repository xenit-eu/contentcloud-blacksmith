package eu.xenit.contentcloud.blacksmith.driver.rest;

import eu.xenit.contentcloud.blacksmith.model.BuildRequestId;
import eu.xenit.contentcloud.blacksmith.service.ArtifactsService;
import eu.xenit.contentcloud.blacksmith.model.ArtifactBuildRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ArtifactsRestController {

    @NonNull
    private final ArtifactsService artifactsService;

    // POST /artifacts/
    @PostMapping("/artifacts")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<BuildRequestId> requestArtifact(@RequestBody ArtifactBuildRequest request) {
        var id = this.artifactsService.requestArtifact(request);
        return ResponseEntity.accepted().body(id);
    }

}
