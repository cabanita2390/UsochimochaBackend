package com.app.usochicamochabackend.execution.application.service;

import com.app.usochicamochabackend.execution.application.port.*;
import com.app.usochicamochabackend.execution.infrastructure.entity.ResultEntity;
import com.app.usochicamochabackend.execution.infrastructure.repository.ResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResultService implements
        CreateResultUseCase,
        DeleteResultUseCase,
        FindAllResultsUseCase,
        FindResultByIdUseCase,
        UpdateResultUseCase {

    private final ResultRepository resultRepository;

    @Override
    public ResultEntity createResult(ResultEntity resultEntity) {
        return resultRepository.save(resultEntity);
    }

    @Override
    public void deleteResult(Long id) {
        resultRepository.deleteById(id);
    }

    @Override
    public List<ResultEntity> findAllResults() {
        return resultRepository.findAll();
    }

    @Override
    public ResultEntity findResultById(Long id) {
        return resultRepository.findById(id).orElse(null);
    }

    @Override
    public ResultEntity updateResult(ResultEntity resultEntity) {
        return resultRepository.save(resultEntity);
    }
}