package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PaymentCardCreateDto {
    @NotNull(message = "Enter user's id")
    private Long userId;
    @NotBlank(message = "Enter card number")
    @Size(min = 16, max = 16, message = "Card must contain 16 digits")
    @Pattern(regexp = "\\d+", message = "Enter only numbers")
    private String number;
    @NotBlank(message = "Enter holder name")
    private String holder;
    @Future(message = "Date must be in future")
    private LocalDate expirationDate;

}
