package com.app.usochicamochabackend.order.application.dto;

import com.app.usochicamochabackend.review.application.dto.InspectionFormResponse;

import java.util.List;

public record GetAllOrdersByInspectionIdResponse(InspectionFormResponse inspectionResponse, List<OrderResponse> orders) {}