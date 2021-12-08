package eu.xenit.contentcloud.blacksmith.spi;

/**
 * Interface that provides in-process event publication functionality.
 */

@FunctionalInterface
public interface EventPublisher {

    /**
     * Publish an application event.
     *
     * @param event the event to publish
     */
    void publishEvent(Object event);

}
