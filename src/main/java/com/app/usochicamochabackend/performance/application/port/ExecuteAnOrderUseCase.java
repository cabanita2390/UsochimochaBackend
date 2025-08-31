package com.app.usochicamochabackend.performance.application.port;

import com.app.usochicamochabackend.performance.application.dto.ExecuteAnOrderRequest;
import com.app.usochicamochabackend.performance.application.dto.ExecuteDTO;

public interface ExecuteAnOrderUseCase {
    ExecuteDTO execute(ExecuteAnOrderRequest request);
}
