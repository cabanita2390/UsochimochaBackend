package com.app.usochicamochabackend.review.application.service;

import com.app.usochicamochabackend.review.application.dto.InspectionFormRequest;
import com.app.usochicamochabackend.review.application.dto.InspectionFormResponse;
import com.app.usochicamochabackend.review.application.dto.ImageDTO;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import com.app.usochicamochabackend.review.infrastructure.entity.ImageEntity;
import com.app.usochicamochabackend.review.infrastructure.repository.InspectionRepository;
import com.app.usochicamochabackend.review.infrastructure.repository.ImageRepository;
import com.app.usochicamochabackend.review.web.InspectionStreamController;
import com.app.usochicamochabackend.review.web.SoatRuntStreamController;
import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import com.app.usochicamochabackend.machine.infrastructure.repository.MachineRepository;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InspectionServiceTest {

    @Mock
    private InspectionStreamController inspectionStreamController;

    @Mock
    private SoatRuntStreamController notificationStreamController;

    @Mock
    private NotificationService notificationService;

    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private UserRepositoryJpa userRepository;

    @Mock
    private MachineRepository machineRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private SaveActionUseCase saveActionUseCase;

    @InjectMocks
    private InspectionService inspectionService;

    private UserEntity testUser;
    private MachineEntity testMachine;
    private InspectionEntity testInspection;

    @BeforeEach
    void setUp() {
        testUser = TestDataBuilder.createTestUser();
        testMachine = TestDataBuilder.createTestMachine();
        testInspection = TestDataBuilder.createTestInspection(testMachine, testUser);
        TestSecurityUtils.setUpSecurityContext(1L, "testuser", "ADMIN");
    }

    @AfterEach
    void tearDown() {
        TestSecurityUtils.clearSecurityContext();
    }

    @Test
    void createInspectionOnlyData_ShouldReturnInspectionFormResponse_WhenInspectionIsCreated() {
        // Given
        InspectionFormRequest request = new InspectionFormRequest(
                "test-uuid-456",
                false,
                LocalDateTime.now(),
                150.0,
                "GOOD",
                "GOOD",
                "GOOD",
                "GOOD",
                "GOOD",
                "GOOD",
                "GOOD",
                "GOOD",
                "GOOD",
                "GOOD",
                "GOOD",
                "GOOD",
                "2024-12-31",
                "Test observations",
                "Applied",
                "All points greased",
                1L,
                1L
        );

        InspectionEntity savedInspection = TestDataBuilder.createTestInspection(testMachine, testUser);
        savedInspection.setId(2L);
        savedInspection.setUUID("test-uuid-456");

        when(userRepository.getUserEntityById(1L)).thenReturn(testUser);
        when(machineRepository.findById(1L)).thenReturn(Optional.of(testMachine));
        when(inspectionRepository.save(any(InspectionEntity.class))).thenReturn(savedInspection);

        // When
        InspectionFormResponse response = inspectionService.createInspectionOnlyData(request);

        // Then
        assertNotNull(response);
        assertEquals("test-uuid-456", response.UUID());
        assertFalse(response.isUnexpected());
        assertEquals("GOOD", response.leakStatus());

        verify(userRepository).getUserEntityById(1L);
        verify(machineRepository).findById(1L);
        verify(inspectionRepository).save(any(InspectionEntity.class));
        verify(saveActionUseCase).save(anyString());
        verify(notificationService).notify(anyString());
        verify(inspectionStreamController).publish(any(InspectionFormResponse.class));
    }

    @Test
    void saveInspectionImage_ShouldSaveImage_WhenInspectionExists() {
        // Given
        String imageUrl = "http://example.com/image.jpg";
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(testInspection));

        // When
        inspectionService.saveInspectionImage(1L, imageUrl);

        // Then
        verify(inspectionRepository).findById(1L);
        verify(imageRepository).save(any(ImageEntity.class));
        verify(saveActionUseCase).save(anyString());
        verify(notificationService).notify(anyString());
    }

    @Test
    void saveInspectionImage_ShouldThrowException_WhenInspectionNotFound() {
        // Given
        when(inspectionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, 
            () -> inspectionService.saveInspectionImage(999L, "http://example.com/image.jpg"));
        verify(inspectionRepository).findById(999L);
        verify(imageRepository, never()).save(any(ImageEntity.class));
    }

    @Test
    void getInspectionById_ShouldReturnInspectionFormResponse_WhenInspectionExists() {
        // Given
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(testInspection));

        // When
        InspectionFormResponse response = inspectionService.getInspectionById(1L);

        // Then
        assertNotNull(response);
        assertEquals("test-uuid-123", response.UUID());
        assertEquals("GOOD", response.leakStatus());
        assertEquals("Test observations", response.observations());
        verify(inspectionRepository).findById(1L);
    }

    @Test
    void getInspectionById_ShouldThrowException_WhenInspectionNotFound() {
        // Given
        when(inspectionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> inspectionService.getInspectionById(999L));
        verify(inspectionRepository).findById(999L);
    }

    @Test
    void getInspectionImages_ShouldReturnListOfImages_WhenImagesExist() {
        // Given
        List<ImageEntity> images = Arrays.asList(
                new ImageEntity(1L, "http://example.com/image1.jpg", testInspection),
                new ImageEntity(2L, "http://example.com/image2.jpg", testInspection)
        );
        when(imageRepository.findByInspectionId(1L)).thenReturn(images);

        // When
        List<ImageDTO> imageDTOs = inspectionService.getInspectionImages(1L);

        // Then
        assertNotNull(imageDTOs);
        assertEquals(2, imageDTOs.size());
        assertEquals("http://example.com/image1.jpg", imageDTOs.get(0).url());
        assertEquals("http://example.com/image2.jpg", imageDTOs.get(1).url());
        verify(imageRepository).findByInspectionId(1L);
    }

    @Test
    void getAllInspectionsWithoutImages_ShouldReturnListOfInspections() {
        // Given
        InspectionEntity inspection2 = TestDataBuilder.createTestInspection(testMachine, testUser);
        inspection2.setId(2L);
        inspection2.setUUID("test-uuid-789");

        List<InspectionEntity> inspections = Arrays.asList(testInspection, inspection2);
        when(inspectionRepository.findAll()).thenReturn(inspections);

        // When
        List<InspectionFormResponse> responses = inspectionService.getAllInspectionsWithoutImages();

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("test-uuid-123", responses.get(0).UUID());
        assertEquals("test-uuid-789", responses.get(1).UUID());
        verify(inspectionRepository).findAll();
    }
}
