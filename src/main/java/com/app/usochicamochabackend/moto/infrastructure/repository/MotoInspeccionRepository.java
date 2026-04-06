package com.app.usochicamochabackend.moto.infrastructure.repository;

import com.app.usochicamochabackend.moto.infrastructure.entity.InspeccionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MotoInspeccionRepository extends JpaRepository<InspeccionEntity, Long> {
}
