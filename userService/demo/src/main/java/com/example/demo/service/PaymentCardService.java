package com.example.demo.service;

import com.example.demo.dto.PaymentCardCreateDto;
import com.example.demo.dto.PaymentCardDisplayDto;
import com.example.demo.entity.PaymentCard;
import com.example.demo.entity.User;
import com.example.demo.exception.PaymentCardLimitException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.PaymentCardMapper;
import com.example.demo.repository.PaymentCardRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.specification.PaymentCardSpecification;
import lombok.RequiredArgsConstructor;
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

    public PaymentCardDisplayDto getPaymentCardById(Long id) {
        PaymentCard paymentCard = paymentCardRepository.findById(id).orElseThrow(
            ()-> new ResourceNotFoundException("PaymentCard with such id not found!"));
        return paymentCardMapper.toDisplayDto(paymentCard);
    }

    public List<PaymentCardDisplayDto> findAllCardsByUserId(Long userId) {
        return paymentCardRepository.findByUserId(userId).stream().map(
            paymentCardMapper::toDisplayDto).toList();
    }

    public Page<PaymentCardDisplayDto> findAllCards(String name,String surname, Pageable pageable) {
        return paymentCardRepository.findAll(
            PaymentCardSpecification.withFilters(name,surname),pageable)
            .map(paymentCardMapper::toDisplayDto);
    }
    public List<PaymentCardDisplayDto> findActiveCardsByUserId(Long userId) {
        List<PaymentCard> activePaymentCards = paymentCardRepository.findActiveCardsByUserId(userId);
        return activePaymentCards.stream().map(paymentCardMapper::toDisplayDto).toList();
    }

    @Transactional
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
    public void deleteCard(Long id) {
        PaymentCard paymentCard = paymentCardRepository.findById(id).orElseThrow(
            ()-> new ResourceNotFoundException("PaymentCard with such id not found!"));
        paymentCardRepository.delete(paymentCard);
    }

    @Transactional
    public PaymentCardDisplayDto activateCard(Long id) {
        PaymentCard paymentCard = paymentCardRepository.findById(id).orElseThrow(
            ()->new ResourceNotFoundException("PaymentCard with such id not found!"));
        paymentCard.setActive(true);
        return paymentCardMapper.toDisplayDto(paymentCard);
    }

    @Transactional
    public PaymentCardDisplayDto deactivateCard(Long id) {
        PaymentCard paymentCard = paymentCardRepository.findById(id).orElseThrow(
            ()-> new ResourceNotFoundException("PaymentCard with such id not found!"));
        paymentCard.setActive(false);
        return paymentCardMapper.toDisplayDto(paymentCard);
    }

}
