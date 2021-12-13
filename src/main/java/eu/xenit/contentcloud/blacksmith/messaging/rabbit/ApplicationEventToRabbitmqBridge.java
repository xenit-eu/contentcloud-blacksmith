package eu.xenit.contentcloud.blacksmith.messaging.rabbit;

import eu.xenit.contentcloud.blacksmith.model.ArtifactBuildFailed;
import eu.xenit.contentcloud.blacksmith.model.ArtifactBuildSuccess;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Exchange;
import org.springframework.context.event.EventListener;

@Slf4j
@RequiredArgsConstructor
public class ApplicationEventToRabbitmqBridge {

    private final AmqpTemplate amqpTemplate;

    private final Exchange exchange;

    @EventListener
    public void forwardBuildSuccess(ArtifactBuildSuccess buildSuccess) {
        log.info("success: {}", buildSuccess);

        try {
            this.amqpTemplate.convertAndSend(exchange.getName(), "build.success", buildSuccess);
        } catch (RuntimeException ex) {
            log.warn("sending msg failed", ex);
            throw ex;
        }
    }

    @EventListener
    public void forwardBuildFailed(ArtifactBuildFailed buildFailed) {
        log.info("failed: {}", buildFailed);

        try {
            this.amqpTemplate.convertAndSend(exchange.getName(), "build.failed", buildFailed);
        } catch (RuntimeException ex) {
            log.warn("sending msg failed", ex);
            throw ex;
        }
    }
}
