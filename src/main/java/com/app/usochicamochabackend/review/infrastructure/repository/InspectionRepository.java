package com.app.usochicamochabackend.review.infrastructure.repository;

import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InspectionRepository extends JpaRepository<InspectionEntity, Long> {}