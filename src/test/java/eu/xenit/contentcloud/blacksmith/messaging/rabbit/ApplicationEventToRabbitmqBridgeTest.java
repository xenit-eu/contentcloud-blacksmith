package eu.xenit.contentcloud.blacksmith.messaging.rabbit;

import com.fasterxml.jackson.databind.JsonNode;
import com.rabbitmq.client.Channel;
import eu.xenit.contentcloud.blacksmith.model.ArtifactBuildFailed;
import eu.xenit.contentcloud.blacksmith.model.ArtifactBuildSuccess;
import eu.xenit.contentcloud.blacksmith.model.BuildRequestId;
import eu.xenit.contentcloud.blacksmith.model.RequestDetails;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.test.TestRabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.mock;

@SpringBootTest
class ApplicationEventToRabbitBridgeTest {

    @Configuration
    @Import({RabbitmqConfiguration.class, RabbitAutoConfiguration.class})
    public static class Config {

        final List<Message<JsonNode>> success = new ArrayList<>();
        final List<Message<JsonNode>> failed = new ArrayList<>();

        @RabbitListener(queues = "build.success")
        public void listenBuildSuccess(Message<JsonNode> msg) {
            success.add(msg);
        }

        @RabbitListener(queues = "build.failed")
        public void listenBuildFailed(Message<JsonNode> msg) {
            failed.add(msg);
        }

        @Bean
        public TestRabbitTemplate template(RabbitTemplateConfigurer configurer, ConnectionFactory connectionFactory) {
            var template = new TestRabbitTemplate(connectionFactory);
            configurer.configure(template, connectionFactory);
            return template;
        }

        @Bean
        public ConnectionFactory connectionFactory() {
            ConnectionFactory factory = mock(ConnectionFactory.class);
            Connection connection = mock(Connection.class);
            Channel channel = mock(Channel.class);
            willReturn(connection).given(factory).createConnection();
            willReturn(channel).given(connection).createChannel(anyBoolean());
            given(channel.isOpen()).willReturn(true);
            return factory;
        }

    }

    @Autowired
    private ApplicationEventToRabbitBridge bridge;

    @Autowired
    private Config config;

    @Test
    void forward_buildSuccess() {
        assertThat(bridge).isNotNull();
        var buildRequestId = BuildRequestId.randomId();
        var successEvent = new ArtifactBuildSuccess(
                buildRequestId,
                "rg.fr-par.scw.cloud/content-cloud-apps/holmes.dcm-api",
                "docker-image",
                new RequestDetails(
                        URI.create("https://api.content-cloud.eu/orgs/holmes/projects/dcm/changesets/a470d440-3519-4802-8576-650237c9151f"),
                        "api",
                        "docker-image"),
                Map.of("deployment", "c754baf7-0db9-44ac-ad1d-da4633e44a68",
                        "artifact", "f5cae000-29a5-497b-b78c-806c9157918e")

        );

        bridge.forwardBuildSuccess(successEvent);

        assertThat(config.success).singleElement()
                .satisfies(msg -> assertThat(msg.getHeaders().get("contentType")).isEqualTo("application/json"))
                .extracting(Message::getPayload)
                .satisfies(json -> {
                    assertThat(json.get("id").asText()).isEqualTo(buildRequestId.toString());
                    assertThat(json.get("artifact").asText())
                            .isEqualTo("rg.fr-par.scw.cloud/content-cloud-apps/holmes.dcm-api");
                    assertThat(json.get("success").asBoolean()).isEqualTo(true);
                    assertThat(json.get("type").asText()).isEqualTo("docker-image");
                });
    }

    @Test
    void forward_buildFailed() {
        assertThat(bridge).isNotNull();
        var buildRequestId = BuildRequestId.randomId();
        var buildFailed = new ArtifactBuildFailed(
                buildRequestId,
                new RequestDetails(
                        URI.create("https://api.content-cloud.eu/orgs/holmes/projects/dcm/changesets/a470d440-3519-4802-8576-650237c9151f"),
                        "api",
                        "docker-image"),
                "I stumbled",
                new String[]{ "because I'm clumsy"},
                Map.of("deployment", "c754baf7-0db9-44ac-ad1d-da4633e44a68",
                        "artifact", "f5cae000-29a5-497b-b78c-806c9157918e")

        );

        bridge.forwardBuildFailed(buildFailed);

        assertThat(config.failed).singleElement()
                .satisfies(msg -> assertThat(msg.getHeaders().get("contentType")).isEqualTo("application/json"))
                .extracting(Message::getPayload)
                .satisfies(json -> {
                    assertThat(json.get("id").asText()).isEqualTo(buildRequestId.toString());
                    assertThat(json.get("success").asBoolean()).isEqualTo(false);
                    assertThat(json.get("reason").asText()).isEqualTo("I stumbled");
                });
    }

}