package com.microservice.kafka;

import com.microservice.client.UserDetailsClient;
import com.microservice.event.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer service for consuming user events
 * This is an example consumer that can be extended for specific business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaUserEventConsumer {

    private final UserDetailsClient userDetailsClient;

    /**
     * Listen to user events from Kafka topic
     */
    @KafkaListener(
            topics = "${spring.kafka.topics.user-events:user-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
            @Payload UserEvent userEvent,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.info("Consumed user event - Event Type: {}, User ID: {}, Partition: {}, Offset: {}",
                    userEvent.getEventType(), userEvent.getUserId(), partition, offset);

            // Handle different event types
            switch (userEvent.getEventType()) {
                case "CREATED":
                    handleUserCreated(userEvent);
                    break;
                case "UPDATED":
                    handleUserUpdated(userEvent);
                    break;
                case "DELETED":
                    handleUserDeleted(userEvent);
                    break;
                case "DEACTIVATED":
                    handleUserDeactivated(userEvent);
                    break;
                default:
                    log.warn("Unknown event type: {}", userEvent.getEventType());
            }

            // Acknowledge message after successful processing
            acknowledgment.acknowledge();
            log.debug("Message acknowledged for event: {}", userEvent.getEventId());

        } catch (Exception e) {
            log.error("Error processing user event: {}", userEvent, e);
            // In case of error, the message will be retried (based on Kafka consumer configuration)
            // For a DLQ (Dead Letter Queue), consider using a separate error handler
        }
    }

    /**
     * Handle user created event
     */
    private void handleUserCreated(UserEvent event) {
        log.info("Processing user creation event for user: {} ({})", event.getUserId(), event.getEmail());
        // Trigger the user-details microservice to store the new user's details
        userDetailsClient.sendUserCreated(event);
    }

    /**
     * Handle user updated event
     */
    private void handleUserUpdated(UserEvent event) {
        log.info("Processing user update event for user: {} ({})", event.getUserId(), event.getEmail());
        // Add business logic here
        // Examples:
        // - Update cache
        // - Sync with other systems
        // - Update search index
    }

    /**
     * Handle user deleted event
     */
    private void handleUserDeleted(UserEvent event) {
        log.info("Processing user deletion event for user: {} ({})", event.getUserId(), event.getEmail());
        // Add business logic here
        // Examples:
        // - Clean up associated data
        // - Archive user information
        // - Remove from other systems
    }

    /**
     * Handle user deactivated event
     */
    private void handleUserDeactivated(UserEvent event) {
        log.info("Processing user deactivation event for user: {} ({})", event.getUserId(), event.getEmail());
        // Add business logic here
        // Examples:
        // - Revoke tokens
        // - Disable services
        // - Notify related services
    }
}
