package com.example.demo.mapper;

import com.example.demo.dto.UserCreateDto;
import com.example.demo.dto.UserDisplayDto;
import com.example.demo.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    public User toEntity(UserCreateDto userCreateDto);
    public UserDisplayDto toDisplayDto(User user);
}
