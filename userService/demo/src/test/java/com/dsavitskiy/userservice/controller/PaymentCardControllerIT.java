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

 class PaymentCardControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @Autowired
    private UserRepository userRepository;

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
        user.setEmail(UUID.randomUUID() + "@gmail.com");
        user.setActive(true);

        return userRepository.save(user);
    }

    private PaymentCard createCard(User user) {

        PaymentCard card = new PaymentCard();
        card.setUser(user);
        card.setNumber("1111222233334444");
        card.setHolder("Alex Smith");
        card.setExpirationDate(LocalDate.now().plusYears(3));
        card.setActive(true);

        return paymentCardRepository.save(card);
    }

    @Test
    void shouldCreatePaymentCard() throws Exception {

        User user = createUser();

        String json = """
            {
              "userId":"%s",
              "number":"5555444433332222",
              "holder":"Alex Smith",
              "expirationDate":"2030-12-31"
            }
            """.formatted(user.getId());

        MvcResult result = mockMvc.perform(post("/api/payment-cards")
                .with(adminJwt(UUID.randomUUID()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andReturn();

        PaymentCardDisplayDto response =
            objectMapper.readValue(result.getResponse().getContentAsString(),
                PaymentCardDisplayDto.class);

        assertThat(response.getUserId()).isEqualTo(user.getId());
        assertThat(response.getHolder()).isEqualTo("Alex Smith");
        assertThat(response.getNumber()).isEqualTo("5555444433332222");
    }

    @Test
    void shouldGetPaymentCardById() throws Exception {

        User user = createUser();
        PaymentCard card = createCard(user);

        MvcResult result = mockMvc.perform(
                get("/api/payment-cards/{id}", card.getId())
                    .with(userJwt(user.getId())))
            .andExpect(status().isOk())
            .andReturn();

        PaymentCardDisplayDto response =
            objectMapper.readValue(result.getResponse().getContentAsString(),
                PaymentCardDisplayDto.class);

        assertThat(response.getId()).isEqualTo(card.getId());
        assertThat(response.getUserId()).isEqualTo(user.getId());
    }

    @Test
    void shouldUpdatePaymentCard() throws Exception {

        User user = createUser();
        PaymentCard card = createCard(user);

        String json = """
            {
              "userId":"%s",
              "number":"9999888877776666",
              "holder":"Updated Holder",
              "expirationDate":"2031-01-01"
            }
            """.formatted(user.getId());

        MvcResult result = mockMvc.perform(
                put("/api/payment-cards/{id}", card.getId())
                    .with(adminJwt(UUID.randomUUID()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
            .andExpect(status().isOk())
            .andReturn();

        PaymentCardDisplayDto response =
            objectMapper.readValue(result.getResponse().getContentAsString(),
                PaymentCardDisplayDto.class);

        assertThat(response.getHolder()).isEqualTo("Updated Holder");
        assertThat(response.getNumber()).isEqualTo("9999888877776666");
    }
    @Test
    void shouldGetCardsByUserId() throws Exception {

        User user = createUser();
        createCard(user);

        MvcResult result = mockMvc.perform(
                get("/api/payment-cards/user/{userId}", user.getId())
                    .with(userJwt(user.getId())))
            .andExpect(status().isOk())
            .andReturn();

        PaymentCardDisplayDto[] response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            PaymentCardDisplayDto[].class
        );

        assertThat(response).hasSize(1);
        assertThat(response[0].getUserId()).isEqualTo(user.getId());
    }

    @Test
    void shouldGetActiveCards() throws Exception {

        User user = createUser();

        PaymentCard active = createCard(user);

        PaymentCard inactive = new PaymentCard();
        inactive.setUser(user);
        inactive.setNumber("4444333322221111");
        inactive.setHolder("Alex Smith");
        inactive.setExpirationDate(LocalDate.now().plusYears(2));
        inactive.setActive(false);
        paymentCardRepository.save(inactive);

        MvcResult result = mockMvc.perform(
                get("/api/payment-cards/user/{userId}/activeCards", user.getId())
                    .with(userJwt(user.getId())))
            .andExpect(status().isOk())
            .andReturn();

        PaymentCardDisplayDto[] response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            PaymentCardDisplayDto[].class
        );

        assertThat(response).hasSize(1);
        assertThat(response[0].getId()).isEqualTo(active.getId());
    }

     @Test
     void shouldActivateCard() throws Exception {
         User user = createUser();
         PaymentCard card = createCard(user);

         card.setActive(false);
         paymentCardRepository.save(card);

         MvcResult result = mockMvc.perform(
                 patch("/api/payment-cards/{id}/activate", card.getId())
                     .with(adminJwt(user.getId())))
             .andExpect(status().isOk())
             .andReturn();

         PaymentCardDisplayDto response = objectMapper.readValue(
             result.getResponse().getContentAsString(),
             PaymentCardDisplayDto.class
         );

         assertThat(response.isActive()).isTrue();

         PaymentCard updated = paymentCardRepository.findById(card.getId()).orElseThrow();
         assertThat(updated.isActive()).isTrue();
     }


     @Test
     void shouldDeactivateCard() throws Exception {
         User user = createUser();
         PaymentCard card = createCard(user);

         card.setActive(true);
         paymentCardRepository.save(card);

         MvcResult result = mockMvc.perform(
                 patch("/api/payment-cards/{id}/deactivate", card.getId())
                     .with(adminJwt(user.getId())))
             .andExpect(status().isOk())
             .andReturn();

         PaymentCardDisplayDto response = objectMapper.readValue(
             result.getResponse().getContentAsString(),
             PaymentCardDisplayDto.class
         );

         assertThat(response.isActive()).isFalse();

         PaymentCard updated = paymentCardRepository.findById(card.getId()).orElseThrow();
         assertThat(updated.isActive()).isFalse();
     }

    @Test
    void shouldDeletePaymentCard() throws Exception {

        User user = createUser();
        PaymentCard card = createCard(user);

        mockMvc.perform(delete("/api/payment-cards/{id}", card.getId())
                .with(adminJwt(UUID.randomUUID())))
            .andExpect(status().isNoContent());

        assertThat(paymentCardRepository.findById(card.getId())).isEmpty();
    }

    @Test
    void shouldGetAllCards() throws Exception {

        User user = createUser();
        createCard(user);

        MvcResult result = mockMvc.perform(
                get("/api/payment-cards")
                    .with(adminJwt(UUID.randomUUID())))
            .andExpect(status().isOk())
            .andReturn();

        assertThat(result.getResponse().getContentAsString())
            .contains("content");
    }

     private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor userJwt(UUID id) {
         return jwt()
             .jwt(jwt -> jwt
                 .subject(id.toString())
                 .claim("sub", id.toString())
                 .claim("realm_access", Map.of("roles", List.of("USER"))))
             .authorities(new SimpleGrantedAuthority("ROLE_USER"));
     }
}