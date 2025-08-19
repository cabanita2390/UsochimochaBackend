package com.app.usochicamochabackend.order.domain;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.execution.domain.Result;
import com.app.usochicamochabackend.inspection.domain.Inspection;

import java.time.LocalDateTime;

public class Order {
    private Long id;
    private String status;
    private LocalDateTime orderDate;
    private String description;

    private Inspection inspection;
    private Result result;

    private UserEntity assignerUser;
    private UserEntity assignedUser;


}
