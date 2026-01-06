package ru.murad.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.murad.service.KafkaProducerService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class UserApiIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:12.3")
            .withDatabaseName("user_db")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7").withExposedPorts(6379);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl());
        registry.add("spring.datasource.username", () -> postgres.getUsername());
        registry.add("spring.datasource.password", () -> postgres.getPassword());

        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "user_schema");
        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/db.changelog-master.xml");
        registry.add("spring.liquibase.default-schema", () -> "user_schema");
        registry.add("spring.liquibase.liquibase-schema", () -> "public");

        registry.add("spring.cache.type", () -> "redis");
        registry.add("spring.data.redis.host", () -> redis.getHost());
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));

        registry.add("spring.jpa.show-sql", () -> "false");

        // Добавляем конфигурацию для Kafka
        registry.add("kafka.topics.user-events", () -> "user-events");
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
        registry.add("spring.kafka.producer.key-serializer", () -> "org.apache.kafka.common.serialization.StringSerializer");
        registry.add("spring.kafka.producer.value-serializer", () -> "org.springframework.kafka.support.serializer.JsonSerializer");
        registry.add("spring.kafka.consumer.group-id", () -> "test-group");
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.kafka.consumer.key-deserializer", () -> "org.apache.kafka.common.serialization.StringDeserializer");
        registry.add("spring.kafka.consumer.value-deserializer", () -> "org.springframework.kafka.support.serializer.JsonDeserializer");
        registry.add("spring.kafka.properties.spring.json.trusted.packages", () -> "*");
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    // Мокаем KafkaProducerService чтобы тесты не зависели от реального Kafka
    @MockBean
    KafkaProducerService kafkaProducerService;

    @Test
    @Transactional
    void full_flow_crud_and_cache() throws Exception {
        // CREATE
        var create = """
        {
            "fio": "IT Test",
            "phoneNumber": "+79001112233",
            "email": "test@example.com",
            "avatar": "https://via.placeholder.com/150",
            "role": "ROLE_USER"
        }
        """;
        var createRs = mvc.perform(post("/api/createNewUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(create))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andReturn();

        var json = createRs.getResponse().getContentAsString();
        var id = om.readTree(json).get("uuid").asText();

        // GET (MISS -> наполняем кэш)
        mvc.perform(get("/api/users").param("userID", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fio").value("IT Test"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));

        // UPDATE (CachePut)
        var update = """
        {
            "uuid": "%s",
            "fio": "IT Test Updated",
            "phoneNumber": "+79005557788",
            "email": "test2@example.com",
            "avatar": "https://via.placeholder.com/300",
            "role": "ROLE_ADMIN"
        }
        """.formatted(id);
        mvc.perform(put("/api/userDetailsUpdate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(update))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fio").value("IT Test Updated"))
                .andExpect(jsonPath("$.email").value("test2@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));

        // GET after UPDATE (должны увидеть обновлённые данные — попали в кэш)
        mvc.perform(get("/api/users").param("userID", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fio").value("IT Test Updated"))
                .andExpect(jsonPath("$.email").value("test2@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));

        // DELETE (CacheEvict)
        mvc.perform(delete("/api/users").param("userID", id))
                .andExpect(status().isNoContent());

        // GET -> 404
        mvc.perform(get("/api/users").param("userID", id))
                .andExpect(status().isNotFound());
    }
}