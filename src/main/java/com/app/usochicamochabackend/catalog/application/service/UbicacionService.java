package com.app.usochicamochabackend.catalog.application.service;

import com.app.usochicamochabackend.catalog.application.dto.CatalogDTO;
import com.app.usochicamochabackend.catalog.infrastructure.entity.UbicacionEntity;
import com.app.usochicamochabackend.catalog.infrastructure.repository.UbicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UbicacionService {
    private final UbicacionRepository repository;

    public List<CatalogDTO> findAll() {
        return repository.findAll().stream()
                .map(u -> new CatalogDTO(u.getId(), u.getNombreUbicacion(), u.getActivo()))
                .toList();
    }

    public CatalogDTO create(CatalogDTO dto) {
        UbicacionEntity entity = UbicacionEntity.builder()
                .nombreUbicacion(dto.name())
                .activo(true)
                .build();
        UbicacionEntity saved = repository.save(entity);
        return new CatalogDTO(saved.getId(), saved.getNombreUbicacion(), saved.getActivo());
    }

    public CatalogDTO update(Integer id, CatalogDTO dto) {
        UbicacionEntity entity = repository.findById(id).orElseThrow();
        entity.setNombreUbicacion(dto.name());
        entity.setActivo(dto.active());
        UbicacionEntity saved = repository.save(entity);
        return new CatalogDTO(saved.getId(), saved.getNombreUbicacion(), saved.getActivo());
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }
}
