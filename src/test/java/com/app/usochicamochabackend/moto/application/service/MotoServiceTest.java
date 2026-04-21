package com.app.usochicamochabackend.moto.application.service;

import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.moto.application.dto.*;
import com.app.usochicamochabackend.moto.infrastructure.entity.*;
import com.app.usochicamochabackend.moto.infrastructure.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MotoServiceTest {

    @Mock
    private VehiculoRepository vehiculoRepository;
    @Mock
    private UbicacionRepository ubicacionRepository;
    @Mock
    private MotoInspeccionRepository inspeccionRepository;
    @Mock
    private InspDetalleDocumentosRepository detalleDocumentosRepository;
    @Mock
    private DocumentacionRepository documentacionRepository;

    @InjectMocks
    private MotoService motoService;

    private VehiculoEntity mockVehiculo;

    @BeforeEach
    void setUp() {
        mockVehiculo = new VehiculoEntity();
        mockVehiculo.setId(1);
        mockVehiculo.setPlaca("MOTO123");
        mockVehiculo.setKilometrajeActual(1000);
        mockVehiculo.setActivo(true);
    }

    @Test
    void getMotocicletas_ShouldReturnList() {
        when(vehiculoRepository.findActivosByTipo("MOTOCICLETA"))
                .thenReturn(List.of(mockVehiculo));

        List<MotoPlacaResponse> result = motoService.getMotocicletas();

        assertEquals(1, result.size());
        assertEquals("MOTO123", result.get(0).placa());
        verify(vehiculoRepository).findActivosByTipo("MOTOCICLETA");
    }

    @Test
    void getUbicaciones_ShouldReturnList() {
        UbicacionEntity ubicacion = new UbicacionEntity();
        ubicacion.setId(1);
        ubicacion.setNombreUbicacion("Patio Central");
        ubicacion.setActivo(true);

        when(ubicacionRepository.findByActivoTrue()).thenReturn(List.of(ubicacion));

        List<UbicacionResponse> result = motoService.getUbicaciones();

        assertEquals(1, result.size());
        assertEquals("Patio Central", result.get(0).nombreUbicacion());
    }

    @Test
    void getDocumentosByPlaca_ShouldCalculateCorrectStatus() {
        when(vehiculoRepository.findByPlacaAndActivoTrue("MOTO123"))
                .thenReturn(Optional.of(mockVehiculo));

        DocumentacionEntity docSoat = new DocumentacionEntity();
        docSoat.setId(1);
        docSoat.setFechaVencimiento(LocalDate.now().plusMonths(5)); // Vigente
        docSoat.setImagenUrl("soat.jpg");

        when(documentacionRepository.findLatestByVehiculoAndTipo(anyInt(), eq("SOAT")))
                .thenReturn(Optional.of(docSoat));
        when(documentacionRepository.findLatestByVehiculoAndTipo(anyInt(), eq("TECNOMECANICA")))
                .thenReturn(Optional.empty()); // Sin Información

        List<DocumentoExistenteResponse> result = motoService.getDocumentosByPlaca("MOTO123");

        assertEquals(3, result.size()); // SOAT, TECNO, LICENCIA
        
        DocumentoExistenteResponse soat = result.stream()
                .filter(d -> d.tipoDocumento().equals("SOAT")).findFirst().get();
        assertEquals("Vigente", soat.estadoCheck());
        assertTrue(soat.imagenUrl().contains("soat.jpg"));

        DocumentoExistenteResponse tecno = result.stream()
                .filter(d -> d.tipoDocumento().equals("REVISION_TECNO")).findFirst().get();
        assertEquals("Sin Información", tecno.estadoCheck());
    }

    @Test
    void getDocumentosByPlaca_NotFound_ShouldThrowException() {
        when(vehiculoRepository.findByPlacaAndActivoTrue("UNKNOWN"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> 
            motoService.getDocumentosByPlaca("UNKNOWN")
        );
    }
}
