package eu.xenit.contentcloud.blacksmith.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.UUID;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BuildRequestId {


    @NonNull
    @JsonProperty("id")
    UUID value;

    public static BuildRequestId of(String id) {
        return new BuildRequestId(UUID.fromString(id));
    }

    public static BuildRequestId randomId() {
        return new BuildRequestId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return this.value.toString();
    }
}
