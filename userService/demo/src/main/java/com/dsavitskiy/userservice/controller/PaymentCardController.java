package com.dsavitskiy.userservice.controller;

import com.dsavitskiy.userservice.dto.PaymentCardCreateDto;
import com.dsavitskiy.userservice.dto.PaymentCardDisplayDto;
import com.dsavitskiy.userservice.service.PaymentCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment-cards")
public class PaymentCardController {

    private final PaymentCardService paymentCardService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or #paymentCardCreateDto.userId.toString() == authentication.name")
    public ResponseEntity<PaymentCardDisplayDto> createPaymentCard(
        @Valid @RequestBody PaymentCardCreateDto paymentCardCreateDto) {
        PaymentCardDisplayDto paymentCard =
            paymentCardService.createPaymentCard(paymentCardCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentCard);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @paymentCardSecurityService.isOwner(#id)")
    public ResponseEntity<PaymentCardDisplayDto> getPaymentCardById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentCardService.getPaymentCardById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @paymentCardSecurityService.isOwner(#id)")
    public ResponseEntity<PaymentCardDisplayDto> updatePaymentCard(
        @PathVariable Long id,
        @Valid @RequestBody PaymentCardCreateDto paymentCardCreateDto) {
        return ResponseEntity.ok(paymentCardService.updateCard(id, paymentCardCreateDto));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId.toString() == authentication.name")
    public ResponseEntity<List<PaymentCardDisplayDto>> getPaymentCardsByUserId(
        @PathVariable UUID userId) {
        return ResponseEntity.ok(paymentCardService.findAllCardsByUserId(userId));
    }

    @GetMapping("/user/{userId}/activeCards")
    @PreAuthorize("hasRole('ADMIN') or #userId.toString() == authentication.name")
    public ResponseEntity<List<PaymentCardDisplayDto>> getActivePaymentCardsByUserId(
        @PathVariable UUID userId) {
        return ResponseEntity.ok(paymentCardService.findActiveCardsByUserId(userId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PaymentCardDisplayDto>> getAllPaymentCards(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String surname,
        @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(paymentCardService.findAllCards(name, surname, pageable));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePaymentCard(@PathVariable Long id) {
        paymentCardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN') or @paymentCardSecurityService.isOwner(#id)")
    public ResponseEntity<PaymentCardDisplayDto> activatePaymentCard(@PathVariable Long id) {
        return ResponseEntity.ok(paymentCardService.activateCard(id));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN') or @paymentCardSecurityService.isOwner(#id)")
    public ResponseEntity<PaymentCardDisplayDto> deactivatePaymentCard(@PathVariable Long id) {
        return ResponseEntity.ok(paymentCardService.deactivateCard(id));
    }
}