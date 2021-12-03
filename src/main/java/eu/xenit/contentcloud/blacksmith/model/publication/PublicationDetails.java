package eu.xenit.contentcloud.blacksmith.model.publication;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class PublicationDetails {

    private String type;

    @JsonAnySetter
    private Map<String, Object> properties = new HashMap<>();

    public String getProperty(String name) {
        return (String) this.properties.get(name);
    }
}
