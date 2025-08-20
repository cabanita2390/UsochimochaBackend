package com.app.usochicamochabackend.performance.application.port;

import com.app.usochicamochabackend.performance.infrastructure.entity.ResultEntity;
import java.util.List;

public interface FindAllResultsUseCase {
    List<ResultEntity> findAllResults();
}