package com.dsavitskiy.userservice.mapper;

import com.dsavitskiy.userservice.dto.UserCreateDto;
import com.dsavitskiy.userservice.dto.UserDisplayDto;
import com.dsavitskiy.userservice.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    public User toEntity(UserCreateDto userCreateDto);
    public UserDisplayDto toDisplayDto(User user);
}
