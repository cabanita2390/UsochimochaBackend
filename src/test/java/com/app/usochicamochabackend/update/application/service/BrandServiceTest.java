package com.app.usochicamochabackend.update.application.service;

import com.app.usochicamochabackend.update.application.dto.BrandRequest;
import com.app.usochicamochabackend.update.application.dto.BrandResponse;
import com.app.usochicamochabackend.update.infrastructure.entity.BrandEntity;
import com.app.usochicamochabackend.update.infrastructure.repository.BrandRepository;
import com.app.usochicamochabackend.actions.application.port.SaveActionUseCase;
import com.app.usochicamochabackend.notifications.application.NotificationService;
import com.app.usochicamochabackend.utils.TestDataBuilder;
import com.app.usochicamochabackend.utils.TestSecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private SaveActionUseCase saveActionUseCase;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BrandService brandService;

    private BrandEntity testBrand;

    @BeforeEach
    void setUp() {
        testBrand = TestDataBuilder.createTestBrand();
        TestSecurityUtils.setUpSecurityContext(1L, "testuser", "ADMIN");
    }

    @AfterEach
    void tearDown() {
        TestSecurityUtils.clearSecurityContext();
    }

    @Test
    void createBrand_ShouldReturnBrandResponse_WhenBrandIsCreated() {
        // Given
        BrandRequest request = new BrandRequest("HYDRAULIC", "New Brand");
        BrandEntity savedBrand = BrandEntity.builder()
                .id(2L)
                .type("HYDRAULIC")
                .name("New Brand")
                .status(true)
                .build();

        when(brandRepository.save(any(BrandEntity.class))).thenReturn(savedBrand);

        // When
        BrandResponse response = brandService.createBrand(request);

        // Then
        assertNotNull(response);
        assertEquals("HYDRAULIC", response.type());
        assertEquals("New Brand", response.name());

        verify(brandRepository).save(any(BrandEntity.class));
        verify(saveActionUseCase).save(anyString());
    }

    @Test
    void getBrandById_ShouldReturnBrandResponse_WhenBrandExists() {
        // Given
        when(brandRepository.findById(1L)).thenReturn(Optional.of(testBrand));

        // When
        BrandResponse response = brandService.getBrandById(1L);

        // Then
        assertNotNull(response);
        assertEquals("OIL", response.type());
        assertEquals("Test Brand", response.name());
        verify(brandRepository).findById(1L);
    }

    @Test
    void getBrandById_ShouldThrowException_WhenBrandNotFound() {
        // Given
        when(brandRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> brandService.getBrandById(999L));
        verify(brandRepository).findById(999L);
    }

    @Test
    void updateBrand_ShouldReturnUpdatedBrandResponse() {
        // Given
        BrandRequest request = new BrandRequest("MOTOR", "Updated Brand");
        BrandEntity updatedBrand = BrandEntity.builder()
                .id(1L)
                .type("MOTOR")
                .name("Updated Brand")
                .status(true)
                .build();

        when(brandRepository.findById(1L)).thenReturn(Optional.of(testBrand));
        when(brandRepository.save(any(BrandEntity.class))).thenReturn(updatedBrand);

        // When
        BrandResponse response = brandService.updateBrandById(1L, request);

        // Then
        assertNotNull(response);
        assertEquals("MOTOR", response.type());
        assertEquals("Updated Brand", response.name());

        verify(brandRepository).findById(1L);
        verify(brandRepository).save(any(BrandEntity.class));
        verify(saveActionUseCase).save(anyString());
        verify(notificationService, times(2)).notify(anyString());
    }

    @Test
    void deleteBrand_ShouldSetStatusToFalse_WhenBrandExists() {
        // Given
        when(brandRepository.findById(1L)).thenReturn(Optional.of(testBrand));

        // When
        brandService.deleteBrandById(1L);

        // Then
        verify(brandRepository).findById(1L);
        verify(brandRepository).save(any(BrandEntity.class));
        verify(saveActionUseCase).save(anyString());
        verify(notificationService, times(2)).notify(anyString());
    }

    @Test
    void getAllBrands_ShouldReturnListOfBrands() {
        // Given
        BrandEntity brand2 = BrandEntity.builder()
                .id(2L)
                .type("HYDRAULIC")
                .name("Brand 2")
                .status(true)
                .build();

        List<BrandEntity> brands = Arrays.asList(testBrand, brand2);
        when(brandRepository.findByStatusTrue()).thenReturn(brands);

        // When
        List<BrandResponse> responses = brandService.getAllBrands();

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Test Brand", responses.get(0).name());
        assertEquals("Brand 2", responses.get(1).name());
        verify(brandRepository).findByStatusTrue();
    }

    @Test
    void getAllBrandsByType_ShouldReturnBrandsOfSpecificType() {
        // Given
        String type = "OIL";
        List<BrandEntity> oilBrands = Arrays.asList(testBrand);
        when(brandRepository.findAllByType(type)).thenReturn(oilBrands);

        // When
        List<BrandResponse> responses = brandService.getAllBrandsByType(type);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Test Brand", responses.get(0).name());
        assertEquals("OIL", responses.get(0).type());
        verify(brandRepository).findAllByType(type);
    }

}
