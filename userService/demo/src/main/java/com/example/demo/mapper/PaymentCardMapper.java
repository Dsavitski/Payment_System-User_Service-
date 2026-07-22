package com.example.demo.mapper;

import com.example.demo.dto.PaymentCardCreateDto;
import com.example.demo.dto.PaymentCardDisplayDto;
import com.example.demo.entity.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {
    @Mapping(target = "user",ignore = true)
    public PaymentCard toEntity(PaymentCardCreateDto paymentCardCreateDto);
    @Mapping(source = "user.id", target = "userId")
    public PaymentCardDisplayDto toDisplayDto(PaymentCard paymentCard);
}
