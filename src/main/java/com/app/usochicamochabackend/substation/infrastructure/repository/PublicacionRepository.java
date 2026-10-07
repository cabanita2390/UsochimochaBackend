package com.app.usochicamochabackend.substation.infrastructure.repository;

import com.app.usochicamochabackend.substation.infrastructure.entity.PublicacionEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;

import java.util.Optional;

public interface PublicacionRepository extends JpaRepository<PublicacionEntity, Long> {

    /** Última publicación vigente (no revertida) del año. */
    Optional<PublicacionEntity> findFirstByAnioAndRevertidaFalseOrderByIdDesc(Integer anio);

    /** Igual, bloqueada para escritura: dos "deshacer" seguidos no revierten dos veces. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PublicacionEntity> findFirstByAnioAndRevertidaFalseAndInicialFalseOrderByIdDesc(Integer anio);

    List<PublicacionEntity> findByAnioOrderByIdDesc(Integer anio);
}
