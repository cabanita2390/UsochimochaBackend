package com.app.usochicamochabackend.review.application.service;

import com.app.usochicamochabackend.review.application.dto.InspectionDTO;
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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

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
                "Applied",
                "All points greased",
                "Test observations",
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
        verify(notificationService).notify("inspections-updated");
        verify(notificationService).notify("actions-updated");
        verify(inspectionStreamController).publish(any(InspectionFormResponse.class));
    }

    @Test
    void saveInspectionImage_ShouldSaveImage_WhenInspectionExists() throws Exception {
        // Given
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(mockFile.getInputStream()).thenReturn(null); // For duplicate check

        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(testInspection));
        when(inspectionRepository.save(any(InspectionEntity.class))).thenReturn(testInspection);

        // When
        inspectionService.saveInspectionImage(1L, mockFile);

        // Then
        verify(inspectionRepository).findById(1L);
        verify(imageRepository).save(any(ImageEntity.class));
        verify(inspectionRepository).save(any(InspectionEntity.class));
        verify(saveActionUseCase).save(anyString());
        verify(notificationService).notify("inspections-updated");
        verify(notificationService).notify("actions-updated");
    }

    @Test
    void saveInspectionImage_ShouldThrowException_WhenInspectionNotFound() throws Exception {
        // Given
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getBytes()).thenReturn(new byte[]{1, 2, 3});

        when(inspectionRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class,
            () -> inspectionService.saveInspectionImage(999L, mockFile));
        verify(inspectionRepository).findById(999L);
        verify(imageRepository, never()).save(any(ImageEntity.class));
    }

    @Test
    void getInspectionById_ShouldReturnInspectionDTO_WhenInspectionExists() {
        // Given
        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(testInspection));

        // When
        InspectionDTO response = inspectionService.getInspectionById(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("test-uuid-123", response.UUID());
        assertEquals("GOOD", response.leakStatus());
        assertEquals("Test observations", response.observations());
        assertNotNull(response.user());
        assertNotNull(response.machine());
        assertNotNull(response.images());
        assertNotNull(response.orders());
        verify(inspectionRepository).findById(1L);
        verify(saveActionUseCase).save(anyString());
        verify(notificationService).notify("actions-updated");
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
        List<ImageDTO> imageDTOs = inspectionService.getAllImagesByInspectionId(1L);

        // Then
        assertNotNull(imageDTOs);
        assertEquals(2, imageDTOs.size());
        assertEquals("http://example.com/image1.jpg", imageDTOs.get(0).url());
        assertEquals("http://example.com/image2.jpg", imageDTOs.get(1).url());
        verify(imageRepository).findByInspectionId(1L);
    }

    @Test
    void getAllInspectionsWithoutImages_ShouldReturnPageOfInspections() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        InspectionEntity inspection2 = TestDataBuilder.createTestInspection(testMachine, testUser);
        inspection2.setId(2L);
        inspection2.setUUID("test-uuid-789");

        List<InspectionEntity> inspections = Arrays.asList(testInspection, inspection2);
        Page<InspectionEntity> inspectionPage = new PageImpl<>(inspections, pageable, inspections.size());
        when(inspectionRepository.findAll(pageable)).thenReturn(inspectionPage);

        // When
        Page<InspectionFormResponse> responses = inspectionService.getAllInspectionsWithoutImages(pageable);

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.getContent().size());
        assertEquals("test-uuid-123", responses.getContent().get(0).UUID());
        assertEquals("test-uuid-789", responses.getContent().get(1).UUID());
        verify(inspectionRepository).findAll(pageable);
        verify(saveActionUseCase).save(anyString());
        verify(notificationService).notify("actions-updated");
    }
}
