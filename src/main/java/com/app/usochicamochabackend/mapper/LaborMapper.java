package com.app.usochicamochabackend.mapper;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.performance.application.dto.LaborRequest;
import com.app.usochicamochabackend.performance.application.dto.LaborResponse;
import com.app.usochicamochabackend.performance.infrastructure.entity.LaborEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LaborMapper {

    private LaborMapper() {}

    public static LaborEntity toEntity(LaborRequest request, UserEntity mechanic) {
        if (request == null) return null;

        LaborEntity entity = new LaborEntity();
        entity.setDate(LocalDateTime.now());
        entity.setPrice(request.price());
        entity.setSameMecanic(request.sameMecanic());
        entity.setMecanic(request.sameMecanic() ? mechanic : null);
        entity.setContractor(request.contractor());
        entity.setObservations(request.observations());

        return entity;
    }

    public static LaborResponse toResponse(LaborEntity entity) {
        if (entity == null) return null;

        return new LaborResponse(
                entity.getId(),
                entity.getDate(),
                entity.getPrice(),
                entity.getSameMecanic(),
                entity.getMecanic(),
                entity.getContractor(),
                entity.getObservations()
        );
    }
}
