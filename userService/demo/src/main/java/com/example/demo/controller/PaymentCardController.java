package com.example.demo.controller;

import com.example.demo.dto.PaymentCardCreateDto;
import com.example.demo.dto.PaymentCardDisplayDto;
import com.example.demo.service.PaymentCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment-cards")
public class PaymentCardController {

    private final PaymentCardService paymentCardService;

    @PostMapping
    public ResponseEntity<PaymentCardDisplayDto> createPaymentCard(
        @Valid @RequestBody PaymentCardCreateDto paymentCardCreateDto) {
        PaymentCardDisplayDto paymentCard =
            paymentCardService.createPaymentCard(paymentCardCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentCard);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardDisplayDto> getPaymentCardById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentCardService.getPaymentCardById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentCardDisplayDto> updatePaymentCard(
        @PathVariable Long id,
        @Valid @RequestBody PaymentCardCreateDto paymentCardCreateDto) {
        return ResponseEntity.ok(paymentCardService.updateCard(id, paymentCardCreateDto));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentCardDisplayDto>> getPaymentCardsByUserId(
        @PathVariable Long userId) {
        List<PaymentCardDisplayDto> paymentCards = paymentCardService.findAllCardsByUserId(userId);
        return ResponseEntity.ok(paymentCards);
    }

    @GetMapping("/user/{userId}/activeCards")
    public ResponseEntity<List<PaymentCardDisplayDto>> getActivePaymentCardsByUserId(
        @PathVariable Long userId) {
        return ResponseEntity.ok(paymentCardService.findActiveCardsByUserId(userId));
    }

    @GetMapping
    public ResponseEntity<Page<PaymentCardDisplayDto>> getAllPaymentCards(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String surname,
        @PageableDefault(size = 10) Pageable pageable) {
        Page<PaymentCardDisplayDto> paymentCards =
            paymentCardService.findAllCards(name, surname, pageable);
        return ResponseEntity.ok(paymentCards);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>deletePaymentCard(@PathVariable Long id) {
        paymentCardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<PaymentCardDisplayDto> activatePaymentCard(
        @PathVariable Long id){
        return ResponseEntity.ok(paymentCardService.activateCard(id));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<PaymentCardDisplayDto> deactivatePaymentCard(
        @PathVariable Long id){
        return ResponseEntity.ok(paymentCardService.deactivateCard(id));
    }
}
