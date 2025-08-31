package com.app.usochicamochabackend.review.infrastructure.repository;

import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InspectionRepository extends JpaRepository<InspectionEntity, Long> {
    List<InspectionEntity> findByMachineId(Long machineId);
}