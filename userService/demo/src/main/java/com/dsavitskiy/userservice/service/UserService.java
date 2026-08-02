package com.dsavitskiy.userservice.service;

import com.dsavitskiy.userservice.dto.UserCreateDto;
import com.dsavitskiy.userservice.dto.UserDisplayDto;
import com.dsavitskiy.userservice.entity.User;
import com.dsavitskiy.userservice.exception.ResourceNotFoundException;
import com.dsavitskiy.userservice.mapper.UserMapper;
import com.dsavitskiy.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private static final String LOG_USER_NOT_FOUND = "User {} not found";
    private static final String NO_SUCH_USER = "User with such id not found!";

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public UserDisplayDto createUser(UserCreateDto userCreateDto) {
        log.info("Creating user with email {}", userCreateDto.getEmail());
        User user = userMapper.toEntity(userCreateDto);
        user.setId(UUID.randomUUID());
        user.setActive(true);
        User savedUser = userRepository.save(user);
        log.info("User {} created", savedUser.getId());
        return userMapper.toDisplayDto(savedUser);
    }

    @PreAuthorize("hasRole('ADMIN') or #id.toString() == authentication.name")
    @Cacheable(value = "users", key = "#id")
    @Transactional(readOnly = true)
    public UserDisplayDto findUserById(UUID id) {
        log.debug("Getting user {}", id);
        User user = userRepository.findUserWithPaymentCardsById(id)
            .orElseThrow(() -> {
                log.warn(LOG_USER_NOT_FOUND, id);
                return new ResourceNotFoundException(NO_SUCH_USER);
            });
        return userMapper.toDisplayDto(user);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @CachePut(value = "users", key = "#id")
    public UserDisplayDto updateUser(UUID id, UserCreateDto userCreateDto) {
        log.info("Updating user {}", id);
        User existingUser = userRepository.findUserWithPaymentCardsById(id)
            .orElseThrow(() -> {
                log.warn(LOG_USER_NOT_FOUND, id);
                return new ResourceNotFoundException(NO_SUCH_USER);
            });
        userMapper.updateEntity(userCreateDto, existingUser);
        User savedUser = userRepository.save(existingUser);
        log.info("User {} updated", id);
        return userMapper.toDisplayDto(savedUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(UUID id) {
        log.info("Deleting user {}", id);
        User user = userRepository.findById(id)
            .orElseThrow(() -> {
                log.warn(LOG_USER_NOT_FOUND, id);
                return new ResourceNotFoundException(NO_SUCH_USER);
            });
        userRepository.delete(user);
        log.info("User {} deleted", id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @CachePut(value = "users", key = "#id")
    public UserDisplayDto activateUser(UUID id) {
        log.info("Activating user {}", id);
        User user = userRepository.findUserWithPaymentCardsById(id)
            .orElseThrow(() -> {
                log.warn(LOG_USER_NOT_FOUND, id);
                return new ResourceNotFoundException(NO_SUCH_USER);
            });
        user.setActive(true);
        User savedUser = userRepository.save(user);
        log.info("User {} activated", id);
        return userMapper.toDisplayDto(savedUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @CachePut(value = "users", key = "#id")
    public UserDisplayDto deactivateUser(UUID id) {
        log.info("Deactivating user {}", id);
        User user = userRepository.findUserWithPaymentCardsById(id)
            .orElseThrow(() -> {
                log.warn(LOG_USER_NOT_FOUND, id);
                return new ResourceNotFoundException(NO_SUCH_USER);
            });
        user.setActive(false);
        User savedUser = userRepository.save(user);
        log.info("User {} deactivated", id);
        return userMapper.toDisplayDto(savedUser);
    }
}