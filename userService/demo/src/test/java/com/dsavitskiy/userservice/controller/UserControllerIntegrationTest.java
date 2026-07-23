package com.dsavitskiy.userservice.controller;

import com.dsavitskiy.userservice.dto.PaymentCardCreateDto;
import com.dsavitskiy.userservice.dto.UserCreateDto; // Замените на ваш реальный пакет DTO
import com.dsavitskiy.userservice.entity.User;
import com.dsavitskiy.userservice.integration.IntegrationTest;
import com.dsavitskiy.userservice.repository.PaymentCardRepository;
import com.dsavitskiy.userservice.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerIntegrationTest extends IntegrationTest {

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository cardRepository;

    @Test
    void createAndFindUser_flowTest() throws Exception {
        UserCreateDto createDto = new UserCreateDto();
        createDto.setName("Ivan");
        createDto.setSurname("Ivanov");
        createDto.setEmail("ivan@test.com");
        createDto.setBirthDate(LocalDate.of(1990, 1, 1));

        MvcResult result = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name", is("Ivan")))
            .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        Long createdUserId = jsonNode.get("id").asLong();
        assertNotNull(createdUserId);

        assertEquals(1, userRepository.count());

        User savedUser = userRepository.findUserWithPaymentCardsById(createdUserId).orElseThrow();

        assertEquals("Ivan", savedUser.getName());
        assertEquals("ivan@test.com", savedUser.getEmail());
        assertTrue(savedUser.getPaymentCards().isEmpty()); // Карт пока нет
    }

    @Test
    void cardLimit_flowTest() throws Exception {
        User user = new User();
        user.setName("CardTester");
        user.setSurname("Tester");
        user.setEmail("cards@test.com");
        user.setActive(true);
        user = userRepository.save(user);

        for (int i = 1; i <= 5; i++) {
            PaymentCardCreateDto cardDto = new PaymentCardCreateDto();
            cardDto.setNumber("111122223333444" + i);
            cardDto.setHolder("CARD TESTER");
            cardDto.setExpirationDate(LocalDate.now().plusYears(1));

            mockMvc.perform(post("/users/{userId}/cards", user.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isCreated());
        }

        assertEquals(5, cardRepository.countByUserId(user.getId()));

        PaymentCardCreateDto sixthCardDto = new PaymentCardCreateDto();
        sixthCardDto.setNumber("9999888877776666");
        sixthCardDto.setHolder("CARD TESTER");
        sixthCardDto.setExpirationDate(LocalDate.now().plusYears(1));

        mockMvc.perform(post("/users/{userId}/cards", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sixthCardDto)))
            .andExpect(status().isBadRequest());
    }
}