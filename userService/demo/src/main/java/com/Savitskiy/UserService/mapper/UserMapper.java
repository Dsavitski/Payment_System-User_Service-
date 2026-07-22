package com.Savitskiy.UserService.mapper;

import com.Savitskiy.UserService.dto.UserCreateDto;
import com.Savitskiy.UserService.dto.UserDisplayDto;
import com.Savitskiy.UserService.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    public User toEntity(UserCreateDto userCreateDto);
    public UserDisplayDto toDisplayDto(User user);
}
