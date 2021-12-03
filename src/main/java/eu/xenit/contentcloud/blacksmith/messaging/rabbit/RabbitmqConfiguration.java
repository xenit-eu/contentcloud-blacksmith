package eu.xenit.contentcloud.blacksmith.messaging.rabbit;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfiguration {
    static final String topicExchangeName = "contentcloud";

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(topicExchangeName);
    }

    @Bean
    org.springframework.amqp.support.converter.MessageConverter jacksonMessageConverter() {
        var converter = new Jackson2JsonMessageConverter();
        converter.setAlwaysConvertToInferredType(true); // deserialization used in tests only
        return converter;
    }

    @Bean
    ApplicationEventToRabbitBridge rabbitmqBridge(AmqpTemplate template, Exchange exchange) {
        return new ApplicationEventToRabbitBridge(template, exchange);
    }
}
