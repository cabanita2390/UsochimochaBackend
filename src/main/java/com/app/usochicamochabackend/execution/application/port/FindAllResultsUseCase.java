package com.app.usochicamochabackend.execution.application.port;

import com.app.usochicamochabackend.execution.infrastructure.entity.ResultEntity;
import java.util.List;

public interface FindAllResultsUseCase {
    List<ResultEntity> findAllResults();
}