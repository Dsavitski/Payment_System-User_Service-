package com.dsavitskiy.userservice.service;

import com.dsavitskiy.userservice.dto.PaymentCardCreateDto;
import com.dsavitskiy.userservice.dto.PaymentCardDisplayDto;
import com.dsavitskiy.userservice.entity.PaymentCard;
import com.dsavitskiy.userservice.entity.User;
import com.dsavitskiy.userservice.exception.PaymentCardLimitException;
import com.dsavitskiy.userservice.exception.ResourceNotFoundException;
import com.dsavitskiy.userservice.mapper.PaymentCardMapper;
import com.dsavitskiy.userservice.repository.PaymentCardRepository;
import com.dsavitskiy.userservice.repository.UserRepository;
import com.dsavitskiy.userservice.specification.PaymentCardSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCardService {
    private static final String LOG_PAYMENT_CARD_NOT_FOUND = "Payment card {} not found";
    private static final String NO_SUCH_PAYMENT_CARD = "PaymentCard with such id not found!";

    private final PaymentCardMapper paymentCardMapper;
    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = {"payment_cards", "payment_card_by_id", "payment_cards_by_user_id", "payment_cards_active"}, allEntries = true)
    public PaymentCardDisplayDto createPaymentCard(PaymentCardCreateDto paymentCardCreateDto) {
        User user = userRepository.findById(paymentCardCreateDto.getUserId()).orElseThrow(() -> {
            log.info("User {} not found", paymentCardCreateDto.getUserId());
            return new ResourceNotFoundException("User with such id not found!");
        });
        long cardCount = paymentCardRepository.countByUserId(paymentCardCreateDto.getUserId());
        if (cardCount >= 5) {
            log.info("User {} exceeded payment card limit: {}", user.getId(), cardCount);
            throw new PaymentCardLimitException("Amount of cards exceeded (" + cardCount + ")");
        }
        PaymentCard paymentCard = paymentCardMapper.toEntity(paymentCardCreateDto);
        paymentCard.setUser(user);
        PaymentCard savedPaymentCard = paymentCardRepository.save(paymentCard);
        log.info("Payment card {} created for user {}", savedPaymentCard.getId(), user.getId());
        return paymentCardMapper.toDisplayDto(savedPaymentCard);
    }

    @Cacheable(value = "payment_card_by_id", key = "#id")
    @Transactional(readOnly = true)
    public PaymentCardDisplayDto getPaymentCardById(Long id) {
        PaymentCard paymentCard = paymentCardRepository.findById(id).orElseThrow(() -> {
            log.info(LOG_PAYMENT_CARD_NOT_FOUND, id);
            return new ResourceNotFoundException(NO_SUCH_PAYMENT_CARD);
        });
        checkAccess(paymentCard.getUser().getId());

        return paymentCardMapper.toDisplayDto(paymentCard);
    }

    @PreAuthorize("hasRole('ADMIN') or #userId.toString() == authentication.name")
    @Cacheable(value = "payment_cards_by_user_id", key = "#userId")
    @Transactional(readOnly = true)
    public List<PaymentCardDisplayDto> findAllCardsByUserId(UUID userId) {
        log.debug("Getting all payment cards for user {}", userId);
        return paymentCardRepository.findByUserId(userId).stream()
            .map(paymentCardMapper::toDisplayDto).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "payment_cards", key = "#name + '-' + #surname + '-' + #pageable")
    @Transactional(readOnly = true)
    public Page<PaymentCardDisplayDto> findAllCards(String name, String surname, Pageable pageable) {
        log.debug("Searching payment cards: name={}, surname={}, page={}", name, surname, pageable);
        return paymentCardRepository.findAll(
                PaymentCardSpecification.withFilters(name, surname), pageable)
            .map(paymentCardMapper::toDisplayDto);
    }

    @PreAuthorize("hasRole('ADMIN') or #userId.toString() == authentication.name")
    @Cacheable(value = "payment_cards_active", key = "#userId")
    @Transactional(readOnly = true)
    public List<PaymentCardDisplayDto> findActiveCardsByUserId(UUID userId) {
        log.debug("Getting active payment cards for user {}", userId);
        List<PaymentCard> activePaymentCards = paymentCardRepository.findActiveCardsByUserId(userId);
        return activePaymentCards.stream().map(paymentCardMapper::toDisplayDto).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @CacheEvict(value = {"payment_cards", "payment_card_by_id", "payment_cards_by_user_id", "payment_cards_active"}, allEntries = true)
    public PaymentCardDisplayDto updateCard(Long id, PaymentCardCreateDto paymentCardCreateDto) {
        log.info("Updating payment card {}", id);
        PaymentCard existingPaymentCard = paymentCardRepository.findById(id)
            .orElseThrow(() -> {
                log.info(LOG_PAYMENT_CARD_NOT_FOUND, id);
                return new ResourceNotFoundException(NO_SUCH_PAYMENT_CARD);
            });

        User user = userRepository.findById(paymentCardCreateDto.getUserId())
            .orElseThrow(() -> {
                log.info("User {} not found while updating payment card {}", paymentCardCreateDto.getUserId(), id);
                return new ResourceNotFoundException("User with such id not found!");
            });

        paymentCardMapper.updateEntity(paymentCardCreateDto, existingPaymentCard);
        existingPaymentCard.setUser(user);

        PaymentCard savedPaymentCard = paymentCardRepository.save(existingPaymentCard);
        log.info("Payment card {} updated", id);
        return paymentCardMapper.toDisplayDto(savedPaymentCard);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @CacheEvict(value = {"payment_cards", "payment_card_by_id", "payment_cards_by_user_id", "payment_cards_active"}, allEntries = true)
    public void deleteCard(Long id) {
        log.info("Deleting payment card {}", id);
        PaymentCard paymentCard = paymentCardRepository.findById(id).orElseThrow(() -> {
            log.info(LOG_PAYMENT_CARD_NOT_FOUND, id);
            return new ResourceNotFoundException(NO_SUCH_PAYMENT_CARD);
        });
        paymentCardRepository.delete(paymentCard);
        log.info("Payment card {} deleted", id);
    }

    @Transactional
    @CacheEvict(value = {"payment_cards", "payment_card_by_id", "payment_cards_by_user_id", "payment_cards_active"}, allEntries = true)
    public PaymentCardDisplayDto activateCard(Long id) {
        log.info("Activating payment card {}", id);
        PaymentCard paymentCard = paymentCardRepository.findById(id).orElseThrow(() -> {
            log.info(LOG_PAYMENT_CARD_NOT_FOUND, id);
            return new ResourceNotFoundException(NO_SUCH_PAYMENT_CARD);
        });

        checkAccess(paymentCard.getUser().getId());

        paymentCard.setActive(true);
        PaymentCard saved = paymentCardRepository.save(paymentCard);
        log.info("Payment card {} activated", id);
        return paymentCardMapper.toDisplayDto(saved);
    }

    @Transactional
    @CacheEvict(value = {"payment_cards", "payment_card_by_id", "payment_cards_by_user_id", "payment_cards_active"}, allEntries = true)
    public PaymentCardDisplayDto deactivateCard(Long id) {
        log.info("Deactivating payment card {}", id);
        PaymentCard paymentCard = paymentCardRepository.findById(id).orElseThrow(() -> {
            log.info(LOG_PAYMENT_CARD_NOT_FOUND, id);
            return new ResourceNotFoundException(NO_SUCH_PAYMENT_CARD);
        });
        checkAccess(paymentCard.getUser().getId());
        paymentCard.setActive(false);
        PaymentCard saved = paymentCardRepository.save(paymentCard);
        log.info("Payment card {} deactivated", id);
        return paymentCardMapper.toDisplayDto(saved);
    }

    private void checkAccess(UUID resourceOwnerId) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("Not authenticated");
        }
        boolean isAdmin = auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN"));
        if (!isAdmin && !resourceOwnerId.toString().equals(auth.getName())) {
            log.warn("Access denied for user {} to resource owned by {}", auth.getName(), resourceOwnerId);
            throw new AccessDeniedException("Доступ запрещен: вы не являетесь владельцем этого ресурса");
        }
    }
}