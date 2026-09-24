package com.app.usochicamochabackend.substation.infrastructure.repository;

import com.app.usochicamochabackend.substation.infrastructure.entity.EjecucionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface EjecucionRepository extends JpaRepository<EjecucionEntity, Long>, JpaSpecificationExecutor<EjecucionEntity> {

    boolean existsByUuidCliente(UUID uuidCliente);

    Optional<EjecucionEntity> findByUuidCliente(UUID uuidCliente);

    Page<EjecucionEntity> findByProgramacion_Id(Long programacionId, Pageable pageable);

    Optional<EjecucionEntity> findFirstByProgramacion_IdOrderByIdDesc(Long programacionId);
}
