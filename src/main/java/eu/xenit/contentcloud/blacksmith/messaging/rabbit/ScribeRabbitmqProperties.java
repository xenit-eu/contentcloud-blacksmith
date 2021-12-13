package eu.xenit.contentcloud.blacksmith.messaging.rabbit;

import lombok.Data;

@Data
public class ScribeRabbitmqProperties {

    /**
     * Whether to enable rabbitmq, to send out notifications. Defaults to {@code false}.
     */
    private boolean enabled = false;

    /**
     * Name of the exchange to use for send operations. Defaults to {@code contentcloud}.
     */
    private String exchange = "contentcloud";

}
