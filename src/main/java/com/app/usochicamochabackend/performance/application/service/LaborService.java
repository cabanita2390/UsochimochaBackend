package com.app.usochicamochabackend.performance.application.service;

import com.app.usochicamochabackend.performance.application.port.*;
import com.app.usochicamochabackend.performance.infrastructure.entity.LaborEntity;
import com.app.usochicamochabackend.performance.infrastructure.repository.LaborRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LaborService implements
        CreateLaborUseCase,
        DeleteLaborUseCase,
        FindAllLaborsUseCase,
        FindLaborByIdUseCase,
        UpdateLaborUseCase {

    private final LaborRepository laborRepository;

    @Override
    public LaborEntity createLabor(LaborEntity laborEntity) {
        return laborRepository.save(laborEntity);
    }

    @Override
    public void deleteLabor(Long id) {
        laborRepository.deleteById(id);
    }

    @Override
    public List<LaborEntity> findAllLabors() {
        return laborRepository.findAll();
    }

    @Override
    public LaborEntity findLaborById(Long id) {
        return laborRepository.findById(id).orElse(null);
    }

    @Override
    public LaborEntity updateLabor(LaborEntity laborEntity) {
        return laborRepository.save(laborEntity);
    }
}