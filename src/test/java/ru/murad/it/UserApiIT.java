package ru.murad.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class UserApiIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
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
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @Test
    void full_flow_crud_and_cache() throws Exception {
        // CREATE
        var create = """
        {"fio":"IT Test","phoneNumber":"+79001112233","avatar":"https://via.placeholder.com/150","role":"ROLE_USER"}
        """;
        var createRs = mvc.perform(post("/api/createNewUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(create))
                .andExpect(status().isCreated())
                .andReturn();

        var json = createRs.getResponse().getContentAsString();
        var id = om.readTree(json).get("uuid").asText();

        // GET (MISS -> наполняем кэш)
        mvc.perform(get("/api/users").param("userID", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fio").value("IT Test"));

        // UPDATE (CachePut)
        var update = """
        {"uuid":"%s","fio":"IT Test Updated","phoneNumber":"+79005557788","avatar":"https://via.placeholder.com/300","role":"ROLE_ADMIN"}
        """.formatted(id);
        mvc.perform(put("/api/userDetailsUpdate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(update))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fio").value("IT Test Updated"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));

        // GET after UPDATE (должны увидеть обновлённые данные — попали в кэш)
        mvc.perform(get("/api/users").param("userID", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));

        // DELETE (CacheEvict)
        mvc.perform(delete("/api/users").param("userID", id))
                .andExpect(status().isNoContent());

        // GET -> 404
        mvc.perform(get("/api/users").param("userID", id))
                .andExpect(status().isNotFound());
    }
}
