package com.app.usochicamochabackend.mapper;

import com.app.usochicamochabackend.order.application.dto.OrderResponse;
import com.app.usochicamochabackend.order.application.dto.OrderWithoutInspectionResponse;
import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;

import java.util.List;

public class OrderMapper {

    private OrderMapper() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static OrderResponse toDto(OrderEntity entity) {
        if (entity == null) {
            return null;
        }

        return new OrderResponse(
                entity.getId(),
                entity.getStatus(),
                entity.getDate(),
                entity.getDescription(),
                InspectionMapper.toDto(entity.getInspection()),
                UserMapper.toResponse(entity.getAssignerUser())
        );
    }

    public static OrderWithoutInspectionResponse toDtoWithoutInspection(OrderEntity entity) {
        if (entity == null) {
            return null;
        }

        return new OrderWithoutInspectionResponse(
                entity.getId(),
                entity.getStatus(),
                entity.getDate(),
                entity.getDescription(),
                UserMapper.toResponse(entity.getAssignerUser())
        );
    }

    public static List<OrderWithoutInspectionResponse> toDtoListWithoutInspection(List<OrderEntity> entity) {
        if (entity == null) return null;

        return entity.stream().map(OrderMapper::toDtoWithoutInspection).toList();
    }
}
