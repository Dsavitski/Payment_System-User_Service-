package com.dsavitskiy.userservice.mapper;

import com.dsavitskiy.userservice.dto.PaymentCardCreateDto;
import com.dsavitskiy.userservice.dto.PaymentCardDisplayDto;
import com.dsavitskiy.userservice.entity.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {

    @Mapping(target = "user", ignore = true)
    PaymentCard toEntity(PaymentCardCreateDto dto);

    @Mapping(source = "user.id", target = "userId")
    PaymentCardDisplayDto toDisplayDto(PaymentCard entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntity(PaymentCardCreateDto dto,
                      @MappingTarget PaymentCard entity);
}
