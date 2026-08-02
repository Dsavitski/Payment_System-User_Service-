package com.dsavitskiy.userservice.controller;

import com.dsavitskiy.userservice.AbstractIntegrationTest;
import com.dsavitskiy.userservice.dto.PaymentCardDisplayDto;
import com.dsavitskiy.userservice.entity.PaymentCard;
import com.dsavitskiy.userservice.entity.User;
import com.dsavitskiy.userservice.repository.PaymentCardRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class PaymentCardControllerIT extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        paymentCardRepository.deleteAll();
        userRepository.deleteAll();
    }

    private <T> T performAndGetResponse(MockHttpServletRequestBuilder request, Class<T> responseType) throws Exception {
        MvcResult result = mockMvc.perform(request.with(SecurityMockMvcRequestPostProcessors.jwt()))
            .andExpect(status().is2xxSuccessful())
            .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), responseType);
    }

    private <T> List<T> performAndGetListResponse(MockHttpServletRequestBuilder request, Class<T> elementType) throws Exception {
        MvcResult result = mockMvc.perform(request.with(SecurityMockMvcRequestPostProcessors.jwt()))
            .andExpect(status().is2xxSuccessful())
            .andReturn();

        return objectMapper.readValue(
            result.getResponse().getContentAsString(),
            objectMapper.getTypeFactory().constructCollectionType(List.class, elementType)
        );
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

        PaymentCardDisplayDto createdCard = performAndGetResponse(
            post("/api/payment-cards").contentType(MediaType.APPLICATION_JSON).content(json),
            PaymentCardDisplayDto.class
        );

        assertThat(createdCard.getNumber()).isEqualTo("1234567890123456");
        assertThat(paymentCardRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldGetPaymentCardById() throws Exception {
        User user = createUser();
        PaymentCard expectedCard = createCard(user, true);

        PaymentCardDisplayDto actualCard = performAndGetResponse(
            get("/api/payment-cards/{id}", expectedCard.getId()),
            PaymentCardDisplayDto.class
        );

        assertThat(actualCard.getId()).isEqualTo(expectedCard.getId());
    }

    @Test
    void shouldGetCardsByUserId() throws Exception {
        User user = createUser();
        createCard(user, true);
        createCard(user, false);

        List<PaymentCardDisplayDto> cards = performAndGetListResponse(
            get("/api/payment-cards/user/{userId}", user.getId()),
            PaymentCardDisplayDto.class
        );

        assertThat(cards).hasSize(2);
    }

    @Test
    void shouldDeletePaymentCard() throws Exception {
        User user = createUser();
        PaymentCard card = createCard(user, true);

        mockMvc.perform(delete("/api/payment-cards/{id}", card.getId())
                .with(SecurityMockMvcRequestPostProcessors.jwt()))
            .andExpect(status().isNoContent());

        assertThat(paymentCardRepository.findById(card.getId())).isEmpty();
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

    private PaymentCard createCard(User user, boolean active) {
        PaymentCard card = new PaymentCard();
        card.setUser(user);
        card.setNumber("1111222233334444");
        card.setHolder("Alex Smith");
        card.setExpirationDate(LocalDate.now().plusYears(5));
        card.setActive(active);
        return paymentCardRepository.save(card);
    }
}