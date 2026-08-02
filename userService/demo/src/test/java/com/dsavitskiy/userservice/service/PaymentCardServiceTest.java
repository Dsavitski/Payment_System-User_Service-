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
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
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
        createDto.setUserId(userId);
        createDto.setNumber("1111222233334444");
        createDto.setHolder("Alex Smith");
        createDto.setExpirationDate(LocalDate.of(2030, Month.JANUARY, 1));

        displayDto = new PaymentCardDisplayDto();
        displayDto.setId(1L);
        displayDto.setUserId(userId);
        displayDto.setNumber("1111222233334444");
        displayDto.setHolder("Alex Smith");

        Authentication auth = new UsernamePasswordAuthenticationToken(
            userId.toString(),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createPaymentCard_shouldCreateCard() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countByUserId(userId)).thenReturn(2L);
        when(paymentCardMapper.toEntity(createDto)).thenReturn(paymentCard);
        when(paymentCardRepository.save(paymentCard)).thenReturn(paymentCard);
        when(paymentCardMapper.toDisplayDto(paymentCard)).thenReturn(displayDto);

        PaymentCardDisplayDto result = paymentCardService.createPaymentCard(createDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(userRepository).findById(userId);
        verify(paymentCardRepository).countByUserId(userId);
        verify(paymentCardMapper).toEntity(createDto);
        verify(paymentCardRepository).save(paymentCard);
        verify(paymentCardMapper).toDisplayDto(paymentCard);
    }

    @Test
    void createPaymentCard_shouldThrowWhenUserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.createPaymentCard(createDto));

        verify(userRepository).findById(userId);
        verify(paymentCardRepository, never()).countByUserId(any());
        verify(paymentCardMapper, never()).toEntity(any());
        verify(paymentCardRepository, never()).save(any());
    }

    @Test
    void createPaymentCard_shouldThrowWhenLimitExceeded() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(paymentCardRepository.countByUserId(userId)).thenReturn(5L);

        assertThrows(PaymentCardLimitException.class, () -> paymentCardService.createPaymentCard(createDto));

        verify(userRepository).findById(userId);
        verify(paymentCardRepository).countByUserId(userId);
        verify(paymentCardMapper, never()).toEntity(any());
        verify(paymentCardRepository, never()).save(any());
    }

    @Test
    void getPaymentCardById_shouldReturnCard() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(paymentCard));
        when(paymentCardMapper.toDisplayDto(paymentCard)).thenReturn(displayDto);

        PaymentCardDisplayDto result = paymentCardService.getPaymentCardById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(paymentCardRepository).findById(1L);
        verify(paymentCardMapper).toDisplayDto(paymentCard);
    }

    @Test
    void getPaymentCardById_shouldThrowException() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.getPaymentCardById(1L));

        verify(paymentCardRepository).findById(1L);
        verify(paymentCardMapper, never()).toDisplayDto(any());
    }

    @Test
    void findAllCardsByUserId_shouldReturnCards() {
        when(paymentCardRepository.findByUserId(userId)).thenReturn(List.of(paymentCard));
        when(paymentCardMapper.toDisplayDto(paymentCard)).thenReturn(displayDto);

        List<PaymentCardDisplayDto> result = paymentCardService.findAllCardsByUserId(userId);

        assertEquals(1, result.size());

        verify(paymentCardRepository).findByUserId(userId);
        verify(paymentCardMapper).toDisplayDto(paymentCard);
    }

    @Test
    void findAllCards_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PaymentCard> page = new PageImpl<>(List.of(paymentCard));

        when(paymentCardRepository.findAll(ArgumentMatchers.<Specification<PaymentCard>>any(), eq(pageable))).thenReturn(page);
        when(paymentCardMapper.toDisplayDto(paymentCard)).thenReturn(displayDto);

        Page<PaymentCardDisplayDto> result = paymentCardService.findAllCards("Alex", "Smith", pageable);

        assertEquals(1, result.getTotalElements());

        verify(paymentCardRepository).findAll(ArgumentMatchers.<Specification<PaymentCard>>any(), eq(pageable));
        verify(paymentCardMapper).toDisplayDto(paymentCard);
    }

    @Test
    void findActiveCardsByUserId_shouldReturnCards() {
        when(paymentCardRepository.findActiveCardsByUserId(userId)).thenReturn(List.of(paymentCard));
        when(paymentCardMapper.toDisplayDto(paymentCard)).thenReturn(displayDto);

        List<PaymentCardDisplayDto> result = paymentCardService.findActiveCardsByUserId(userId);

        assertEquals(1, result.size());

        verify(paymentCardRepository).findActiveCardsByUserId(userId);
        verify(paymentCardMapper).toDisplayDto(paymentCard);
    }

    @Test
    void updateCard_shouldUpdateCard() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(paymentCard));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(paymentCardRepository.save(paymentCard)).thenReturn(paymentCard);
        when(paymentCardMapper.toDisplayDto(paymentCard)).thenReturn(displayDto);

        PaymentCardDisplayDto result = paymentCardService.updateCard(1L, createDto);

        assertNotNull(result);

        verify(paymentCardRepository).findById(1L);
        verify(userRepository).findById(userId);
        verify(paymentCardMapper).updateEntity(createDto, paymentCard);
        verify(paymentCardRepository).save(paymentCard);
        verify(paymentCardMapper).toDisplayDto(paymentCard);
    }

    @Test
    void updateCard_shouldThrowWhenCardNotFound() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.updateCard(1L, createDto));

        verify(paymentCardRepository).findById(1L);
        verify(paymentCardRepository, never()).save(any());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void updateCard_shouldThrowWhenUserNotFound() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(paymentCard));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.updateCard(1L, createDto));

        verify(paymentCardRepository).findById(1L);
        verify(userRepository).findById(userId);
        verify(paymentCardRepository, never()).save(any());
        verify(paymentCardMapper, never()).toDisplayDto(any());
    }

    @Test
    void deleteCard_shouldDeleteCard() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(paymentCard));

        paymentCardService.deleteCard(1L);

        verify(paymentCardRepository).findById(1L);
        verify(paymentCardRepository).delete(paymentCard);
    }

    @Test
    void deleteCard_shouldThrowWhenCardNotFound() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.deleteCard(1L));

        verify(paymentCardRepository).findById(1L);
        verify(paymentCardRepository, never()).delete((PaymentCard) any());
    }

    @Test
    void activateCard_shouldActivateCard() {
        paymentCard.setActive(false);

        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(paymentCard));
        when(paymentCardMapper.toDisplayDto(paymentCard)).thenReturn(displayDto);

        PaymentCardDisplayDto result = paymentCardService.activateCard(1L);

        assertNotNull(result);
        assertTrue(paymentCard.isActive());

        verify(paymentCardRepository).findById(1L);
        verify(paymentCardMapper).toDisplayDto(paymentCard);
    }

    @Test
    void activateCard_shouldThrowWhenCardNotFound() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.activateCard(1L));

        verify(paymentCardRepository).findById(1L);
        verify(paymentCardMapper, never()).toDisplayDto(any());
    }

    @Test
    void deactivateCard_shouldDeactivateCard() {
        paymentCard.setActive(true);

        when(paymentCardRepository.findById(1L)).thenReturn(Optional.of(paymentCard));
        when(paymentCardMapper.toDisplayDto(paymentCard)).thenReturn(displayDto);

        PaymentCardDisplayDto result = paymentCardService.deactivateCard(1L);

        assertNotNull(result);
        assertFalse(paymentCard.isActive());

        verify(paymentCardRepository).findById(1L);
        verify(paymentCardMapper).toDisplayDto(paymentCard);
    }

    @Test
    void deactivateCard_shouldThrowWhenCardNotFound() {
        when(paymentCardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentCardService.deactivateCard(1L));

        verify(paymentCardRepository).findById(1L);
        verify(paymentCardMapper, never()).toDisplayDto(any());
    }
}