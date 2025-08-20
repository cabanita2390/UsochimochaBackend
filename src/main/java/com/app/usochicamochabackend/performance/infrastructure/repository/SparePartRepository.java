package com.app.usochicamochabackend.performance.infrastructure.repository;

import com.app.usochicamochabackend.performance.infrastructure.entity.SparePartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SparePartRepository extends JpaRepository<SparePartEntity, Long> {
}