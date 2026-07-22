package com.example.demo.service;

import com.example.demo.dto.UserCreateDto;
import com.example.demo.dto.UserDisplayDto;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

    public UserDisplayDto findUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
            ()->new ResourceNotFoundException("User with such id not found!"));
        return userMapper.toDisplayDto(user);
    }

    @Transactional
    public UserDisplayDto updateUser(Long id, UserCreateDto userCreateDto) {
        User existingUser = userRepository.findById(id).orElseThrow(
            ()->new ResourceNotFoundException("User with such id not found!"));
        existingUser.setName(userCreateDto.getName());
        existingUser.setSurname(userCreateDto.getSurname());
        existingUser.setBirthDate(userCreateDto.getBirthDate());
        existingUser.setEmail(userCreateDto.getEmail());
        User savedUser = userRepository.save(existingUser);
        return userMapper.toDisplayDto(savedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(
            ()->new ResourceNotFoundException("User with such id not found!"));
        userRepository.delete(user);
    }

    @Transactional
    public UserDisplayDto activateUser(Long id) {
        User user= userRepository.findById(id).orElseThrow(
            ()-> new ResourceNotFoundException("User with such id not found!"));
        user.setActive(true);
        User savedUser = userRepository.save(user);
        return userMapper.toDisplayDto(savedUser);
    }

    @Transactional
    public UserDisplayDto deactivateUser(Long id) {
        User user= userRepository.findById(id).orElseThrow(
            ()-> new ResourceNotFoundException("User with such id not found!"));
        user.setActive(false);
        User savedUser = userRepository.save(user);
        return userMapper.toDisplayDto(savedUser);
    }
}
