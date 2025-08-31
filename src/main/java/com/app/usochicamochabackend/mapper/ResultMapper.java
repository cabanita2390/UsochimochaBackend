package com.app.usochicamochabackend.mapper;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.order.application.dto.OrderDTO;
import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;
import com.app.usochicamochabackend.performance.application.dto.*;
import com.app.usochicamochabackend.performance.infrastructure.entity.LaborEntity;
import com.app.usochicamochabackend.performance.infrastructure.entity.ResultEntity;
import com.app.usochicamochabackend.performance.infrastructure.entity.SparePartEntity;

import java.time.LocalDateTime;
import java.util.List;

public class ResultMapper {

    private ResultMapper() {}

    public static ResultEntity toEntity(ExecuteAnOrderRequest request, OrderEntity order) {
        if (request == null || order == null) return null;

        ResultEntity result = new ResultEntity();
        result.setDate(LocalDateTime.now());
        result.setTimeSpent(request.timeSpent());
        result.setDescription(result.getDescription());
        result.setOrder(order);

        UserEntity mechanicFromInspection = order.getInspection() != null ? order.getInspection().getUser() : null;

        // map labors
        List<LaborEntity> labors = request.labors().stream()
                .map(laborRequest -> LaborMapper.toEntity(laborRequest, mechanicFromInspection))
                .toList();
        result.setLaborForce(labors);

        // map spare parts
        List<SparePartEntity> spareParts = request.spareParts().stream()
                .map(SparePartMapper::toEntity)
                .toList();
        result.setSparePart(spareParts);

        return result;
    }

    public static ExecuteDTO toResponse(ResultEntity entity, OrderDTO orderDTO) {
        if (entity == null) return null;

        List<LaborResponse> labors = entity.getLaborForce().stream()
                .map(LaborMapper::toResponse)
                .toList();

        List<SparePartResponse> spareParts = entity.getSparePart().stream()
                .map(SparePartMapper::toResponse)
                .toList();

        return new ExecuteDTO(
                orderDTO,
                entity.getDate(),
                entity.getTimeSpent(),
                entity.getDescription(),
                labors,
                spareParts
        );
    }

    public static ResultDTO toResponseResult(ResultEntity entity) {
        if (entity == null) return null;

        List<LaborResponse> labors = entity.getLaborForce().stream()
                .map(LaborMapper::toResponse)
                .toList();

        List<SparePartResponse> spareParts = entity.getSparePart().stream()
                .map(SparePartMapper::toResponse)
                .toList();

        return new ResultDTO(
                entity.getId(),
                entity.getDate(),
                entity.getTimeSpent(),
                entity.getDescription(),
                labors,
                spareParts
        );
    }

    public static List<ResultDTO> toResponseList(List<ResultEntity> entityList) {
        if (entityList == null) return null;

        return entityList.stream().map(ResultMapper::toResponseResult).toList();
    }
}
