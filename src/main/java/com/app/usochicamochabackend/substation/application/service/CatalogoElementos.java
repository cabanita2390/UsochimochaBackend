package com.app.usochicamochabackend.substation.application.service;

import com.app.usochicamochabackend.substation.application.dto.ElementoDeConjuntoResponse;
import com.app.usochicamochabackend.substation.infrastructure.entity.ConjuntoElementoEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.ConjuntoEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.DisciplinaEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.ElementoEntity;
import com.app.usochicamochabackend.substation.infrastructure.repository.ConjuntoElementoRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.ElementoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Lo que tiene adentro cada conjunto (V54) y los nombres de elemento que se reutilizan entre
 * conjuntos. Lo usan Configuración (web) y el registro de visitas ("Otro" desde el móvil).
 */
@Component
@RequiredArgsConstructor
public class CatalogoElementos {

    private final ElementoRepository elementoRepository;
    private final ConjuntoElementoRepository conjuntoElementoRepository;

    /** Elementos vigentes de cada conjunto (en el conjunto y activos), en su orden. */
    public Map<Long, List<ElementoDeConjuntoResponse>> elementosPorConjunto(Collection<Long> conjuntoIds) {
        if (conjuntoIds.isEmpty()) {
            return Map.of();
        }
        return conjuntoElementoRepository.findByConjunto_IdInOrderByOrdenAscIdAsc(conjuntoIds).stream()
                .filter(ce -> ce.getStatus() && ce.getElemento().getStatus())
                .collect(Collectors.groupingBy(ce -> ce.getConjunto().getId(),
                        Collectors.mapping(ElementoDeConjuntoResponse::fromEntity, Collectors.toList())));
    }

    public List<ElementoDeConjuntoResponse> elementosDe(Long conjuntoId) {
        return elementosPorConjunto(List.of(conjuntoId)).getOrDefault(conjuntoId, List.of());
    }

    /**
     * El elemento de la disciplina con ese nombre, comparando sin mayúsculas ni tildes ("fusibles" es
     * "Fusibles"); si no existe se crea con el nombre normalizado. Uno desactivado se reutiliza tal
     * cual: el nombre es el mismo.
     */
    public ElementoEntity elementoPorNombre(DisciplinaEntity disciplina, String nombre, boolean provisional) {
        String limpio = SubstationCatalogAdminService.normalizarNombre(nombre);
        String clave = SubstationCatalogAdminService.claveNombre(limpio);
        return elementoRepository.findByDisciplina_CodigoOrderByNombreAsc(disciplina.getCodigo()).stream()
                .filter(e -> SubstationCatalogAdminService.claveNombre(e.getNombre()).equals(clave))
                .findFirst()
                .orElseGet(() -> elementoRepository.save(ElementoEntity.builder()
                        .nombre(limpio)
                        .disciplina(disciplina)
                        .provisional(provisional)
                        .build()));
    }

    /**
     * Mete el elemento en el conjunto si no está (o lo vuelve a poner si se había quitado), al final de
     * la lista. Con {@code provisional} queda marcado para que el administrador lo revise.
     */
    public ConjuntoElementoEntity agregarAConjunto(ConjuntoEntity conjunto, ElementoEntity elemento, boolean provisional) {
        var existente = conjuntoElementoRepository.findByConjunto_IdAndElemento_Id(conjunto.getId(), elemento.getId());
        if (existente.isPresent()) {
            ConjuntoElementoEntity ce = existente.get();
            if (!ce.getStatus()) {
                ce.setStatus(true);
                ce.setProvisional(provisional);
                return conjuntoElementoRepository.save(ce);
            }
            return ce;
        }
        int siguiente = conjuntoElementoRepository.findByConjunto_IdOrderByOrdenAscIdAsc(conjunto.getId()).stream()
                .mapToInt(ConjuntoElementoEntity::getOrden).max().orElse(0) + 1;
        return conjuntoElementoRepository.save(ConjuntoElementoEntity.builder()
                .conjunto(conjunto)
                .elemento(elemento)
                .orden(siguiente)
                .provisional(provisional)
                .build());
    }
}
