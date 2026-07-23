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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentCardServiceTest {

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private PaymentCardMapper paymentCardMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaymentCardService paymentCardService;

    private User user;
    private PaymentCard paymentCard;
    private PaymentCardCreateDto createDto;
    private PaymentCardDisplayDto displayDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Alex");
        user.setSurname("Smith");

        paymentCard = new PaymentCard();
        paymentCard.setId(1L);
        paymentCard.setNumber("1111222233334444");
        paymentCard.setHolder("Alex Smith");
        paymentCard.setExpirationDate(LocalDate.of(2030, Month.JANUARY, 1));
        paymentCard.setActive(true);
        paymentCard.setUser(user);

        createDto = new PaymentCardCreateDto();
        createDto.setUserId(1L);
        createDto.setNumber("1111222233334444");
        createDto.setHolder("Alex Smith");
        createDto.setExpirationDate(LocalDate.of(2030, Month.JANUARY, 1));

        displayDto = new PaymentCardDisplayDto();
        displayDto.setId(1L);
        displayDto.setNumber("1111222233334444");
        displayDto.setHolder("Alex Smith");
    }

    @Test
    void createPaymentCard_shouldCreateCard() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countByUserId(1L)).thenReturn(2L);
        when(paymentCardMapper.toEntity(createDto)).thenReturn(paymentCard);
        when(paymentCardRepository.save(paymentCard)).thenReturn(paymentCard);
        when(paymentCardMapper.toDisplayDto(paymentCard)).thenReturn(displayDto);

        PaymentCardDisplayDto result = paymentCardService.createPaymentCard(createDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(paymentCardRepository).save(paymentCard);
    }

    @Test
    void createPaymentCard_shouldThrowWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.createPaymentCard(createDto));

        verify(paymentCardRepository, never()).save(any());
    }

    @Test
    void createPaymentCard_shouldThrowWhenLimitExceeded() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countByUserId(1L)).thenReturn(5L);

        assertThrows(PaymentCardLimitException.class, () -> paymentCardService.createPaymentCard(createDto));

        verify(paymentCardRepository, never()).save(any());
    }

    @Test
    void getPaymentCardById_shouldReturnCard() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(paymentCard));
        when(paymentCardMapper.toDisplayDto(paymentCard)).thenReturn(displayDto);

        PaymentCardDisplayDto result = paymentCardService.getPaymentCardById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getPaymentCardById_shouldThrowException() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.getPaymentCardById(1L));
    }

    @Test
    void findAllCardsByUserId_shouldReturnCards() {
        when(paymentCardRepository.findByUserId(1L)).thenReturn(List.of(paymentCard));
        when(paymentCardMapper.toDisplayDto(paymentCard)).thenReturn(displayDto);

        List<PaymentCardDisplayDto> result = paymentCardService.findAllCardsByUserId(1L);

        assertEquals(1, result.size());
    }

    @Test
    void findAllCards_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0,10);
        Page<PaymentCard> page = new PageImpl<>(List.of(paymentCard));

        when(paymentCardRepository.findAll(
            ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<PaymentCard>>any(),
            eq(pageable)))
            .thenReturn(page);

        when(paymentCardMapper.toDisplayDto(paymentCard)).thenReturn(displayDto);

        Page<PaymentCardDisplayDto> result =
            paymentCardService.findAllCards("Alex","Smith",pageable);

        assertEquals(1,result.getTotalElements());
    }

    @Test
    void findActiveCardsByUserId_shouldReturnCards() {
        when(paymentCardRepository.findActiveCardsByUserId(1L)).thenReturn(List.of(paymentCard));
        when(paymentCardMapper.toDisplayDto(paymentCard)).thenReturn(displayDto);

        List<PaymentCardDisplayDto> result = paymentCardService.findActiveCardsByUserId(1L);

        assertEquals(1, result.size());
    }

    @Test
    void updateCard_shouldUpdateCard() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(paymentCard));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(paymentCardRepository.save(paymentCard)).thenReturn(paymentCard);
        when(paymentCardMapper.toDisplayDto(paymentCard)).thenReturn(displayDto);

        PaymentCardDisplayDto result = paymentCardService.updateCard(1L, createDto);

        assertNotNull(result);
        assertEquals("1111222233334444", paymentCard.getNumber());
        verify(paymentCardRepository).save(paymentCard);
    }

    @Test
    void updateCard_shouldThrowWhenCardNotFound() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.updateCard(1L, createDto));

        verify(paymentCardRepository, never()).save(any());
    }

    @Test
    void updateCard_shouldThrowWhenUserNotFound() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(paymentCard));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.updateCard(1L, createDto));

        verify(paymentCardRepository, never()).save(any());
    }

    @Test
    void deleteCard_shouldDeleteCard() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(paymentCard));

        paymentCardService.deleteCard(1L);

        verify(paymentCardRepository).delete(paymentCard);
    }

    @Test
    void deleteCard_shouldThrowException() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.deleteCard(1L));

        verify(paymentCardRepository, never()).delete(any(PaymentCard.class));
    }

    @Test
    void activateCard_shouldActivateCard() {
        paymentCard.setActive(false);

        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(paymentCard));
        when(paymentCardMapper.toDisplayDto(paymentCard)).thenReturn(displayDto);

        PaymentCardDisplayDto result = paymentCardService.activateCard(1L);

        assertTrue(paymentCard.isActive());
        assertNotNull(result);
    }

    @Test
    void activateCard_shouldThrowException() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.activateCard(1L));
    }

    @Test
    void deactivateCard_shouldDeactivateCard() {
        paymentCard.setActive(true);

        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(paymentCard));
        when(paymentCardMapper.toDisplayDto(paymentCard)).thenReturn(displayDto);

        PaymentCardDisplayDto result = paymentCardService.deactivateCard(1L);

        assertFalse(paymentCard.isActive());
        assertNotNull(result);
    }

    @Test
    void deactivateCard_shouldThrowException() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.deactivateCard(1L));
    }
}