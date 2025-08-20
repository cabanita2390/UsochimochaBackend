package com.app.usochicamochabackend.performance.infrastructure.repository;

import com.app.usochicamochabackend.performance.infrastructure.entity.LaborEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LaborRepository extends JpaRepository<LaborEntity, Long> {
}