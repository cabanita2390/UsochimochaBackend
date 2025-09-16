package com.app.usochicamochabackend.review.web;

import com.app.usochicamochabackend.order.application.dto.OrderWithoutInspectionResponse;
import com.app.usochicamochabackend.review.application.dto.InspectionDTO;
import com.app.usochicamochabackend.review.application.dto.InspectionFormRequest;
import com.app.usochicamochabackend.review.application.dto.InspectionFormResponse;
import com.app.usochicamochabackend.review.application.dto.ImageDTO;
import com.app.usochicamochabackend.review.application.port.*;
import com.app.usochicamochabackend.user.application.dto.UserResponse;
import com.app.usochicamochabackend.machine.application.dto.MachineResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InspectionController.class)
class InspectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private CreateInspectionOnlyDataUseCase createInspectionOnlyDataUseCase;

    @Mock
    private GetAllInspectionsWithoutImagesUseCase getAllInspectionsWithoutImagesUseCase;

    @Mock
    private GetInspectionByIdUseCase getInspectionByIdUseCase;

    @Mock
    private GetInspectionImagesUseCase getInspectionImagesUseCase;

    @Mock
    private SaveInspectionImageUseCase saveInspectionImageUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createInspection_ShouldReturnCreatedInspection() throws Exception {
        // Given
        InspectionFormRequest request = new InspectionFormRequest(
                "test-uuid-123",
                false,
                LocalDateTime.now(),
                100.0,
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

        UserResponse userResponse = new UserResponse(1L, "Test User", "testuser", "test@example.com", "ADMIN");
        MachineResponse machineResponse = new MachineResponse(1L, "Test Machine", "Model X", "Test Company", LocalDate.now().plusMonths(6), "Test Brand", LocalDate.now().plusMonths(12), "ENG123", "ID123");

        InspectionFormResponse response = new InspectionFormResponse(
                1L,
                "test-uuid-123",
                false,
                LocalDateTime.now(),
                100.0,
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
                userResponse,
                machineResponse
        );

        when(createInspectionOnlyDataUseCase.createInspectionOnlyData(any(InspectionFormRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/inspection")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.UUID").value("test-uuid-123"))
                .andExpect(jsonPath("$.unexpected").value(false))
                .andExpect(jsonPath("$.leakStatus").value("GOOD"))
                .andExpect(jsonPath("$.observations").value("Test observations"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllInspections_ShouldReturnInspectionsList() throws Exception {
        // Given
        UserResponse userResponse = new UserResponse(1L, "Test User", "testuser", "test@example.com", "ADMIN");
        MachineResponse machineResponse = new MachineResponse(1L, "Test Machine", "Model X", "Test Company", LocalDate.now().plusMonths(6), "Test Brand", LocalDate.now().plusMonths(12), "ENG123", "ID123");

        List<InspectionFormResponse> inspections = Arrays.asList(
                new InspectionFormResponse(1L, "uuid-1", false, LocalDateTime.now(), 100.0, "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "2024-12-31", "Applied", "Greased", "Obs 1", userResponse, machineResponse),
                new InspectionFormResponse(2L, "uuid-2", true, LocalDateTime.now(), 150.0, "BAD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "GOOD", "2024-12-31", "Applied", "Greased", "Obs 2", userResponse, machineResponse)
        );
        Pageable pageable = PageRequest.of(0, 10);
        Page<InspectionFormResponse> inspectionPage = new PageImpl<>(inspections, pageable, inspections.size());
        when(getAllInspectionsWithoutImagesUseCase.getAllInspectionsWithoutImages(any(Pageable.class))).thenReturn(inspectionPage);

        // When & Then
        mockMvc.perform(get("/api/v1/inspection")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].UUID").value("uuid-1"))
                .andExpect(jsonPath("$.content[1].UUID").value("uuid-2"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getInspectionById_ShouldReturnInspection() throws Exception {
        // Given
        UserResponse userResponse = new UserResponse(1L, "Test User", "testuser", "test@example.com", "ADMIN");
        MachineResponse machineResponse = new MachineResponse(1L, "Test Machine", "Model X", "Test Company", LocalDate.now().plusMonths(6), "Test Brand", LocalDate.now().plusMonths(12), "ENG123", "ID123");

        List<ImageDTO> images = Arrays.asList(
                new ImageDTO(1L, "http://example.com/image1.jpg"),
                new ImageDTO(2L, "http://example.com/image2.jpg")
        );

        List<OrderWithoutInspectionResponse> orders = Arrays.asList(
                new OrderWithoutInspectionResponse(1L, "PENDING", LocalDateTime.now(), "Order 1", userResponse),
                new OrderWithoutInspectionResponse(2L, "COMPLETED", LocalDateTime.now(), "Order 2", userResponse)
        );

        InspectionDTO response = new InspectionDTO(
                1L,
                "test-uuid-123",
                false,
                LocalDateTime.now(),
                100.0,
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
                userResponse,
                machineResponse,
                images,
                orders
        );
        when(getInspectionByIdUseCase.getInspectionById(1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/inspection/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.UUID").value("test-uuid-123"))
                .andExpect(jsonPath("$.leakStatus").value("GOOD"))
                .andExpect(jsonPath("$.observations").value("Test observations"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getInspectionImages_ShouldReturnImagesList() throws Exception {
        // Given
        List<ImageDTO> images = Arrays.asList(
                new ImageDTO(1L, "http://example.com/image1.jpg"),
                new ImageDTO(2L, "http://example.com/image2.jpg")
        );
        when(getInspectionImagesUseCase.getAllImagesByInspectionId(1L)).thenReturn(images);

        // When & Then
        mockMvc.perform(get("/api/v1/inspection/1/images")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].url").value("http://example.com/image1.jpg"))
                .andExpect(jsonPath("$[1].url").value("http://example.com/image2.jpg"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void saveInspectionImage_ShouldReturnCreated() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/inspection/1/images")
                .with(csrf())
                .param("imageUrl", "http://example.com/new-image.jpg"))
                .andExpect(status().isCreated());
    }
}
