package com.dsavitskiy.userservice.security;

import com.dsavitskiy.userservice.repository.PaymentCardRepository;
import com.dsavitskiy.userservice.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("paymentCardSecurityService")
@RequiredArgsConstructor
public class PaymentCardSecurityService {

    private final PaymentCardRepository paymentCardRepository;

    public boolean isOwner(Long cardId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        return paymentCardRepository.findById(cardId)
            .map(card -> card.getUser().getId().equals(currentUserId))
            .orElse(false);
    }
}