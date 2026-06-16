package com.microservice.kafka;

import com.microservice.dto.UserDTO;
import com.microservice.event.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Kafka producer service for publishing user events
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaUserEventProducer {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    @Value("${spring.kafka.topics.user-events:user-events}")
    private String userEventsTopic;

    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * Publish user created event
     */
    public void publishUserCreatedEvent(UserDTO userDTO) {
        UserEvent event = buildUserEvent(userDTO, "CREATED");
        publishEvent(event);
        log.info("User created event published for user ID: {}", userDTO.getId());
    }

    /**
     * Publish user updated event
     */
    public void publishUserUpdatedEvent(UserDTO userDTO) {
        UserEvent event = buildUserEvent(userDTO, "UPDATED");
        publishEvent(event);
        log.info("User updated event published for user ID: {}", userDTO.getId());
    }

    /**
     * Publish user deleted event
     */
    public void publishUserDeletedEvent(UserDTO userDTO) {
        UserEvent event = buildUserEvent(userDTO, "DELETED");
        publishEvent(event);
        log.info("User deleted event published for user ID: {}", userDTO.getId());
    }

    /**
     * Publish user deactivated event
     */
    public void publishUserDeactivatedEvent(UserDTO userDTO) {
        UserEvent event = buildUserEvent(userDTO, "DEACTIVATED");
        publishEvent(event);
        log.info("User deactivated event published for user ID: {}", userDTO.getId());
    }

    /**
     * Build UserEvent from UserDTO
     */
    private UserEvent buildUserEvent(UserDTO userDTO, String eventType) {
        return UserEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .userId(userDTO.getId())
                .email(userDTO.getEmail())
                .firstName(userDTO.getFirstName())
                .lastName(userDTO.getLastName())
                .phoneNumber(userDTO.getPhoneNumber())
                .address(userDTO.getAddress())
                .city(userDTO.getCity())
                .state(userDTO.getState())
                .zipCode(userDTO.getZipCode())
                .country(userDTO.getCountry())
                .active(userDTO.isActive())
                .timestamp(LocalDateTime.now())
                .source(applicationName)
                .build();
    }

    /**
     * Publish event to Kafka topic
     */
    private void publishEvent(UserEvent event) {
        try {
            Message<UserEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, userEventsTopic)
                    .setHeader(KafkaHeaders.KEY, event.getUserId())
                    .setHeader("eventType", event.getEventType())
                    .build();

            kafkaTemplate.send(message);
            log.debug("Event sent to Kafka topic: {} - Event: {}", userEventsTopic, event);
        } catch (Exception e) {
            log.error("Failed to publish event to Kafka: {}", event, e);
            // In production, you might want to handle this with retry logic or a DLQ
        }
    }
}
