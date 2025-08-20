package com.app.usochicamochabackend.performance.application.port;

import com.app.usochicamochabackend.performance.infrastructure.entity.ResultEntity;

public interface CreateResultUseCase {
    ResultEntity createResult(ResultEntity resultEntity);
}