package com.app.usochicamochabackend.catalog.application.service;

import com.app.usochicamochabackend.catalog.application.dto.CatalogDTO;
import com.app.usochicamochabackend.catalog.infrastructure.entity.AreaEntity;
import com.app.usochicamochabackend.catalog.infrastructure.repository.AreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AreaService {
    private final AreaRepository areaRepository;

    public List<CatalogDTO> findAll() {
        return areaRepository.findAll().stream()
                .map(a -> new CatalogDTO(a.getId(), a.getNombre(), a.getActivo()))
                .toList();
    }

    public CatalogDTO create(CatalogDTO dto) {
        AreaEntity entity = AreaEntity.builder()
                .nombre(dto.name())
                .activo(true)
                .build();
        AreaEntity saved = areaRepository.save(entity);
        return new CatalogDTO(saved.getId(), saved.getNombre(), saved.getActivo());
    }

    public CatalogDTO update(Integer id, CatalogDTO dto) {
        AreaEntity entity = areaRepository.findById(id).orElseThrow();
        entity.setNombre(dto.name());
        entity.setActivo(dto.active());
        AreaEntity saved = areaRepository.save(entity);
        return new CatalogDTO(saved.getId(), saved.getNombre(), saved.getActivo());
    }

    public void delete(Integer id) {
        areaRepository.deleteById(id);
    }
}
