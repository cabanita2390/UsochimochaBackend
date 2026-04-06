package com.app.usochicamochabackend.mapper;

import com.app.usochicamochabackend.update.application.dto.BrandRequest;
import com.app.usochicamochabackend.update.application.dto.BrandResponse;
import com.app.usochicamochabackend.update.infrastructure.entity.BrandEntity;

public class BrandMapper {

    public static BrandEntity toEntity(BrandRequest request) {
        return BrandEntity.builder()
                .type(request.type())
                .name(request.name())
                .build();
    }

    public static BrandResponse toResponse(BrandEntity entity) {
        return new BrandResponse(
                entity.getId(),
                entity.getType(),
                entity.getName()
        );
    }
}
