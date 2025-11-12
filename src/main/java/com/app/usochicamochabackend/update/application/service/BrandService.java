package com.app.usochicamochabackend.update.application.service;

import com.app.usochicamochabackend.actions.application.port.SaveActionUseCase;
import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.mapper.BrandMapper;
import com.app.usochicamochabackend.notifications.application.NotificationService;
import com.app.usochicamochabackend.update.application.dto.BrandRequest;
import com.app.usochicamochabackend.update.application.dto.BrandResponse;
import com.app.usochicamochabackend.update.application.port.*;
import com.app.usochicamochabackend.update.infrastructure.entity.BrandEntity;
import com.app.usochicamochabackend.update.infrastructure.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandService implements
        CreateBrandUseCase,
        GetBrandByIdUseCase,
        UpdateBrandUseCase,
        DeleteBrandUseCase,
        GetAllBrandsUseCase,
        GetAllBrandsByTypeUseCase {

    private final BrandRepository brandRepository;
    private final SaveActionUseCase saveActionUseCase;
    private final NotificationService notificationService;

    @Override
    public BrandResponse createBrand(BrandRequest request) {
        BrandEntity entity = BrandMapper.toEntity(request);

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        saveActionUseCase.save("El usuario " + userPrincipal.username() + " ha creado una marca de aceite " + request.name() + " de tipo " + entity.getType());

        return BrandMapper.toResponse(brandRepository.save(entity));
    }

    @Override
    public BrandResponse getBrandById(Long id) {
        BrandEntity entity = brandRepository.findById(id)
                .filter(BrandEntity::isStatus)
                .orElseThrow(() -> new RuntimeException("Brand not found with id " + id));

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        saveActionUseCase.save("El usuario " + userPrincipal.username() + " ha obtenido una marca de aceite " + entity.getName() + " de tipo " + entity.getType());

        return BrandMapper.toResponse(entity);
    }

    @Override
    public BrandResponse updateBrandById(Long id, BrandRequest updated) {
        BrandEntity existing = brandRepository.findById(id)
                .filter(BrandEntity::isStatus)
                .orElseThrow(() -> new RuntimeException("Brand not found with id " + id));

        existing.setName(updated.name());
        existing.setType(updated.type());

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        saveActionUseCase.save("El usuario " + userPrincipal.username() + " ha actualizado una marca de aceite " + existing.getName() + " de tipo " + existing.getType());


        return BrandMapper.toResponse(brandRepository.save(existing));
    }

    @Override
    public void deleteBrandById(Long id) {
        BrandEntity brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found with id " + id));

        if (!brand.isStatus()) {
            throw new RuntimeException("Brand already deleted with id " + id);
        }

        brand.setStatus(false); // soft delete
        brandRepository.save(brand);

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        saveActionUseCase.save("El usuario " + userPrincipal.username() + " ha  eliminado una marca de aceite " + brand.getName() + " de tipo " + brand.getType());

    }

    @Override
    public List<BrandResponse> getAllBrands() {

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        saveActionUseCase.save("El usuario " + userPrincipal.username() + " ha obtenido todas las marcas de aceite");

        return brandRepository.findByStatusTrue()
                .stream()
                .filter(BrandEntity::isStatus)
                .map(BrandMapper::toResponse)
                .toList();
    }

    @Override
    public List<BrandResponse> getAllBrandsByType(String type) {

        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        saveActionUseCase.save("El usuario " + userPrincipal.username() + " ha obtenido todas las marcas de aceite de tipo " + type);

        return brandRepository.findAllByType(type)
                .stream()
                .filter(BrandEntity::isStatus)
                .map(BrandMapper::toResponse)
                .toList();
    }

}