package com.app.usochicamochabackend.substation.infrastructure.repository;

import com.app.usochicamochabackend.substation.infrastructure.entity.ConjuntoElementoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ConjuntoElementoRepository extends JpaRepository<ConjuntoElementoEntity, Long> {

    /** Todo el contenido de esos conjuntos (también lo quitado), en su orden. */
    List<ConjuntoElementoEntity> findByConjunto_IdInOrderByOrdenAscIdAsc(Collection<Long> conjuntoIds);

    List<ConjuntoElementoEntity> findByConjunto_IdOrderByOrdenAscIdAsc(Long conjuntoId);

    Optional<ConjuntoElementoEntity> findByConjunto_IdAndElemento_Id(Long conjuntoId, Long elementoId);
}
