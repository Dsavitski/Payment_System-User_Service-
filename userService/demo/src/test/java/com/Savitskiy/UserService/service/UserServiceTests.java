package com.Savitskiy.UserService.service;

import com.Savitskiy.UserService.dto.UserCreateDto;
import com.Savitskiy.UserService.dto.UserDisplayDto;
import com.Savitskiy.UserService.entity.User;
import com.Savitskiy.UserService.exception.ResourceNotFoundException;
import com.Savitskiy.UserService.mapper.UserMapper;
import com.Savitskiy.UserService.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
        user.setBirthDate(LocalDate.of(2000,1,1));
        user.setActive(true);
        createDto = new UserCreateDto();
        createDto.setName("Alex");
        createDto.setSurname("Smith");
        createDto.setEmail("alex@test.com");
        createDto.setBirthDate(LocalDate.of(2000,1,1));
        displayDto = new UserDisplayDto();
        displayDto.setId(1L);
        displayDto.setName("Alex");
        displayDto.setSurname("Smith");
    }

    @Test
    void createUser_shouldCreateUser() {
        when(userMapper.toEntity(createDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDisplayDto(user)).thenReturn(displayDto);
        UserDisplayDto result = userService.createUser(createDto);
        assertNotNull(result);
        assertEquals("Alex", result.getName());
        verify(userMapper).toEntity(createDto);
        verify(userRepository).save(user);
        verify(userMapper).toDisplayDto(user);
    }

    @Test
    void findUserById_shouldReturnUser() {
        when(userRepository.findUserWithPaymentCardsById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDisplayDto(user)).thenReturn(displayDto);
        UserDisplayDto result = userService.findUserById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository).findUserWithPaymentCardsById(1L);
    }

    @Test
    void findUserById_shouldThrowException() {
        when(userRepository.findUserWithPaymentCardsById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.findUserById(1L));
        verify(userMapper, never()).toDisplayDto(any());
    }

    @Test
    void updateUser_shouldUpdateUser() {
        when(userRepository.findUserWithPaymentCardsById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDisplayDto(user)).thenReturn(displayDto);
        UserDisplayDto result = userService.updateUser(1L, createDto);
        assertNotNull(result);
        assertEquals("Alex", user.getName());
        assertEquals("Smith", user.getSurname());
        assertEquals("alex@test.com", user.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_shouldThrowException() {
        when(userRepository.findUserWithPaymentCardsById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(1L, createDto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_shouldDeleteUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        userService.deleteUser(1L);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_shouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(1L));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void activateUser_shouldActivateUser() {
        user.setActive(false);
        when(userRepository.findUserWithPaymentCardsById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDisplayDto(user)).thenReturn(displayDto);
        UserDisplayDto result = userService.activateUser(1L);
        assertTrue(user.isActive());
        assertNotNull(result);
        verify(userRepository).save(user);
    }

    @Test
    void activateUser_shouldThrowException() {
        when(userRepository.findUserWithPaymentCardsById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.activateUser(1L));
        verify(userRepository, never()).save(any());
    }

    @Test
    void deactivateUser_shouldDeactivateUser() {
        user.setActive(true);
        when(userRepository.findUserWithPaymentCardsById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDisplayDto(user)).thenReturn(displayDto);
        UserDisplayDto result = userService.deactivateUser(1L);
        assertFalse(user.isActive());
        assertNotNull(result);
        verify(userRepository).save(user);
    }

    @Test
    void deactivateUser_shouldThrowException() {
        when(userRepository.findUserWithPaymentCardsById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.deactivateUser(1L));
        verify(userRepository, never()).save(any());
    }
}