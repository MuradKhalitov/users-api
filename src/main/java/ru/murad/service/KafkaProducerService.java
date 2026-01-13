package ru.murad.service;

import ru.murad.dto.UserEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.user-events}")
    private String userEventsTopic;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate,
                                ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendUserEvent(UserEvent userEvent) {
        try {
            String message = objectMapper.writeValueAsString(userEvent);

            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(userEventsTopic, message);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Sent user event to Kafka topic {}: {}",
                            userEventsTopic, userEvent);
                    logger.debug("Message sent to partition {} with offset {}",
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    logger.error("Unable to send message to Kafka: {}",
                            ex.getMessage(), ex);
                }
            });

        } catch (JsonProcessingException e) {
            logger.error("Error serializing user event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to serialize user event", e);
        }
    }
}
