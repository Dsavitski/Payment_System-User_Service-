package com.dsavitskiy.userservice.controller;

import com.dsavitskiy.userservice.dto.PaymentCardCreateDto; // Замените на ваш реальный DTO
import com.dsavitskiy.userservice.entity.PaymentCard;
import com.dsavitskiy.userservice.entity.User;
import com.dsavitskiy.userservice.integration.IntegrationTest;
import com.dsavitskiy.userservice.repository.PaymentCardRepository;
import com.dsavitskiy.userservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class PaymentCardControllerIntegrationTest extends IntegrationTest {

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository cardRepository;

    @Test
    void createCards_shouldAllowUpTo5Cards() throws Exception {
        User user = new User();
        user.setName("John");
        user.setSurname("Doe");
        user.setEmail("john@doe.com");
        user.setActive(true);
        user = userRepository.save(user);

        for (int i = 1; i <= 5; i++) {
            PaymentCardCreateDto dto = new PaymentCardCreateDto();
            dto.setNumber("111122223333444" + i);
            dto.setHolder("JOHN DOE");
            dto.setExpirationDate(LocalDate.now().plusYears(1));

            mockMvc.perform(post("/users/{userId}/cards", user.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
        }

        assertEquals(5, cardRepository.countByUserId(user.getId()));
    }

    @Test
    void createCard_shouldReject6thCard() throws Exception {
        User user = new User();
        user.setName("Jane");
        user.setSurname("Doe");
        user.setEmail("jane@doe.com");
        user.setActive(true);
        user = userRepository.save(user);

        for (int i = 1; i <= 5; i++) {
            PaymentCard card = new PaymentCard();
            card.setUser(user);
            card.setNumber("111122223333444" + i);
            card.setHolder("JANE DOE");
            card.setExpirationDate(LocalDate.now().plusYears(1));
            card.setActive(true);
            cardRepository.save(card);
        }

        PaymentCardCreateDto dto = new PaymentCardCreateDto();
        dto.setNumber("9999888877776666");
        dto.setHolder("JANE DOE");
        dto.setExpirationDate(LocalDate.now().plusYears(1));

        mockMvc.perform(post("/users/{userId}/cards", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest()); // Если у вас возвращается 500, замените на isInternalServerError()
    }

    @Test
    void getCardsByUserId_shouldReturnOnlyUserCards() throws Exception {
        User user1 = userRepository.save(createUser("User", "One", "u1@test.com"));
        User user2 = userRepository.save(createUser("User", "Two", "u2@test.com"));

        PaymentCard card1 = createCard(user1, "1111");
        PaymentCard card2 = createCard(user1, "2222");
        PaymentCard card3 = createCard(user2, "3333");

        cardRepository.saveAll(java.util.List.of(card1, card2, card3));

        mockMvc.perform(get("/users/{userId}/cards", user1.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2))) // Только 2 карты первого пользователя
            .andExpect(jsonPath("$[0].number", org.hamcrest.Matchers.is("1111")))
            .andExpect(jsonPath("$[1].number", org.hamcrest.Matchers.is("2222")));
    }

    private User createUser(String name, String surname, String email) {
        User u = new User();
        u.setName(name);
        u.setSurname(surname);
        u.setEmail(email);
        u.setActive(true);
        return u;
    }

    private PaymentCard createCard(User user, String number) {
        PaymentCard c = new PaymentCard();
        c.setUser(user);
        c.setNumber(number);
        c.setHolder("HOLDER");
        c.setExpirationDate(LocalDate.now().plusYears(1));
        c.setActive(true);
        return c;
    }
}