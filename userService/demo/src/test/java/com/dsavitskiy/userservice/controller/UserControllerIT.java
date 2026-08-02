package com.dsavitskiy.userservice.controller;

import com.dsavitskiy.userservice.AbstractIntegrationTest;
import com.dsavitskiy.userservice.dto.UserDisplayDto;
import com.dsavitskiy.userservice.entity.User;
import com.dsavitskiy.userservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class UserControllerIT extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Этот способ инициализации гарантированно работает и убирает ошибку IDE
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        userRepository.deleteAll();
    }

    private <T> T performAndGetResponse(MockHttpServletRequestBuilder request, Class<T> responseType) throws Exception {
        MvcResult result = mockMvc.perform(request.with(SecurityMockMvcRequestPostProcessors.jwt()))
            .andExpect(status().is2xxSuccessful())
            .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), responseType);
    }

    @Test
    void shouldCreateUser() throws Exception {
        String json = """
                {
                  "name":"Alex",
                  "surname":"Smith",
                  "birthDate":"1995-01-01",
                  "email":"%s"
                }
                """.formatted(UUID.randomUUID() + "@test.com");

        UserDisplayDto createdUser = performAndGetResponse(
            post("/api/users").contentType(MediaType.APPLICATION_JSON).content(json),
            UserDisplayDto.class
        );

        assertThat(createdUser.getName()).isEqualTo("Alex");
    }

    @Test
    void shouldGetUserById() throws Exception {
        User expectedUser = createUser();

        UserDisplayDto actualUser = performAndGetResponse(
            get("/api/users/{id}", expectedUser.getId()),
            UserDisplayDto.class
        );

        assertThat(actualUser.getId()).isEqualTo(expectedUser.getId());
    }

    @Test
    void shouldUpdateUser() throws Exception {
        User user = createUser();
        String json = """
                {
                  "name":"Updated",
                  "surname":"Smith",
                  "birthDate":"1998-05-05",
                  "email":"%s"
                }
                """.formatted(UUID.randomUUID() + "@test.com");

        UserDisplayDto updatedUser = performAndGetResponse(
            put("/api/users/{id}", user.getId()).contentType(MediaType.APPLICATION_JSON).content(json),
            UserDisplayDto.class
        );

        assertThat(updatedUser.getName()).isEqualTo("Updated");
    }

    @Test
    void shouldDeleteUser() throws Exception {
        User user = createUser();

        mockMvc.perform(delete("/api/users/{id}", user.getId())
                .with(SecurityMockMvcRequestPostProcessors.jwt()))
            .andExpect(status().isNoContent());

        assertThat(userRepository.existsById(user.getId())).isFalse();
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
}