package com.app.usochicamochabackend.performance.application.port;

import com.app.usochicamochabackend.performance.application.dto.ExecuteAnOrderRequest;
import com.app.usochicamochabackend.performance.application.dto.ExecuteAnOrderResponse;

public interface ExecuteAnOrderUseCase {
    ExecuteAnOrderResponse execute(ExecuteAnOrderRequest request);
}
