package com.dsavitskiy.userservice.controller;

import com.dsavitskiy.userservice.AbstractIntegrationTest;
import com.dsavitskiy.userservice.dto.UserDisplayDto;
import com.dsavitskiy.userservice.entity.User;
import com.dsavitskiy.userservice.repository.PaymentCardRepository;
import com.dsavitskiy.userservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerIT extends AbstractIntegrationTest {

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public JwtDecoder jwtDecoder() {
            return token -> Jwt.withTokenValue(token)
                .header("alg", "none")
                .claim("sub", "admin")
                .claim("realm_access", Map.of("roles", List.of("ADMIN")))
                .build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        paymentCardRepository.deleteAll();
        userRepository.deleteAll();
    }

    private <T> T performAndGetResponse(MockHttpServletRequestBuilder request, Class<T> responseType) throws Exception {
        MvcResult result = mockMvc.perform(request.with(jwt())) // 👇 jwt() теперь успешно пройдет через наш тестовый JwtDecoder
            .andExpect(status().is2xxSuccessful())
            .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        return objectMapper.readValue(responseContent, responseType);
    }

    @Test
    void shouldCreateUser() throws Exception {
        String email = randomEmail();
        String json = """
                {
                  "name":"Alex",
                  "surname":"Smith",
                  "birthDate":"1995-01-01",
                  "email":"%s"
                }
                """.formatted(email);

        UserDisplayDto createdUser = performAndGetResponse(
            post("/api/users").contentType(MediaType.APPLICATION_JSON).content(json),
            UserDisplayDto.class
        );

        assertThat(createdUser.getName()).isEqualTo("Alex");
        assertThat(createdUser.getEmail()).isEqualTo(email);
    }

    @Test
    void shouldGetUserById() throws Exception {
        User expectedUser = createUser();

        UserDisplayDto actualUser = performAndGetResponse(
            get("/api/users/{id}", expectedUser.getId()),
            UserDisplayDto.class
        );

        assertThat(actualUser.getId()).isEqualTo(expectedUser.getId());
        assertThat(actualUser.getName()).isEqualTo("Alex");
        assertThat(actualUser.getEmail()).isEqualTo(expectedUser.getEmail());
    }

    @Test
    void shouldUpdateUser() throws Exception {
        User user = createUser();
        String updatedEmail = randomEmail();
        String json = """
                {
                  "name":"Updated",
                  "surname":"Smith",
                  "birthDate":"1998-05-05",
                  "email":"%s"
                }
                """.formatted(updatedEmail);

        UserDisplayDto updatedResponseUser = performAndGetResponse(
            put("/api/users/{id}", user.getId()).contentType(MediaType.APPLICATION_JSON).content(json),
            UserDisplayDto.class
        );

        assertThat(updatedResponseUser.getName()).isEqualTo("Updated");
        assertThat(updatedResponseUser.getEmail()).isEqualTo(updatedEmail);
    }

    @Test
    void shouldActivateUser() throws Exception {
        User user = createUser();
        user.setActive(false);
        userRepository.save(user);

        UserDisplayDto activatedUser = performAndGetResponse(
            patch("/api/users/{id}/activate", user.getId()),
            UserDisplayDto.class
        );

        assertThat(activatedUser.isActive()).isTrue();
    }

    @Test
    void shouldDeactivateUser() throws Exception {
        User user = createUser();
        user.setActive(true);
        userRepository.save(user);

        UserDisplayDto deactivatedUser = performAndGetResponse(
            patch("/api/users/{id}/deactivate", user.getId()),
            UserDisplayDto.class
        );

        assertThat(deactivatedUser.isActive()).isFalse();
    }

    @Test
    void shouldDeleteUser() throws Exception {
        User user = createUser();

        mockMvc.perform(delete("/api/users/{id}", user.getId()).with(jwt()))
            .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(user.getId())).isFalse();
    }

    private User createUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Alex");
        user.setSurname("Smith");
        user.setBirthDate(LocalDate.of(1995, Month.JANUARY, 1));
        user.setEmail(randomEmail());
        user.setActive(true);
        return userRepository.save(user);
    }

    private String randomEmail() {
        return UUID.randomUUID() + "@test.com";
    }
}