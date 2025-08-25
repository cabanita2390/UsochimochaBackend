package com.app.usochicamochabackend.order.application.port;

import com.app.usochicamochabackend.order.application.dto.AssignOrderResponse;

public interface GetOrderByInspectionId {
    AssignOrderResponse getOrderByInspectionId(Long inspectionId);
}
