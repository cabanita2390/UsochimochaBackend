package com.app.usochicamochabackend.performance.application.service;

import com.app.usochicamochabackend.performance.application.dto.ExecuteAnOrderRequest;
import com.app.usochicamochabackend.performance.application.dto.ExecuteAnOrderResponse;
import com.app.usochicamochabackend.performance.application.port.*;
import com.app.usochicamochabackend.performance.infrastructure.entity.ResultEntity;
import com.app.usochicamochabackend.performance.infrastructure.repository.ResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResultService implements ExecuteAnOrderUseCase {

    private final ResultRepository resultRepository;


    @Override
    public ExecuteAnOrderResponse execute(ExecuteAnOrderRequest request) {


        return null;
    }
}