package com.dsavitskiy.userservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UserDisplayDto implements Serializable {
    private UUID id;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PaymentCardDisplayDto> paymentCards;
}
