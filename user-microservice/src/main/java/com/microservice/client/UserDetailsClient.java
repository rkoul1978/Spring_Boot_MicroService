package com.microservice.client;

import com.microservice.event.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Client for sending user data to the user-details microservice.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserDetailsClient {

    private final RestTemplate restTemplate;

    @Value("${user-details.service.url:http://localhost:8095}")
    private String userDetailsBaseUrl;

    /**
     * Notify the user-details service that a new user was created.
     */
    public void sendUserCreated(UserEvent event) {
        String url = userDetailsBaseUrl + "/api/v1/user-details";

        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", event.getUserId());
        payload.put("email", event.getEmail());
        payload.put("firstName", event.getFirstName());
        payload.put("lastName", event.getLastName());
        payload.put("phoneNumber", event.getPhoneNumber());
        payload.put("address", event.getAddress());
        payload.put("city", event.getCity());
        payload.put("state", event.getState());
        payload.put("zipCode", event.getZipCode());
        payload.put("country", event.getCountry());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            restTemplate.postForEntity(url, new HttpEntity<>(payload, headers), Void.class);
            log.info("Notified user-details service for userId: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Failed to notify user-details service for userId: {}", event.getUserId(), e);
        }
    }
}
