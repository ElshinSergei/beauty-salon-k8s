package ru.elshin.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AuthControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void register_ShouldReturnCreated() throws Exception {
        String json = "{\"name\": \"Test User\", \"email\": \"test@test.com\", \"password\": \"password123\", \"phone\": \"+79998887766\", \"role\": \"CLIENT\"}";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void register_ShouldReturnConflict_WhenEmailExists() throws Exception {
        String json = "{\"name\": \"Test User\", \"email\": \"existing@test.com\", \"password\": \"password123\", \"phone\": \"+79998887766\", \"role\": \"CLIENT\"}";
        
        // Первый раз успешно
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Второй раз - конфликт
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    void login_ShouldReturnToken() throws Exception {
        // Сначала регистрируем
        String registerJson = "{\"name\": \"Test User\", \"email\": \"login@test.com\", \"password\": \"pass123\", \"phone\": \"+79998887766\", \"role\": \"CLIENT\"}";
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson));

        // Затем логинимся
        String loginJson = "{\"email\": \"login@test.com\", \"password\": \"pass123\"}";
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenWrongPassword() throws Exception {
        // Регистрируем
        String registerJson = "{\"name\": \"Test User\", \"email\": \"wrongpass@test.com\", \"password\": \"correctPass\", \"phone\": \"+79998887766\", \"role\": \"CLIENT\"}";
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson));

        // Логинимся с неправильным паролем
        String loginJson = "{\"email\": \"wrongpass@test.com\", \"password\": \"wrongPass\"}";
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isUnauthorized());
    }
}
