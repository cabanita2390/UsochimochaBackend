package com.app.usochicamochabackend.mapper;

import com.app.usochicamochabackend.order.application.dto.AssignOrderResponse;
import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;

public class OrderMapper {

    private OrderMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static AssignOrderResponse toDto(OrderEntity entity) {
        if (entity == null) {
            return null;
        }

        return new AssignOrderResponse(
                entity.getId(),
                entity.getStatus(),
                entity.getDate(),
                entity.getDescription(),
                InspectionMapper.toDtoWithoutOrder(entity.getInspection()), // ← rompe ciclo
                UserMapper.toResponse(entity.getAssignerUser()),
                UserMapper.toResponse(entity.getAssignedUser())
        );
    }

    public static AssignOrderResponse toDtoWithoutInspection(OrderEntity entity) {
        if (entity == null) {
            return null;
        }

        return new AssignOrderResponse(
                entity.getId(),
                entity.getStatus(),
                entity.getDate(),
                entity.getDescription(),
                null,
                UserMapper.toResponse(entity.getAssignerUser()),
                UserMapper.toResponse(entity.getAssignedUser())
        );
    }
}
