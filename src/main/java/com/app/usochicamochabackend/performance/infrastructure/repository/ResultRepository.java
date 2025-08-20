package com.app.usochicamochabackend.performance.infrastructure.repository;

import com.app.usochicamochabackend.performance.infrastructure.entity.ResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultRepository extends JpaRepository<ResultEntity, Long> {
}