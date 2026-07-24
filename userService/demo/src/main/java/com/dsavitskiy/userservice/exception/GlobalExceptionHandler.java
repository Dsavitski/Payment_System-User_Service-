package com.dsavitskiy.userservice.exception;

import com.dsavitskiy.userservice.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    private static final String MINSK_TIME_ZONE = "Europe/Minsk";


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> resourceNotFoundException(
            ResourceNotFoundException ex) {
        log.error(ex.getMessage());
        ErrorResponseDto response = new ErrorResponseDto(
            LocalDateTime.now(ZoneId.of(MINSK_TIME_ZONE)),
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(PaymentCardLimitException.class)
    public ResponseEntity<ErrorResponseDto> paymentCardLimitException(
        PaymentCardLimitException ex){

        log.error(ex.getMessage());
        ErrorResponseDto response = new ErrorResponseDto(
            LocalDateTime.now(ZoneId.of(MINSK_TIME_ZONE)),
            HttpStatus.CONFLICT.value(),
            HttpStatus.CONFLICT.getReasonPhrase(),
            ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> methodArgumentNotValidException(
        MethodArgumentNotValidException ex){
        String message = Objects.requireNonNull(
            ex.getBindingResult().getFieldError()).getDefaultMessage();
        ErrorResponseDto response = new ErrorResponseDto(
            LocalDateTime.now(ZoneId.of(MINSK_TIME_ZONE)),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> exception(Exception ex){
        log.error("Unexpected error ", ex);
        ErrorResponseDto response = new ErrorResponseDto(
            LocalDateTime.now(ZoneId.of(MINSK_TIME_ZONE)),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            "Internal Server Error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
