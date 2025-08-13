package com.app.usochicamochabackend.inspection.infrastructure.repository;

import com.app.usochicamochabackend.inspection.infrastructure.entity.InspectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InspectionRepository extends JpaRepository<InspectionEntity, Long> {}