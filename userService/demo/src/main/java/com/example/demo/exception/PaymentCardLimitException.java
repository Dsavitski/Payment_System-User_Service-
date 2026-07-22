package com.example.demo.exception;

public class PaymentCardLimitException extends RuntimeException {
    public PaymentCardLimitException(String message) {
                super(message);
    }
    public PaymentCardLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
