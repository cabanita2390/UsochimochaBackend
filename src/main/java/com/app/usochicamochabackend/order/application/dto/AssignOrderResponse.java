package com.app.usochicamochabackend.order.application.dto;

import com.app.usochicamochabackend.review.application.dto.InspectionResponse;
import com.app.usochicamochabackend.user.application.dto.UserResponse;

import java.time.LocalDateTime;

public record AssignOrderResponse(String status,
                                  LocalDateTime date,
                                  String description,
                                  InspectionResponse inspection,
                                  UserResponse assignerUser,
                                  UserResponse assignedUser) {}