package com.app.usochicamochabackend.catalog.application.service;

import com.app.usochicamochabackend.catalog.application.dto.CatalogDTO;
import com.app.usochicamochabackend.catalog.infrastructure.entity.TipoVehiculoEntity;
import com.app.usochicamochabackend.catalog.infrastructure.repository.TipoVehiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoVehiculoService {
    private final TipoVehiculoRepository repository;

    public List<CatalogDTO> findAll() {
        return repository.findAll().stream()
                .map(t -> new CatalogDTO(t.getId(), t.getNombreTipo(), t.getActivo()))
                .toList();
    }

    public CatalogDTO create(CatalogDTO dto) {
        TipoVehiculoEntity entity = TipoVehiculoEntity.builder()
                .nombreTipo(dto.name())
                .activo(true)
                .build();
        TipoVehiculoEntity saved = repository.save(entity);
        return new CatalogDTO(saved.getId(), saved.getNombreTipo(), saved.getActivo());
    }

    public CatalogDTO update(Integer id, CatalogDTO dto) {
        TipoVehiculoEntity entity = repository.findById(id).orElseThrow();
        entity.setNombreTipo(dto.name());
        entity.setActivo(dto.active());
        TipoVehiculoEntity saved = repository.save(entity);
        return new CatalogDTO(saved.getId(), saved.getNombreTipo(), saved.getActivo());
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }
}
