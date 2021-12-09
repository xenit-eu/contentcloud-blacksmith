package eu.xenit.contentcloud.blacksmith.infra.scribe;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "blacksmith.scribe")
public class ScribeProperties {

    /**
     * Scribe endpoint, used for code generation.
     */
    public String url = "https://api.content-cloud.eu/codegen/";
}
