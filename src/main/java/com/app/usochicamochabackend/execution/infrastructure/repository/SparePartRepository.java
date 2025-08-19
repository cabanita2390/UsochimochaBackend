package com.app.usochicamochabackend.execution.infrastructure.repository;

import com.app.usochicamochabackend.execution.infrastructure.entity.SparePartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SparePartRepository extends JpaRepository<SparePartEntity, Long> {
}