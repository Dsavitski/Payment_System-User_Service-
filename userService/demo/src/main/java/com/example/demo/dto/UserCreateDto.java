package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserCreateDto {
    @NotBlank(message = "Enter name")
    private String name;
    @NotBlank(message = "Enter surname")
    private String surname;
    @NotNull(message = "Enter birth date")
    private LocalDate birthDate;
    @NotBlank(message = "Enter email")
    private String email;
}
