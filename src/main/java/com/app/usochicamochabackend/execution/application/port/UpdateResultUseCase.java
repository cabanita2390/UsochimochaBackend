package com.app.usochicamochabackend.execution.application.port;

import com.app.usochicamochabackend.execution.infrastructure.entity.ResultEntity;

public interface UpdateResultUseCase {
    ResultEntity updateResult(ResultEntity resultEntity);
}