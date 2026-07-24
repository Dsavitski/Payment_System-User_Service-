package com.dsavitskiy.userservice.controller;

import com.dsavitskiy.userservice.AbstractIntegrationTest;
import com.dsavitskiy.userservice.entity.User;
import com.dsavitskiy.userservice.repository.PaymentCardRepository;
import com.dsavitskiy.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class UserControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @BeforeEach
    void cleanDatabase() {
        paymentCardRepository.deleteAll();
        userRepository.deleteAll();
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

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Alex"))
            .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void shouldGetUserById() throws Exception {

        User user = createUser();

        mockMvc.perform(get("/users/{id}", user.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(user.getId()))
            .andExpect(jsonPath("$.name").value("Alex"))
            .andExpect(jsonPath("$.email").value(user.getEmail()));
    }

    @Test
    void shouldUpdateUser() throws Exception {

        User user = createUser();

        String json = """
                {
                  "name":"Updated",
                  "surname":"Smith",
                  "birthDate":"1998-05-05",
                  "email":"updated@test.com"
                }
                """;

        mockMvc.perform(put("/users/{id}", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated"))
            .andExpect(jsonPath("$.email").value("updated@test.com"));

        User updated = userRepository.findById(user.getId()).orElseThrow();

        assertEquals("Updated", updated.getName());
        assertEquals("updated@test.com", updated.getEmail());
    }

    @Test
    void shouldActivateUser() throws Exception {

        User user = createUser();

        user.setActive(false);
        userRepository.save(user);

        mockMvc.perform(patch("/users/{id}/activate", user.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.active").value(true));

        User result = userRepository.findById(user.getId()).orElseThrow();

        assertTrue(result.isActive());
    }

    @Test
    void shouldDeactivateUser() throws Exception {

        User user = createUser();

        user.setActive(true);
        userRepository.save(user);

        mockMvc.perform(patch("/users/{id}/deactivate", user.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.active").value(false));

        User result = userRepository.findById(user.getId()).orElseThrow();

        assertFalse(result.isActive());
    }

    @Test
    void shouldDeleteUser() throws Exception {

        User user = createUser();

        mockMvc.perform(delete("/users/{id}", user.getId()))
            .andExpect(status().isNoContent());

        assertFalse(userRepository.existsById(user.getId()));
    }

    private User createUser() {

        User user = new User();

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