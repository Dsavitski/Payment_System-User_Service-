package com.dsavitskiy.userservice.exception;

public class PaymentCardLimitException extends RuntimeException {
    public PaymentCardLimitException(String message) {
                super(message);
    }
}
