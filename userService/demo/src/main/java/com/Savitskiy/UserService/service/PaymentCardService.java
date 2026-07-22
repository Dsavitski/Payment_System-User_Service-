package com.Savitskiy.UserService.service;

import com.Savitskiy.UserService.dto.PaymentCardCreateDto;
import com.Savitskiy.UserService.dto.PaymentCardDisplayDto;
import com.Savitskiy.UserService.entity.PaymentCard;
import com.Savitskiy.UserService.entity.User;
import com.Savitskiy.UserService.exception.PaymentCardLimitException;
import com.Savitskiy.UserService.exception.ResourceNotFoundException;
import com.Savitskiy.UserService.mapper.PaymentCardMapper;
import com.Savitskiy.UserService.repository.PaymentCardRepository;
import com.Savitskiy.UserService.repository.UserRepository;
import com.Savitskiy.UserService.specification.PaymentCardSpecification;
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
            ()-> new ResourceNotFoundException("PaymentCard with such id not found!"));
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
        PaymentCard existingPaymentCard = paymentCardRepository.findById(id).orElseThrow(
            ()-> new ResourceNotFoundException("PaymentCard with such id not found!"));
        User user = userRepository.findById(paymentCardCreateDto.getUserId()).orElseThrow(
            ()-> new ResourceNotFoundException("User with such id not found!"));
        existingPaymentCard.setUser(user);
        existingPaymentCard.setNumber(paymentCardCreateDto.getNumber());
        existingPaymentCard.setHolder(paymentCardCreateDto.getHolder());
        existingPaymentCard.setExpirationDate(paymentCardCreateDto.getExpirationDate());
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
            ()-> new ResourceNotFoundException("PaymentCard with such id not found!"));
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
            ()->new ResourceNotFoundException("PaymentCard with such id not found!"));
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
            ()-> new ResourceNotFoundException("PaymentCard with such id not found!"));
        paymentCard.setActive(false);
        return paymentCardMapper.toDisplayDto(paymentCard);
    }

}
