package com.dsavitskiy.userservice.controller;

import com.dsavitskiy.userservice.entity.PaymentCard;
import com.dsavitskiy.userservice.entity.User;
import com.dsavitskiy.userservice.repository.PaymentCardRepository;
import com.dsavitskiy.userservice.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Autowired
    private ObjectMapper objectMapper;

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

    private <T> T performAndGetResponse(MockHttpServletRequestBuilder request, Class<T> responseType) throws Exception {
        MvcResult result = mockMvc.perform(request)
            .andExpect(status().is2xxSuccessful())
            .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        return objectMapper.readValue(responseContent, responseType);
    }

    private <T> List<T> performAndGetListResponse(MockHttpServletRequestBuilder request, Class<T> elementType) throws Exception {
        MvcResult result = mockMvc.perform(request)
            .andExpect(status().is2xxSuccessful())
            .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        return objectMapper.readValue(responseContent, objectMapper.getTypeFactory().constructCollectionType(List.class, elementType));
    }

    @Test
    void shouldCreatePaymentCard() throws Exception {
        User user = createUser();

        String json = """
                {
                  "userId": "%s",
                  "number": "1234567890123456",
                  "holder": "Alex Smith",
                  "expirationDate": "2030-01-01"
                }
                """.formatted(user.getId());

        PaymentCard createdCard = performAndGetResponse(
            post("/api/payment-cards").contentType(MediaType.APPLICATION_JSON).content(json),
            PaymentCard.class
        );

        assertThat(createdCard.getNumber()).isEqualTo("1234567890123456");
        assertThat(createdCard.getHolder()).isEqualTo("Alex Smith");
        assertThat(paymentCardRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldGetPaymentCardById() throws Exception {
        User user = createUser();
        PaymentCard expectedCard = createCard(user, true);

        PaymentCard actualCard = performAndGetResponse(
            get("/api/payment-cards/{id}", expectedCard.getId()),
            PaymentCard.class
        );

        assertThat(actualCard.getId()).isEqualTo(expectedCard.getId());
        assertThat(actualCard.getHolder()).isEqualTo(expectedCard.getHolder());
        assertThat(actualCard.getNumber()).isEqualTo(expectedCard.getNumber());
    }

    @Test
    void shouldUpdatePaymentCard() throws Exception {
        User user = createUser();
        PaymentCard card = createCard(user, true);

        String json = """
                {
                  "userId": "%s",
                  "number": "9999888877776666",
                  "holder": "Updated Holder",
                  "expirationDate": "2032-01-01"
                }
                """.formatted(user.getId());

        PaymentCard updatedResponseCard = performAndGetResponse(
            put("/api/payment-cards/{id}", card.getId()).contentType(MediaType.APPLICATION_JSON).content(json),
            PaymentCard.class
        );

        assertThat(updatedResponseCard.getHolder()).isEqualTo("Updated Holder");
        assertThat(updatedResponseCard.getNumber()).isEqualTo("9999888877776666");

        PaymentCard dbCard = paymentCardRepository.findById(card.getId()).orElseThrow();
        assertThat(dbCard.getHolder()).isEqualTo("Updated Holder");
        assertThat(dbCard.getNumber()).isEqualTo("9999888877776666");
    }

    @Test
    void shouldGetCardsByUserId() throws Exception {
        User user = createUser();
        createCard(user, true);
        createCard(user, false);

        List<PaymentCard> cards = performAndGetListResponse(
            get("/api/payment-cards/user/{userId}", user.getId()),
            PaymentCard.class
        );

        assertThat(cards).hasSize(2);
    }

    @Test
    void shouldGetActiveCardsByUserId() throws Exception {
        User user = createUser();
        createCard(user, true);
        createCard(user, false);

        List<PaymentCard> activeCards = performAndGetListResponse(
            get("/api/payment-cards/user/{userId}/activeCards", user.getId()),
            PaymentCard.class
        );

        assertThat(activeCards).hasSize(1);
        assertThat(activeCards.get(0).isActive()).isTrue();
    }

    @Test
    void shouldGetAllPaymentCardsPaginated() throws Exception {
        User user = createUser();
        createCard(user, true);

        MvcResult result = mockMvc.perform(get("/api/payment-cards")
                .param("page", "0")
                .param("size", "10"))
            .andExpect(status().isOk())
            .andReturn();

        Page<PaymentCard> page = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            objectMapper.getTypeFactory().constructType(new TypeReference<Page<PaymentCard>>() {})
        );

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getTotalPages()).isEqualTo(1);
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

        PaymentCard responseCard = performAndGetResponse(
            patch("/api/payment-cards/{id}/activate", card.getId()),
            PaymentCard.class
        );

        assertThat(responseCard.isActive()).isTrue();
        assertThat(paymentCardRepository.findById(card.getId()).orElseThrow().isActive()).isTrue();
    }

    @Test
    void shouldDeactivatePaymentCard() throws Exception {
        User user = createUser();
        PaymentCard card = createCard(user, true);

        PaymentCard responseCard = performAndGetResponse(
            patch("/api/payment-cards/{id}/deactivate", card.getId()),
            PaymentCard.class
        );

        assertThat(responseCard.isActive()).isFalse();
        assertThat(paymentCardRepository.findById(card.getId()).orElseThrow().isActive()).isFalse();
    }
}