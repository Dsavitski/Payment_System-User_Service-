package com.dsavitskiy.userservice.controller;

import com.dsavitskiy.userservice.AbstractIntegrationTest;
import com.dsavitskiy.userservice.entity.User;
import com.dsavitskiy.userservice.repository.PaymentCardRepository;
import com.dsavitskiy.userservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@WithMockUser(username = "admin-uuid", roles = {"ADMIN"})
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

    private <T> T performAndGetResponse(MockHttpServletRequestBuilder request, Class<T> responseType) throws Exception {
        MvcResult result = mockMvc.perform(request)
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

        User createdUser = performAndGetResponse(
            post("/api/users").contentType(MediaType.APPLICATION_JSON).content(json),
            User.class
        );

        assertThat(createdUser.getName()).isEqualTo("Alex");
        assertThat(createdUser.getEmail()).isEqualTo(email);
    }

    @Test
    void shouldGetUserById() throws Exception {
        User expectedUser = createUser();

        User actualUser = performAndGetResponse(
            get("/api/users/{id}", expectedUser.getId()),
            User.class
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

        User updatedResponseUser = performAndGetResponse(
            put("/api/users/{id}", user.getId()).contentType(MediaType.APPLICATION_JSON).content(json),
            User.class
        );

        assertThat(updatedResponseUser.getName()).isEqualTo("Updated");
        assertThat(updatedResponseUser.getEmail()).isEqualTo(updatedEmail);

        User dbUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(dbUser.getName()).isEqualTo("Updated");
        assertThat(dbUser.getEmail()).isEqualTo(updatedEmail);
    }

    @Test
    void shouldActivateUser() throws Exception {
        User user = createUser();
        user.setActive(false);
        userRepository.save(user);

        User activatedUser = performAndGetResponse(
            patch("/api/users/{id}/activate", user.getId()),
            User.class
        );

        assertThat(activatedUser.isActive()).isTrue();
        assertThat(userRepository.findById(user.getId()).orElseThrow().isActive()).isTrue();
    }

    @Test
    void shouldDeactivateUser() throws Exception {
        User user = createUser();
        user.setActive(true);
        userRepository.save(user);

        User deactivatedUser = performAndGetResponse(
            patch("/api/users/{id}/deactivate", user.getId()),
            User.class
        );

        assertThat(deactivatedUser.isActive()).isFalse();
        assertThat(userRepository.findById(user.getId()).orElseThrow().isActive()).isFalse();
    }

    @Test
    void shouldDeleteUser() throws Exception {
        User user = createUser();

        mockMvc.perform(delete("/api/users/{id}", user.getId()))
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