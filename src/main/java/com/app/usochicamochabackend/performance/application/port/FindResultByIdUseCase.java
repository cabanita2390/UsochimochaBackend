package com.app.usochicamochabackend.performance.application.port;

import com.app.usochicamochabackend.performance.infrastructure.entity.ResultEntity;

public interface FindResultByIdUseCase {
    ResultEntity findResultById(Long id);
}