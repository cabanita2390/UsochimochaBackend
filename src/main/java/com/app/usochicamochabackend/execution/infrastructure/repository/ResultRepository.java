package com.app.usochicamochabackend.execution.infrastructure.repository;

import com.app.usochicamochabackend.execution.infrastructure.entity.ResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultRepository extends JpaRepository<ResultEntity, Long> {
}