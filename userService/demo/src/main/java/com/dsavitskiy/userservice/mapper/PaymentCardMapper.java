package com.dsavitskiy.userservice.mapper;

import com.dsavitskiy.userservice.dto.PaymentCardCreateDto;
import com.dsavitskiy.userservice.dto.PaymentCardDisplayDto;
import com.dsavitskiy.userservice.entity.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {
    @Mapping(target = "user",ignore = true)
    public PaymentCard toEntity(PaymentCardCreateDto paymentCardCreateDto);
    @Mapping(source = "user.id", target = "userId")
    public PaymentCardDisplayDto toDisplayDto(PaymentCard paymentCard);
}
