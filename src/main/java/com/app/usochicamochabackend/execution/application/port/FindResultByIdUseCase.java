package com.app.usochicamochabackend.execution.application.port;

import com.app.usochicamochabackend.execution.infrastructure.entity.ResultEntity;

public interface FindResultByIdUseCase {
    ResultEntity findResultById(Long id);
}