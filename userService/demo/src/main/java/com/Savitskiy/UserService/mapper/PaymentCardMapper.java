package com.Savitskiy.UserService.mapper;

import com.Savitskiy.UserService.dto.PaymentCardCreateDto;
import com.Savitskiy.UserService.dto.PaymentCardDisplayDto;
import com.Savitskiy.UserService.entity.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {
    @Mapping(target = "user",ignore = true)
    public PaymentCard toEntity(PaymentCardCreateDto paymentCardCreateDto);
    @Mapping(source = "user.id", target = "userId")
    public PaymentCardDisplayDto toDisplayDto(PaymentCard paymentCard);
}
