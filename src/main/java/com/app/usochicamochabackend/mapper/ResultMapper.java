package com.app.usochicamochabackend.mapper;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.order.application.dto.OrderDTO;
import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;
import com.app.usochicamochabackend.performance.application.dto.ExecuteAnOrderRequest;
import com.app.usochicamochabackend.performance.application.dto.ExecuteAnOrderResponse;
import com.app.usochicamochabackend.performance.application.dto.LaborResponse;
import com.app.usochicamochabackend.performance.application.dto.SparePartResponse;
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

    public static ExecuteAnOrderResponse toResponse(ResultEntity entity, OrderDTO orderDTO) {
        if (entity == null) return null;

        List<LaborResponse> labors = entity.getLaborForce().stream()
                .map(LaborMapper::toResponse)
                .toList();

        List<SparePartResponse> spareParts = entity.getSparePart().stream()
                .map(SparePartMapper::toResponse)
                .toList();

        return new ExecuteAnOrderResponse(
                orderDTO,
                entity.getDate(),
                entity.getTimeSpent(),
                labors,
                spareParts
        );
    }
}
