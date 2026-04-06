package com.app.usochicamochabackend.update.infrastructure.repository;

import com.app.usochicamochabackend.update.infrastructure.entity.BrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface BrandRepository extends JpaRepository<BrandEntity, Long> {
    BrandEntity findBrandEntityById(Long id);

    List<BrandEntity> findByStatusTrue();

    List<BrandEntity> findAllByType(String type);
}