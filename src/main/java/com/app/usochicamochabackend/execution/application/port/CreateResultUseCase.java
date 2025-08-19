package com.app.usochicamochabackend.execution.application.port;

import com.app.usochicamochabackend.execution.infrastructure.entity.ResultEntity;

public interface CreateResultUseCase {
    ResultEntity createResult(ResultEntity resultEntity);
}