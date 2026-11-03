package com.app.usochicamochabackend.tire.infrastructure.repository;

import com.app.usochicamochabackend.tire.infrastructure.entity.LlantaPerfilEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LlantaPerfilRepository extends JpaRepository<LlantaPerfilEntity, Long> {
    List<LlantaPerfilEntity> findByStatusTrueOrderByNombreAsc();
    Optional<LlantaPerfilEntity> findByIdAndStatusTrue(Long id);
    boolean existsByNombreIgnoreCaseAndStatusTrue(String nombre);
    boolean existsByNombreIgnoreCaseAndStatusTrueAndIdNot(String nombre, Long id);
}
