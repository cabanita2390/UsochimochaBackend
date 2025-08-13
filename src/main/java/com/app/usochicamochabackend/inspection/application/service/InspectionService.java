package com.app.usochicamochabackend.inspection.application.service;

import com.app.usochicamochabackend.inspection.application.port.*;
import com.app.usochicamochabackend.inspection.infrastructure.entity.InspectionEntity;
import com.app.usochicamochabackend.inspection.infrastructure.repository.InspectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InspectionService implements
        CreateInspectionUseCase,
        DeleteInspectionUseCase,
        FindAllInspectionsUseCase,
        FindInspectionByIdUseCase,
        UpdateInspectionUseCase {

    private final InspectionRepository inspectionRepository;

    @Override
    public InspectionEntity createInspection(InspectionEntity inspectionEntity) {
        return inspectionRepository.save(inspectionEntity);
    }

    @Override
    public void deleteInspection(Long id) {
        inspectionRepository.deleteById(id);
    }

    @Override
    public List<InspectionEntity> findAllInspections() {
        return inspectionRepository.findAll();
    }

    @Override
    public InspectionEntity findInspectionById(Long id) {
        return inspectionRepository.findById(id).orElse(null);
    }

    @Override
    public InspectionEntity updateInspection(InspectionEntity inspectionEntity) {
        return inspectionRepository.save(inspectionEntity);
    }
}