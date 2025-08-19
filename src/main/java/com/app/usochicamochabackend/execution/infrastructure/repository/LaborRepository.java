package com.app.usochicamochabackend.execution.infrastructure.repository;

import com.app.usochicamochabackend.execution.infrastructure.entity.LaborEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LaborRepository extends JpaRepository<LaborEntity, Long> {
}