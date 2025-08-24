package com.app.usochicamochabackend.performance.infrastructure.repository;

import com.app.usochicamochabackend.performance.infrastructure.entity.LaborEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LaborRepository extends JpaRepository<LaborEntity, Long> {
}