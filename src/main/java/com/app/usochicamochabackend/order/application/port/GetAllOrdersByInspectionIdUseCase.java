package com.app.usochicamochabackend.order.application.port;

import com.app.usochicamochabackend.order.application.dto.GetAllOrdersByInspectionIdResponse;

public interface GetAllOrdersByInspectionIdUseCase {
    GetAllOrdersByInspectionIdResponse getAllOrdersByInspectionId(Long inspectionId);
}
