package com.Savitskiy.UserService.service;

import com.Savitskiy.UserService.dto.UserCreateDto;
import com.Savitskiy.UserService.dto.UserDisplayDto;
import com.Savitskiy.UserService.entity.User;
import com.Savitskiy.UserService.exception.ResourceNotFoundException;
import com.Savitskiy.UserService.mapper.UserMapper;
import com.Savitskiy.UserService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

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
            ()->new ResourceNotFoundException("User with such id not found!"));
        return userMapper.toDisplayDto(user);
    }

    @Transactional
    @CachePut(value = "users",key = "#id")
    public UserDisplayDto updateUser(Long id, UserCreateDto userCreateDto) {
        User existingUser = userRepository.findUserWithPaymentCardsById(id).orElseThrow(
            ()->new ResourceNotFoundException("User with such id not found!"));
        existingUser.setName(userCreateDto.getName());
        existingUser.setSurname(userCreateDto.getSurname());
        existingUser.setBirthDate(userCreateDto.getBirthDate());
        existingUser.setEmail(userCreateDto.getEmail());
        User savedUser = userRepository.save(existingUser);
        return userMapper.toDisplayDto(savedUser);
    }

    @Transactional
    @CacheEvict(value = "users",key = "#id")
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(
            ()->new ResourceNotFoundException("User with such id not found!"));
        userRepository.delete(user);
    }

    @Transactional
    @CachePut(value = "users",key = "#id")
    public UserDisplayDto activateUser(Long id) {
        User user= userRepository.findUserWithPaymentCardsById(id).orElseThrow(
            ()-> new ResourceNotFoundException("User with such id not found!"));
        user.setActive(true);
        User savedUser = userRepository.save(user);
        return userMapper.toDisplayDto(savedUser);
    }

    @Transactional
    @CachePut(value = "users",key = "#id")
    public UserDisplayDto deactivateUser(Long id) {
        User user= userRepository.findUserWithPaymentCardsById(id).orElseThrow(
            ()-> new ResourceNotFoundException("User with such id not found!"));
        user.setActive(false);
        User savedUser = userRepository.save(user);
        return userMapper.toDisplayDto(savedUser);
    }
}
