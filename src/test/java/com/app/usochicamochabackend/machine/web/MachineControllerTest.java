package com.app.usochicamochabackend.machine.web;

import com.app.usochicamochabackend.machine.application.dto.MachineRequest;
import com.app.usochicamochabackend.machine.application.dto.MachineResponse;
import com.app.usochicamochabackend.machine.application.port.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MachineController.class)
class MachineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private CreateMachineUseCase createMachineUseCase;

    @Mock
    private DeleteMachineUseCase deleteMachineUseCase;

    @Mock
    private FindAllMachinesUseCase findAllMachinesUseCase;

    @Mock
    private FindMachineByIdUseCase findMachineByIdUseCase;

    @Mock
    private UpdateMachineUseCase updateMachineUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createMachine_ShouldReturnCreatedMachine() throws Exception {
        // Given
        MachineRequest request = new MachineRequest(
                "Test Machine",
                "Model X",
                "Test Company",
                LocalDate.now().plusMonths(6),
                "Test Brand",
                LocalDate.now().plusMonths(12),
                "ENG123",
                "ID123"
        );
        
        MachineResponse response = new MachineResponse(
                1L,
                "Test Machine",
                "Model X",
                "Test Company",
                LocalDate.now().plusMonths(6),
                "Test Brand",
                LocalDate.now().plusMonths(12),
                "ENG123",
                "ID123"
        );

        when(createMachineUseCase.createMachine(any(MachineRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/machine")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Machine"))
                .andExpect(jsonPath("$.model").value("Model X"))
                .andExpect(jsonPath("$.belongsTo").value("Test Company"))
                .andExpect(jsonPath("$.brand").value("Test Brand"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllMachines_ShouldReturnMachinesList() throws Exception {
        // Given
        List<MachineResponse> machines = Arrays.asList(
                new MachineResponse(1L, "Machine 1", "Model X", "Company A", LocalDate.now().plusMonths(6), "Brand A", LocalDate.now().plusMonths(12), "ENG1", "ID1"),
                new MachineResponse(2L, "Machine 2", "Model Y", "Company B", LocalDate.now().plusMonths(3), "Brand B", LocalDate.now().plusMonths(9), "ENG2", "ID2")
        );
        when(findAllMachinesUseCase.findAllMachines()).thenReturn(machines);

        // When & Then
        mockMvc.perform(get("/api/v1/machine")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Machine 1"))
                .andExpect(jsonPath("$[1].name").value("Machine 2"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getMachineById_ShouldReturnMachine() throws Exception {
        // Given
        MachineResponse response = new MachineResponse(
                1L,
                "Test Machine",
                "Model X",
                "Test Company",
                LocalDate.now().plusMonths(6),
                "Test Brand",
                LocalDate.now().plusMonths(12),
                "ENG123",
                "ID123"
        );
        when(findMachineByIdUseCase.findMachineById(1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/machine/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Machine"))
                .andExpect(jsonPath("$.model").value("Model X"))
                .andExpect(jsonPath("$.belongsTo").value("Test Company"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateMachine_ShouldReturnUpdatedMachine() throws Exception {
        // Given
        MachineRequest request = new MachineRequest(
                "Updated Machine",
                "Updated Model",
                "Updated Company",
                LocalDate.now().plusMonths(8),
                "Updated Brand",
                LocalDate.now().plusMonths(14),
                "UPDENG",
                "UPDID"
        );
        
        MachineResponse response = new MachineResponse(
                1L,
                "Updated Machine",
                "Updated Model",
                "Updated Company",
                LocalDate.now().plusMonths(8),
                "Updated Brand",
                LocalDate.now().plusMonths(14),
                "UPDENG",
                "UPDID"
        );

        when(updateMachineUseCase.updateMachine(any(MachineRequest.class), eq(1L))).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/v1/machine/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Updated Machine"))
                .andExpect(jsonPath("$.model").value("Updated Model"))
                .andExpect(jsonPath("$.belongsTo").value("Updated Company"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteMachine_ShouldReturnNoContent() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/machine/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
