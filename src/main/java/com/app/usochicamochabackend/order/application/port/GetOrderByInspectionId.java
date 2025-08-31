package com.app.usochicamochabackend.order.application.port;

import com.app.usochicamochabackend.order.application.dto.OrderDTO;

public interface GetOrderByInspectionId {
    OrderDTO getOrderByInspectionId(Long inspectionId);
}
