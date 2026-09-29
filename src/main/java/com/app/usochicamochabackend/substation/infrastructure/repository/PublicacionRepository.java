package com.app.usochicamochabackend.substation.infrastructure.repository;

import com.app.usochicamochabackend.substation.infrastructure.entity.PublicacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PublicacionRepository extends JpaRepository<PublicacionEntity, Long> {

    /** Última publicación vigente (no revertida) del año. */
    Optional<PublicacionEntity> findFirstByAnioAndRevertidaFalseOrderByIdDesc(Integer anio);
}
