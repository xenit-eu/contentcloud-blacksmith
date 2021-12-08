package eu.xenit.contentcloud.blacksmith.model.publication;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class PublicationDetails {

    private String type;

    @JsonAnySetter
    private Map<String, Object> properties = new HashMap<>();

    public String getProperty(String name) {
        return (String) this.properties.get(name);
    }
}
