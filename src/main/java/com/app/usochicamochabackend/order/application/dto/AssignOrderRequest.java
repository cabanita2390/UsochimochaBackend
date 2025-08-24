package com.app.usochicamochabackend.order.application.dto;

public record AssignOrderRequest(Long inspectionId, Long assignerUserId, String description) {}