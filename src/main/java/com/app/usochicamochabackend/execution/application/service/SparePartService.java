package com.app.usochicamochabackend.execution.application.service;

import com.app.usochicamochabackend.execution.application.port.*;
import com.app.usochicamochabackend.execution.infrastructure.entity.SparePartEntity;
import com.app.usochicamochabackend.execution.infrastructure.repository.SparePartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SparePartService implements
        CreateSparePartUseCase,
        DeleteSparePartUseCase,
        FindAllSparePartsUseCase,
        FindSparePartByIdUseCase,
        UpdateSparePartUseCase {

    private final SparePartRepository sparePartRepository;

    @Override
    public SparePartEntity createSparePart(SparePartEntity sparePartEntity) {
        return sparePartRepository.save(sparePartEntity);
    }

    @Override
    public void deleteSparePart(Long id) {
        sparePartRepository.deleteById(id);
    }

    @Override
    public List<SparePartEntity> findAllSpareParts() {
        return sparePartRepository.findAll();
    }

    @Override
    public SparePartEntity findSparePartById(Long id) {
        return sparePartRepository.findById(id).orElse(null);
    }

    @Override
    public SparePartEntity updateSparePart(SparePartEntity sparePartEntity) {
        return sparePartRepository.save(sparePartEntity);
    }
}