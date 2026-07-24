package com.dsavitskiy.userservice.service;

import com.dsavitskiy.userservice.dto.UserCreateDto;
import com.dsavitskiy.userservice.dto.UserDisplayDto;
import com.dsavitskiy.userservice.entity.User;
import com.dsavitskiy.userservice.exception.ResourceNotFoundException;
import com.dsavitskiy.userservice.mapper.UserMapper;
import com.dsavitskiy.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final String NO_SUCH_PAYMENT_CARD = "User with such id not found!";

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDisplayDto createUser(UserCreateDto userCreateDto) {
        User user = userMapper.toEntity(userCreateDto);
        User savedUser = userRepository.save(user);
        return userMapper.toDisplayDto(savedUser);
    }

    @Cacheable(value = "users",key = "#id")
    public UserDisplayDto findUserById(Long id) {
        User user = userRepository.findUserWithPaymentCardsById(id).orElseThrow(
            ()->new ResourceNotFoundException(NO_SUCH_PAYMENT_CARD));
        return userMapper.toDisplayDto(user);
    }

    @Transactional
    @CachePut(value = "users",key = "#id")
    public UserDisplayDto updateUser(Long id, UserCreateDto userCreateDto) {
        User existingUser = userRepository.findUserWithPaymentCardsById(id).orElseThrow(
            ()->new ResourceNotFoundException(NO_SUCH_PAYMENT_CARD));
        userMapper.updateEntity(userCreateDto, existingUser);
        User savedUser = userRepository.save(existingUser);
        return userMapper.toDisplayDto(savedUser);
    }

    @Transactional
    @CacheEvict(value = "users",key = "#id")
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(
            ()->new ResourceNotFoundException(NO_SUCH_PAYMENT_CARD));
        userRepository.delete(user);
    }

    @Transactional
    @CachePut(value = "users",key = "#id")
    public UserDisplayDto activateUser(Long id) {
        User user= userRepository.findUserWithPaymentCardsById(id).orElseThrow(
            ()-> new ResourceNotFoundException(NO_SUCH_PAYMENT_CARD));
        user.setActive(true);
        User savedUser = userRepository.save(user);
        return userMapper.toDisplayDto(savedUser);
    }

    @Transactional
    @CachePut(value = "users",key = "#id")
    public UserDisplayDto deactivateUser(Long id) {
        User user= userRepository.findUserWithPaymentCardsById(id).orElseThrow(
            ()-> new ResourceNotFoundException(NO_SUCH_PAYMENT_CARD));
        user.setActive(false);
        User savedUser = userRepository.save(user);
        return userMapper.toDisplayDto(savedUser);
    }
}
