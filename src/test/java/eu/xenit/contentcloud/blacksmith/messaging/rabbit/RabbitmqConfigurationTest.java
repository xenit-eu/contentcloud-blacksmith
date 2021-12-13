package eu.xenit.contentcloud.blacksmith.messaging.rabbit;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

class RabbitmqConfigurationTest {

    @Nested
    @SpringBootTest
    class DisabledRabbitmqBridge {

        @Autowired
        private ApplicationContext applicationContext;

        @Test
        void rabbitmqDisabled_byDefault() {
            assertThat(this.applicationContext.getBeanProvider(ApplicationEventToRabbitmqBridge.class))
                    .isEmpty();
        }
    }

    @Nested
    @SpringBootTest(properties = "blacksmith.rabbitmq.enabled=true")
    class EnabledRabbitmqBridge {

        @Autowired
        private ApplicationContext applicationContext;

        @Test
        void rabbitmqEnabled_withProperties() {
            assertThat(this.applicationContext.getBeanProvider(ApplicationEventToRabbitmqBridge.class)).singleElement();

        }
    }
}