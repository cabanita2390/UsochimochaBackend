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
                entity.getStatus(),
                entity.getDate(),
                entity.getDescription(),
                InspectionMapper.toDto(entity.getInspection()),
                UserMapper.toResponse(entity.getAssignerUser()),
                UserMapper.toResponse(entity.getAssignedUser())
        );
    }
}
