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

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Alex");
        user.setSurname("Smith");
        user.setEmail("alex@test.com");
        user.setBirthDate(LocalDate.of(2000, Month.JANUARY, 1));
        user.setActive(true);

        createDto = new UserCreateDto();
        createDto.setName("Alex");
        createDto.setSurname("Smith");
        createDto.setEmail("alex@test.com");
        createDto.setBirthDate(LocalDate.of(2000, Month.JANUARY, 1));

        displayDto = new UserDisplayDto();
        displayDto.setId(1L);
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
        assertEquals(1L, result.getId());
        assertEquals("Alex", result.getName());

        verify(userMapper).toEntity(createDto);
        verify(userRepository).save(user);
        verify(userMapper).toDisplayDto(user);
    }

    @Test
    void findUserById_shouldReturnUser() {
        when(userRepository.findUserWithPaymentCardsById(1L))
            .thenReturn(Optional.of(user));
        when(userMapper.toDisplayDto(user))
            .thenReturn(displayDto);

        UserDisplayDto result = userService.findUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        verify(userRepository).findUserWithPaymentCardsById(1L);
        verify(userMapper).toDisplayDto(user);
    }

    @Test
    void findUserById_shouldThrowResourceNotFoundException() {
        when(userRepository.findUserWithPaymentCardsById(1L))
            .thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> userService.findUserById(1L)
        );

        verify(userRepository).findUserWithPaymentCardsById(1L);
        verify(userMapper, never()).toDisplayDto(any());
    }

    @Test
    void updateUser_shouldUpdateUser() {
        when(userRepository.findUserWithPaymentCardsById(1L))
            .thenReturn(Optional.of(user));
        when(userRepository.save(user))
            .thenReturn(user);
        when(userMapper.toDisplayDto(user))
            .thenReturn(displayDto);

        UserDisplayDto result = userService.updateUser(1L, createDto);

        assertNotNull(result);
        assertEquals("Alex", user.getName());
        assertEquals("Smith", user.getSurname());
        assertEquals("alex@test.com", user.getEmail());
        assertEquals(LocalDate.of(2000, Month.JANUARY, 1), user.getBirthDate());

        verify(userRepository).findUserWithPaymentCardsById(1L);
        verify(userRepository).save(user);
        verify(userMapper).toDisplayDto(user);
    }

    @Test
    void updateUser_shouldThrowResourceNotFoundException() {
        when(userRepository.findUserWithPaymentCardsById(1L))
            .thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> userService.updateUser(1L, createDto)
        );

        verify(userRepository).findUserWithPaymentCardsById(1L);
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toDisplayDto(any());
    }

    @Test
    void deleteUser_shouldDeleteUser() {
        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).findById(1L);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_shouldThrowResourceNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(1L));

        verify(userRepository, never()).delete(any());
    }

    @Test
    void activateUser_shouldActivateUser() {
        user.setActive(false);

        when(userRepository.findUserWithPaymentCardsById(1L))
            .thenReturn(Optional.of(user));
        when(userRepository.save(user))
            .thenReturn(user);
        when(userMapper.toDisplayDto(user))
            .thenReturn(displayDto);

        UserDisplayDto result = userService.activateUser(1L);

        assertNotNull(result);
        assertTrue(user.isActive());

        verify(userRepository).findUserWithPaymentCardsById(1L);
        verify(userRepository).save(user);
        verify(userMapper).toDisplayDto(user);
    }

    @Test
    void activateUser_shouldThrowResourceNotFoundException() {
        when(userRepository.findUserWithPaymentCardsById(1L))
            .thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> userService.activateUser(1L)
        );

        verify(userRepository).findUserWithPaymentCardsById(1L);
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toDisplayDto(any());
    }

    @Test
    void deactivateUser_shouldDeactivateUser() {
        user.setActive(true);

        when(userRepository.findUserWithPaymentCardsById(1L))
            .thenReturn(Optional.of(user));
        when(userRepository.save(user))
            .thenReturn(user);
        when(userMapper.toDisplayDto(user))
            .thenReturn(displayDto);

        UserDisplayDto result = userService.deactivateUser(1L);

        assertNotNull(result);
        assertFalse(user.isActive());

        verify(userRepository).findUserWithPaymentCardsById(1L);
        verify(userRepository).save(user);
        verify(userMapper).toDisplayDto(user);
    }

    @Test
    void deactivateUser_shouldThrowResourceNotFoundException() {
        when(userRepository.findUserWithPaymentCardsById(1L))
            .thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> userService.deactivateUser(1L)
        );

        verify(userRepository).findUserWithPaymentCardsById(1L);
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toDisplayDto(any());
    }
}