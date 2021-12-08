package eu.xenit.contentcloud.blacksmith;

import eu.xenit.contentcloud.blacksmith.infra.build.gradle.GradleBuildExecutor;
import eu.xenit.contentcloud.blacksmith.infra.scribe.DefaultScribeGateway;
import eu.xenit.contentcloud.blacksmith.infra.scribe.ScribeProperties;
import eu.xenit.contentcloud.blacksmith.service.ArtifactBuilderService;
import eu.xenit.contentcloud.blacksmith.spi.EventPublisher;
import eu.xenit.contentcloud.blacksmith.spi.ScribeGateway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EnableConfigurationProperties(ScribeProperties.class)
public class BlacksmithApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlacksmithApplication.class, args);
    }

    @Bean
    EventPublisher eventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return applicationEventPublisher::publishEvent;
    }

    @Bean
    ScribeGateway scribeGateway(ScribeProperties properties, RestTemplateBuilder restTemplateBuilder) {
        return new DefaultScribeGateway(properties, restTemplateBuilder.build());
    }

    @Bean
    ArtifactBuilderService artifactBuilderService(EventPublisher eventPublisher, ScribeGateway scribeGateway) {
        return new ArtifactBuilderService(eventPublisher, scribeGateway, new GradleBuildExecutor());
    }
}
