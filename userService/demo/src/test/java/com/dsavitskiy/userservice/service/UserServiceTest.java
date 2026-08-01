package com.dsavitskiy.userservice.service;

import com.dsavitskiy.userservice.dto.UserCreateDto;
import com.dsavitskiy.userservice.dto.UserDisplayDto;
import com.dsavitskiy.userservice.entity.User;
import com.dsavitskiy.userservice.exception.ResourceNotFoundException;
import com.dsavitskiy.userservice.mapper.UserMapper;
import com.dsavitskiy.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserCreateDto createDto;
    private UserDisplayDto displayDto;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setName("Alex");
        user.setSurname("Smith");
        user.setEmail("alex@test.com");
        user.setBirthDate(LocalDate.of(2000, Month.JANUARY, 1));
        user.setActive(true);

        createDto = new UserCreateDto();
        createDto.setId(userId);
        createDto.setName("Alex");
        createDto.setSurname("Smith");
        createDto.setEmail("alex@test.com");
        createDto.setBirthDate(LocalDate.of(2000, Month.JANUARY, 1));

        displayDto = new UserDisplayDto();
        displayDto.setId(userId);
        displayDto.setName("Alex");
        displayDto.setSurname("Smith");
    }

    @Test
    void createUser_shouldCreateUser() {
        when(userMapper.toEntity(createDto))
            .thenReturn(user);
        when(userRepository.save(user))
            .thenReturn(user);
        when(userMapper.toDisplayDto(user))
            .thenReturn(displayDto);

        UserDisplayDto result = userService.createUser(createDto);

        assertNotNull(result);
        assertInstanceOf(UUID.class, result.getId());
        assertEquals(userId.toString(), result.getId().toString());
        assertEquals("Alex", result.getName());

        verify(userMapper).toEntity(createDto);
        verify(userRepository).save(user);
        verify(userMapper).toDisplayDto(user);
    }

    @Test
    void findUserById_shouldReturnUser() {
        when(userRepository.findUserWithPaymentCardsById(userId))
            .thenReturn(Optional.of(user));
        when(userMapper.toDisplayDto(user))
            .thenReturn(displayDto);

        UserDisplayDto result = userService.findUserById(userId);

        assertNotNull(result);
        assertInstanceOf(UUID.class, result.getId());
        assertEquals(userId.toString(), result.getId().toString());

        verify(userRepository).findUserWithPaymentCardsById(userId);
        verify(userMapper).toDisplayDto(user);
    }

    @Test
    void findUserById_shouldThrowResourceNotFoundException() {
        when(userRepository.findUserWithPaymentCardsById(userId))
            .thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> userService.findUserById(userId)
        );

        verify(userRepository).findUserWithPaymentCardsById(userId);
        verify(userMapper, never()).toDisplayDto(any());
    }

    @Test
    void updateUser_shouldUpdateUser() {
        when(userRepository.findUserWithPaymentCardsById(userId))
            .thenReturn(Optional.of(user));
        when(userRepository.save(user))
            .thenReturn(user);
        when(userMapper.toDisplayDto(user))
            .thenReturn(displayDto);

        UserDisplayDto result = userService.updateUser(userId, createDto);

        assertNotNull(result);
        assertEquals("Alex", user.getName());
        assertEquals("Smith", user.getSurname());
        assertEquals("alex@test.com", user.getEmail());
        assertEquals(LocalDate.of(2000, Month.JANUARY, 1), user.getBirthDate());

        verify(userRepository).findUserWithPaymentCardsById(userId);
        verify(userRepository).save(user);
        verify(userMapper).toDisplayDto(user);
    }

    @Test
    void updateUser_shouldThrowResourceNotFoundException() {
        when(userRepository.findUserWithPaymentCardsById(userId))
            .thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> userService.updateUser(userId, createDto)
        );

        verify(userRepository).findUserWithPaymentCardsById(userId);
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toDisplayDto(any());
    }

    @Test
    void deleteUser_shouldDeleteUser() {
        when(userRepository.findById(userId))
            .thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userRepository).findById(userId);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_shouldThrowResourceNotFoundException() {
        when(userRepository.findById(userId))
            .thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> userService.deleteUser(userId)
        );

        verify(userRepository, never()).delete(any());
    }

    @Test
    void activateUser_shouldActivateUser() {
        user.setActive(false);

        when(userRepository.findUserWithPaymentCardsById(userId))
            .thenReturn(Optional.of(user));
        when(userRepository.save(user))
            .thenReturn(user);
        when(userMapper.toDisplayDto(user))
            .thenReturn(displayDto);

        UserDisplayDto result = userService.activateUser(userId);

        assertNotNull(result);
        assertTrue(user.isActive());

        verify(userRepository).findUserWithPaymentCardsById(userId);
        verify(userRepository).save(user);
        verify(userMapper).toDisplayDto(user);
    }

    @Test
    void activateUser_shouldThrowResourceNotFoundException() {
        when(userRepository.findUserWithPaymentCardsById(userId))
            .thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> userService.activateUser(userId)
        );

        verify(userRepository).findUserWithPaymentCardsById(userId);
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toDisplayDto(any());
    }

    @Test
    void deactivateUser_shouldDeactivateUser() {
        user.setActive(true);

        when(userRepository.findUserWithPaymentCardsById(userId))
            .thenReturn(Optional.of(user));
        when(userRepository.save(user))
            .thenReturn(user);
        when(userMapper.toDisplayDto(user))
            .thenReturn(displayDto);

        UserDisplayDto result = userService.deactivateUser(userId);

        assertNotNull(result);
        assertFalse(user.isActive());

        verify(userRepository).findUserWithPaymentCardsById(userId);
        verify(userRepository).save(user);
        verify(userMapper).toDisplayDto(user);
    }

    @Test
    void deactivateUser_shouldThrowResourceNotFoundException() {
        when(userRepository.findUserWithPaymentCardsById(userId))
            .thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> userService.deactivateUser(userId)
        );

        verify(userRepository).findUserWithPaymentCardsById(userId);
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toDisplayDto(any());
    }
}