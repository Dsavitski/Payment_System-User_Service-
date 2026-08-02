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
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

 class UserControllerIT extends AbstractIntegrationTest {

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

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor adminJwt(UUID id) {
        return jwt()
            .jwt(jwt -> jwt
                .subject(id.toString())
                .claim("sub", id.toString())
                .claim("realm_access", Map.of("roles", List.of("ADMIN"))))
            .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private User createUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Alex");
        user.setSurname("Smith");
        user.setBirthDate(LocalDate.of(1995, Month.JANUARY, 1));
        user.setEmail(UUID.randomUUID() + "@test.com");
        user.setActive(true);

        return userRepository.save(user);
    }

    @Test
    void shouldCreateUser() throws Exception {

        String email = UUID.randomUUID() + "@gmail.com";

        String json = """
                {
                  "name":"Alex",
                  "surname":"Smith",
                  "birthDate":"1995-01-01",
                  "email":"%s"
                }
                """.formatted(email);

        MvcResult result = mockMvc.perform(post("/api/users")
                .with(adminJwt(UUID.randomUUID()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andReturn();

        UserDisplayDto response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            UserDisplayDto.class
        );

        assertThat(response.getName()).isEqualTo("Alex");
        assertThat(response.getSurname()).isEqualTo("Smith");
        assertThat(response.getEmail()).isEqualTo(email);
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void shouldGetUserById() throws Exception {

        User user = createUser();

        MvcResult result = mockMvc.perform(get("/api/users/{id}", user.getId())
                .with(adminJwt(UUID.randomUUID())))
            .andExpect(status().isOk())
            .andReturn();

        UserDisplayDto response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            UserDisplayDto.class
        );

        assertThat(response.getId()).isEqualTo(user.getId());
        assertThat(response.getName()).isEqualTo(user.getName());
        assertThat(response.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void shouldUpdateUser() throws Exception {

        User user = createUser();

        String email = UUID.randomUUID() + "@gmail.com";

        String json = """
                {
                  "name":"Updated",
                  "surname":"UpdatedSurname",
                  "birthDate":"1990-10-10",
                  "email":"%s"
                }
                """.formatted(email);

        MvcResult result = mockMvc.perform(put("/api/users/{id}", user.getId())
                .with(adminJwt(UUID.randomUUID()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andReturn();

        UserDisplayDto response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            UserDisplayDto.class
        );

        assertThat(response.getName()).isEqualTo("Updated");
        assertThat(response.getSurname()).isEqualTo("UpdatedSurname");
        assertThat(response.getEmail()).isEqualTo(email);
    }
    @Test
    void shouldActivateUser() throws Exception {

        User user = createUser();
        user.setActive(false);
        userRepository.save(user);

        MvcResult result = mockMvc.perform(
                patch("/api/users/{id}/activate", user.getId())
                    .with(adminJwt(UUID.randomUUID())))
            .andExpect(status().isOk())
            .andReturn();

        UserDisplayDto response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            UserDisplayDto.class
        );

        assertThat(response.isActive()).isTrue();

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.isActive()).isTrue();
    }

    @Test
    void shouldDeactivateUser() throws Exception {

        User user = createUser();
        user.setActive(true);
        userRepository.save(user);

        MvcResult result = mockMvc.perform(
                patch("/api/users/{id}/deactivate", user.getId())
                    .with(adminJwt(UUID.randomUUID())))
            .andExpect(status().isOk())
            .andReturn();

        UserDisplayDto response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            UserDisplayDto.class
        );

        assertThat(response.isActive()).isFalse();

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.isActive()).isFalse();
    }

    @Test
    void shouldDeleteUser() throws Exception {

        User user = createUser();

        mockMvc.perform(delete("/api/users/{id}", user.getId())
                .with(adminJwt(UUID.randomUUID())))
            .andExpect(status().isNoContent());

        assertThat(userRepository.findById(user.getId())).isEmpty();
    }
}