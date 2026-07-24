package com.dsavitskiy.userservice.controller;

import org.springframework.test.web.servlet.MockMvc;
import com.dsavitskiy.userservice.AbstractIntegrationTest;
import com.dsavitskiy.userservice.entity.User;
import com.dsavitskiy.userservice.repository.UserRepository;
import com.dsavitskiy.userservice.repository.PaymentCardRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import java.time.LocalDate;
import java.time.Month;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@AutoConfigureMockMvc
class PaymentCardControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @Test
    void shouldCreatePaymentCard() throws Exception {
        User user = new User();
        user.setName("Alex");
        user.setSurname("Smith");
        user.setBirthDate(
            LocalDate.of(1995, Month.JANUARY,1)
        );
        user.setEmail(
            "card@test.com"
        );
        user.setActive(true);
        User savedUser =
            userRepository.save(user);
        String json =
            """
            {
              "userId":%d,
              "number":"1234567890123456",
              "holder":"Alex Smith",
              "expirationDate":"2030-01-01"
            }
            """.formatted(savedUser.getId());
        mockMvc.perform(
                post("/payment-cards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
            )
            .andExpect(status().isCreated())
            .andExpect(
                jsonPath("$.number")
                    .value("1234567890123456")
            );
        assert paymentCardRepository
            .count() == 1;
    }
}