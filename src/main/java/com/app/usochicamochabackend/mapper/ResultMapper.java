package com.app.usochicamochabackend.mapper;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.order.application.dto.OrderResponse;
import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;
import com.app.usochicamochabackend.performance.application.dto.*;
import com.app.usochicamochabackend.performance.infrastructure.entity.LaborEntity;
import com.app.usochicamochabackend.performance.infrastructure.entity.ResultEntity;
import com.app.usochicamochabackend.performance.infrastructure.entity.SparePartEntity;

import java.time.LocalDateTime;
import java.util.List;

public class ResultMapper {

    private ResultMapper() {}

    public static ResultEntity toEntity(
            ExecuteAnOrderRequest request,
            OrderEntity order,
            UserRepositoryJpa userRepository
    ) {
        if (request == null || order == null) return null;

        ResultEntity result = new ResultEntity();
        result.setDate(LocalDateTime.now());
        result.setTimeSpent(request.timeSpent());
        result.setDescription(request.description());
        result.setOrder(order);

        // --- regla de negocio sameMecanic ---
        UserEntity mechanic = null;
        if (request.labor() != null && Boolean.TRUE.equals(request.labor().sameMecanic())) {
            mechanic = order.getInspection() != null ? order.getInspection().getUser() : null;
        }

        // Labor (única)
        LaborEntity labor = LaborMapper.toEntity(request.labor(), mechanic);
        result.setLaborForce(labor);

        // Spare parts
        List<SparePartEntity> spareParts = request.spareParts().stream()
                .map(SparePartMapper::toEntity)
                .toList();
        result.setSparePart(spareParts);

        return result;
    }

    public static ExecuteDTO toResponse(ResultEntity entity, OrderResponse orderResponse) {
        if (entity == null) return null;

        List<SparePartResponse> spareParts = entity.getSparePart().stream()
                .map(SparePartMapper::toResponse)
                .toList();

        LaborResponse labor = LaborMapper.toResponse(entity.getLaborForce());

        return new ExecuteDTO(
                orderResponse,
                entity.getDate(),
                entity.getDescription(),
                entity.getTimeSpent(),
                labor,
                spareParts
        );
    }

    public static ResultDTO toResponseResult(ResultEntity entity) {
        if (entity == null) return null;

        List<SparePartResponse> spareParts = entity.getSparePart().stream()
                .map(SparePartMapper::toResponse)
                .toList();

        LaborResponse labor = LaborMapper.toResponse(entity.getLaborForce());

        return new ResultDTO(
                entity.getId(),
                entity.getDate(),
                entity.getDescription(),
                entity.getTimeSpent(),
                labor,
                spareParts
        );
    }

    public static List<ResultDTO> toResponseList(List<ResultEntity> entityList) {
        if (entityList == null) return null;
        return entityList.stream().map(ResultMapper::toResponseResult).toList();
    }
}
