package com.app.usochicamochabackend.vehicle.application.service;

import com.app.usochicamochabackend.vehicle.application.dto.MarcaModeloRequest;
import com.app.usochicamochabackend.vehicle.application.dto.MarcaModeloResponse;
import com.app.usochicamochabackend.vehicle.application.port.MarcaModeloUseCase;
import com.app.usochicamochabackend.vehicle.infrastructure.entity.MarcaModeloEntity;
import com.app.usochicamochabackend.vehicle.infrastructure.repository.MarcaModeloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarcaModeloService implements MarcaModeloUseCase {

    private final MarcaModeloRepository repository;

    @Override
    public List<MarcaModeloResponse> findAll() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public MarcaModeloResponse findById(Integer id) {
        return repository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca no encontrada"));
    }

    @Override
    public MarcaModeloResponse create(MarcaModeloRequest request) {
        MarcaModeloEntity entity = MarcaModeloEntity.builder()
                .descripcion(request.descripcion())
                .build();
        return mapToResponse(repository.save(entity));
    }

    @Override
    public MarcaModeloResponse update(Integer id, MarcaModeloRequest request) {
        MarcaModeloEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca no encontrada"));
        
        entity.setDescripcion(request.descripcion());
        return mapToResponse(repository.save(entity));
    }

    @Override
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca no encontrada");
        }
        repository.deleteById(id);
    }

    private MarcaModeloResponse mapToResponse(MarcaModeloEntity entity) {
        return new MarcaModeloResponse(
                entity.getIdMarca(),
                entity.getDescripcion()
        );
    }
}
