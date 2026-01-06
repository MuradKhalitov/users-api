package ru.murad.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserEvent {
    private EventType eventType;
    private String email;
    private String username;

    @JsonCreator
    public UserEvent(
            @JsonProperty("eventType") EventType eventType,
            @JsonProperty("email") String email,
            @JsonProperty("username") String username) {
        this.eventType = eventType;
        this.email = email;
        this.username = username;
    }

    // Getters and Setters
    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "UserEvent{" +
                "eventType=" + eventType +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}