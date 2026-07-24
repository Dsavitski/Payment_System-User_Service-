package com.dsavitskiy.userservice.controller;

import com.dsavitskiy.userservice.entity.PaymentCard;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class PaymentCardControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @BeforeEach
    void setUp() {
        paymentCardRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User createUser() {
        User user = new User();
        user.setName("Alex");
        user.setSurname("Smith");
        user.setBirthDate(LocalDate.of(1995, Month.JANUARY, 1));
        user.setEmail(UUID.randomUUID() + "@test.com");
        user.setActive(true);
        return userRepository.save(user);
    }

    private PaymentCard createCard(User user, boolean isActive) {
        PaymentCard card = new PaymentCard();
        card.setUser(user);
        card.setNumber(generateCardNumber());
        card.setHolder("Alex Smith");
        card.setExpirationDate(LocalDate.now().plusYears(5));
        card.setActive(isActive);
        return paymentCardRepository.save(card);
    }

    private String generateCardNumber() {
        StringBuilder number = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            number.append(ThreadLocalRandom.current().nextInt(10));
        }
        return number.toString();
    }

    @Test
    void shouldCreatePaymentCard() throws Exception {
        User user = createUser();

        String json = """
                {
                  "userId": %d,
                  "number": "1234567890123456",
                  "holder": "Alex Smith",
                  "expirationDate": "2030-01-01"
                }
                """.formatted(user.getId());

        mockMvc.perform(post("/api/payment-cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.number").value("1234567890123456"))
            .andExpect(jsonPath("$.holder").value("Alex Smith"));

        assertThat(paymentCardRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldGetPaymentCardById() throws Exception {
        User user = createUser();
        PaymentCard card = createCard(user, true);

        mockMvc.perform(get("/api/payment-cards/{id}", card.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(card.getId()))
            .andExpect(jsonPath("$.holder").value("Alex Smith"))
            .andExpect(jsonPath("$.number").value(card.getNumber()));
    }

    @Test
    void shouldUpdatePaymentCard() throws Exception {
        User user = createUser();
        PaymentCard card = createCard(user, true);

        String json = """
                {
                  "userId": %d,
                  "number": "9999888877776666",
                  "holder": "Updated Holder",
                  "expirationDate": "2032-01-01"
                }
                """.formatted(user.getId());

        mockMvc.perform(put("/api/payment-cards/{id}", card.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.holder").value("Updated Holder"))
            .andExpect(jsonPath("$.number").value("9999888877776666"));

        PaymentCard updatedCard = paymentCardRepository.findById(card.getId()).orElseThrow();

        assertThat(updatedCard.getHolder()).isEqualTo("Updated Holder");
        assertThat(updatedCard.getNumber()).isEqualTo("9999888877776666");
    }

    @Test
    void shouldGetCardsByUserId() throws Exception {
        User user = createUser();
        createCard(user, true);
        createCard(user, false);

        mockMvc.perform(get("/api/payment-cards/user/{userId}", user.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldGetActiveCardsByUserId() throws Exception {
        User user = createUser();
        createCard(user, true);
        createCard(user, false);

        mockMvc.perform(get("/api/payment-cards/user/{userId}/activeCards", user.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldGetAllPaymentCardsPaginated() throws Exception {
        User user = createUser();
        createCard(user, true);

        mockMvc.perform(get("/api/payment-cards")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void shouldDeletePaymentCard() throws Exception {
        User user = createUser();
        PaymentCard card = createCard(user, true);

        mockMvc.perform(delete("/api/payment-cards/{id}", card.getId()))
            .andExpect(status().isNoContent());

        assertThat(paymentCardRepository.findById(card.getId())).isEmpty();
        assertThat(paymentCardRepository.count()).isZero();
    }

    @Test
    void shouldActivatePaymentCard() throws Exception {
        User user = createUser();
        PaymentCard card = createCard(user, false);

        mockMvc.perform(patch("/api/payment-cards/{id}/activate", card.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.active").value(true));

        assertThat(paymentCardRepository.findById(card.getId()).orElseThrow().isActive()).isTrue();
    }

    @Test
    void shouldDeactivatePaymentCard() throws Exception {
        User user = createUser();
        PaymentCard card = createCard(user, true);

        mockMvc.perform(patch("/api/payment-cards/{id}/deactivate", card.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.active").value(false));

        assertThat(paymentCardRepository.findById(card.getId()).orElseThrow().isActive()).isFalse();
    }
}