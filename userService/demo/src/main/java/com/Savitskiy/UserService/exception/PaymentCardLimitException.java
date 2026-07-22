package com.Savitskiy.UserService.exception;

public class PaymentCardLimitException extends RuntimeException {
    public PaymentCardLimitException(String message) {
                super(message);
    }
}
