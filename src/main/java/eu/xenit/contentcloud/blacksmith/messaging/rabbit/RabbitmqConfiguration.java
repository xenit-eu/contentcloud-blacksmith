package eu.xenit.contentcloud.blacksmith.messaging.rabbit;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "blacksmith.rabbitmq.enabled")
public class RabbitmqConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "blacksmith.rabbitmq")
    ScribeRabbitmqProperties scribeRabbitmqProperties() {
        return new ScribeRabbitmqProperties();
    }

    @Bean
    TopicExchange exchange(ScribeRabbitmqProperties properties) {
        return new TopicExchange(properties.getExchange());
    }

    @Bean
    org.springframework.amqp.support.converter.MessageConverter jacksonMessageConverter() {
        var converter = new Jackson2JsonMessageConverter();
        converter.setAlwaysConvertToInferredType(true); // deserialization used in tests only
        return converter;
    }

    @Bean
    ApplicationEventToRabbitmqBridge rabbitmqBridge(AmqpTemplate template, Exchange exchange) {
        return new ApplicationEventToRabbitmqBridge(template, exchange);
    }
}
