package com.app.usochicamochabackend.update.web;

import com.app.usochicamochabackend.update.application.dto.BrandRequest;
import com.app.usochicamochabackend.update.application.dto.BrandResponse;
import com.app.usochicamochabackend.update.application.port.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BrandController.class)
class BrandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private CreateBrandUseCase createBrand;

    @Mock
    private GetBrandByIdUseCase getBrandById;

    @Mock
    private UpdateBrandUseCase updateBrand;

    @Mock
    private DeleteBrandUseCase deleteBrand;

    @Mock
    private GetAllBrandsUseCase getAllBrands;

    @Mock
    private GetAllBrandsByTypeUseCase getAllBrandsByType;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createBrand_ShouldReturnCreatedBrand() throws Exception {
        // Given
        BrandRequest request = new BrandRequest("OIL", "Test Brand");
        BrandResponse response = new BrandResponse(1L, "OIL", "Test Brand");
        when(createBrand.createBrand(any(BrandRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/oil/brand")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("OIL"))
                .andExpect(jsonPath("$.name").value("Test Brand"))
                .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getBrandById_ShouldReturnBrand() throws Exception {
        // Given
        BrandResponse response = new BrandResponse(1L, "OIL", "Test Brand");
        when(getBrandById.getBrandById(1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/oil/brand/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("OIL"))
                .andExpect(jsonPath("$.name").value("Test Brand"))
                .andExpect(jsonPath("$.status").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateBrand_ShouldReturnUpdatedBrand() throws Exception {
        // Given
        BrandRequest request = new BrandRequest("HYDRAULIC", "Updated Brand");
        BrandResponse response = new BrandResponse(1L, "HYDRAULIC", "Updated Brand");
        when(updateBrand.updateBrandById(eq(1L), any(BrandRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/v1/oil/brand/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("HYDRAULIC"))
                .andExpect(jsonPath("$.name").value("Updated Brand"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteBrand_ShouldReturnNoContent() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/oil/brand/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllBrands_ShouldReturnBrandsList() throws Exception {
        // Given
        List<BrandResponse> brands = Arrays.asList(
                new BrandResponse(1L, "OIL", "Brand 1"),
                new BrandResponse(2L, "HYDRAULIC", "Brand 2")
        );
        when(getAllBrands.getAllBrands()).thenReturn(brands);

        // When & Then
        mockMvc.perform(get("/api/v1/oil/brand")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Brand 1"))
                .andExpect(jsonPath("$[1].name").value("Brand 2"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllBrandsByType_ShouldReturnBrandsOfSpecificType() throws Exception {
        // Given
        List<BrandResponse> oilBrands = Arrays.asList(
                new BrandResponse(1L, "OIL", "Oil Brand 1"),
                new BrandResponse(2L, "OIL", "Oil Brand 2")
        );
        when(getAllBrandsByType.getAllBrandsByType("OIL")).thenReturn(oilBrands);

        // When & Then
        mockMvc.perform(get("/api/v1/oil/brand/type/OIL")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("OIL"))
                .andExpect(jsonPath("$[1].type").value("OIL"));
    }
}
