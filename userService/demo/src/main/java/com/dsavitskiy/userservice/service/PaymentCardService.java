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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentCardService {
    private static final String N0_SUCH_PAYMENT_CARD = "PaymentCard with such id not found!";
    private final PaymentCardMapper paymentCardMapper;
    private final PaymentCardRepository paymentCardRepository;
    private final UserRepository userRepository;

    @CacheEvict(
        value = {
            "payment_cards",
            "payment_card_by_id",
            "payment_cards_by_user_id",
            "payment_cards_active"
        },
        allEntries = true
    )
    public PaymentCardDisplayDto createPaymentCard(PaymentCardCreateDto paymentCardCreateDto) {
        User user = userRepository.findById(paymentCardCreateDto.getUserId()).orElseThrow(
            ()->new ResourceNotFoundException("User with such id not found!"));
        long cardCount = paymentCardRepository.countByUserId(paymentCardCreateDto.getUserId());
        if (cardCount >= 5) {
            throw new PaymentCardLimitException("Amount of cards exceeded (" + cardCount + ")");
        }
        PaymentCard paymentCard = paymentCardMapper.toEntity(paymentCardCreateDto);
        paymentCard.setUser(user);
        PaymentCard savedPaymentCard = paymentCardRepository.save(paymentCard);
        return paymentCardMapper.toDisplayDto(savedPaymentCard);
    }

    @Cacheable(value = "payment_card_by_id",key = "#id")
    public PaymentCardDisplayDto getPaymentCardById(Long id) {
        PaymentCard paymentCard = paymentCardRepository.findById(id).orElseThrow(
            ()-> new ResourceNotFoundException(N0_SUCH_PAYMENT_CARD));
        return paymentCardMapper.toDisplayDto(paymentCard);
    }
    @Cacheable(value = "payment_cards_by_user_id",key = "#userId")
    public List<PaymentCardDisplayDto> findAllCardsByUserId(Long userId) {
        return paymentCardRepository.findByUserId(userId).stream().map(
            paymentCardMapper::toDisplayDto).toList();
    }
    @Cacheable(value = "payment_cards",
        key = "#name + '-' + #surname + '-' + #pageable")
    public Page<PaymentCardDisplayDto> findAllCards(String name,String surname, Pageable pageable) {
        return paymentCardRepository.findAll(
            PaymentCardSpecification.withFilters(name,surname),pageable)
            .map(paymentCardMapper::toDisplayDto);
    }
    @Cacheable(value = "payment_cards_active",key = "#userId")
    public List<PaymentCardDisplayDto> findActiveCardsByUserId(Long userId) {
        List<PaymentCard> activePaymentCards = paymentCardRepository.findActiveCardsByUserId(userId);
        return activePaymentCards.stream().map(paymentCardMapper::toDisplayDto).toList();
    }

    @Transactional
    @CacheEvict(
        value = {
            "payment_cards",
            "payment_card_by_id",
            "payment_cards_by_user_id",
            "payment_cards_active"
        },
        allEntries = true
    )
    public PaymentCardDisplayDto updateCard(Long id, PaymentCardCreateDto paymentCardCreateDto) {
        PaymentCard existingPaymentCard = paymentCardRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(N0_SUCH_PAYMENT_CARD));

        User user = userRepository.findById(paymentCardCreateDto.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User with such id not found!"));

        paymentCardMapper.updateEntity(paymentCardCreateDto, existingPaymentCard);
        existingPaymentCard.setUser(user);

        PaymentCard savedPaymentCard = paymentCardRepository.save(existingPaymentCard);
        return paymentCardMapper.toDisplayDto(savedPaymentCard);
    }

    @Transactional
    @CacheEvict(
        value = {
            "payment_cards",
            "payment_card_by_id",
            "payment_cards_by_user_id",
            "payment_cards_active"
        },
        allEntries = true
    )
    public void deleteCard(Long id) {
        PaymentCard paymentCard = paymentCardRepository.findById(id).orElseThrow(
            ()-> new ResourceNotFoundException(N0_SUCH_PAYMENT_CARD));
        paymentCardRepository.delete(paymentCard);
    }

    @Transactional
    @CacheEvict(
        value = {
            "payment_cards",
            "payment_card_by_id",
            "payment_cards_by_user_id",
            "payment_cards_active"
        },
        allEntries = true
    )
    public PaymentCardDisplayDto activateCard(Long id) {
        PaymentCard paymentCard = paymentCardRepository.findById(id).orElseThrow(
            ()->new ResourceNotFoundException(N0_SUCH_PAYMENT_CARD));
        paymentCard.setActive(true);
        return paymentCardMapper.toDisplayDto(paymentCard);
    }

    @Transactional
    @CacheEvict(
        value = {
            "payment_cards",
            "payment_card_by_id",
            "payment_cards_by_user_id",
            "payment_cards_active"
        },
        allEntries = true
    )
    public PaymentCardDisplayDto deactivateCard(Long id) {
        PaymentCard paymentCard = paymentCardRepository.findById(id).orElseThrow(
            ()-> new ResourceNotFoundException(N0_SUCH_PAYMENT_CARD));
        paymentCard.setActive(false);
        return paymentCardMapper.toDisplayDto(paymentCard);
    }

}
