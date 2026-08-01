package com.dsavitskiy.userservice.controller;

import com.dsavitskiy.userservice.dto.PaymentCardCreateDto;
import com.dsavitskiy.userservice.dto.PaymentCardDisplayDto;
import com.dsavitskiy.userservice.service.PaymentCardService;
import com.dsavitskiy.userservice.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment-cards")
public class PaymentCardController {

    private final PaymentCardService paymentCardService;

    @PostMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<PaymentCardDisplayDto> createPaymentCard(
        @Valid @RequestBody PaymentCardCreateDto paymentCardCreateDto) {
        PaymentCardDisplayDto paymentCard =
            paymentCardService.createPaymentCard(paymentCardCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentCard);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardDisplayDto> getPaymentCardById(@PathVariable Long id) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();

        PaymentCardDisplayDto card = paymentCardService.getPaymentCardById(id);

        if (!SecurityUtil.isAdmin() && !card.getUserId().equals(currentUserId)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }

        return ResponseEntity.ok(card);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<PaymentCardDisplayDto> updatePaymentCard(
        @PathVariable Long id,
        @Valid @RequestBody PaymentCardCreateDto paymentCardCreateDto) {
        return ResponseEntity.ok(paymentCardService.updateCard(id, paymentCardCreateDto));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentCardDisplayDto>> getPaymentCardsByUserId(
        @PathVariable UUID userId) {

        UUID currentUserId = SecurityUtil.getCurrentUserId();

        if (!SecurityUtil.isAdmin() && !userId.equals(currentUserId)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }

        List<PaymentCardDisplayDto> paymentCards = paymentCardService.findAllCardsByUserId(userId);
        return ResponseEntity.ok(paymentCards);
    }

    @GetMapping("/user/{userId}/activeCards")
    public ResponseEntity<List<PaymentCardDisplayDto>> getActivePaymentCardsByUserId(
        @PathVariable UUID userId) {

        UUID currentUserId = SecurityUtil.getCurrentUserId();

        if (!SecurityUtil.isAdmin() && !userId.equals(currentUserId)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied");
        }

        return ResponseEntity.ok(paymentCardService.findActiveCardsByUserId(userId));
    }

    @GetMapping
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Page<PaymentCardDisplayDto>> getAllPaymentCards(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String surname,
        @PageableDefault(size = 10) Pageable pageable) {
        Page<PaymentCardDisplayDto> paymentCards =
            paymentCardService.findAllCards(name, surname, pageable);
        return ResponseEntity.ok(paymentCards);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<Void> deletePaymentCard(@PathVariable Long id) {
        paymentCardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<PaymentCardDisplayDto> activatePaymentCard(
        @PathVariable Long id){
        return ResponseEntity.ok(paymentCardService.activateCard(id));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<PaymentCardDisplayDto> deactivatePaymentCard(
        @PathVariable Long id){
        return ResponseEntity.ok(paymentCardService.deactivateCard(id));
    }
}