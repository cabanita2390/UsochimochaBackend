package com.app.usochicamochabackend.moto.application.service;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.moto.application.dto.*;
import com.app.usochicamochabackend.moto.infrastructure.entity.*;
import com.app.usochicamochabackend.moto.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MotoService {

        private final VehiculoRepository vehiculoRepository;
        private final UbicacionRepository ubicacionRepository;
        private final DocumentacionRepository documentacionRepository;
        private final MotoInspeccionRepository inspeccionRepository;
        private final InspDetalleDocumentosRepository detalleDocumentosRepository;
        private final TipoVehiculoRepository tipoVehiculoRepository;

        /** Retorna las motocicletas activas (tipo = MOTOCICLETA) */
        public List<MotoPlacaResponse> getMotocicletas() {
                return vehiculoRepository.findActivosByTipo("MOTOCICLETA")
                                .stream()
                                .map(v -> new MotoPlacaResponse(v.getId(), v.getPlaca()))
                                .toList();
        }

        /** Crea una nueva motocicleta con valores predeterminados simplificados */
        @Transactional
        public MotoPlacaResponse registrarNuevaPlaca(String placa) {
                // Limpia el input (elimina posibles comillas del payload JSON en texto plano)
                String plateClean = (placa != null) ? placa.replace("\"", "").trim().toUpperCase() : "";

                if (plateClean.isBlank()) {
                        throw new RuntimeException("La placa no puede estar vacía");
                }

                // Verifica si ya existe (activa)
                var existing = vehiculoRepository.findByPlacaAndActivoTrue(plateClean);
                if (existing.isPresent()) {
                        VehiculoEntity v = existing.get();
                        return new MotoPlacaResponse(v.getId(), v.getPlaca());
                }

                TipoVehiculoEntity tipoMoto = tipoVehiculoRepository.findByNombreTipo("MOTOCICLETA")
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Tipo Motocicleta no configurado en BD"));

                UbicacionEntity ubicacionDefault = ubicacionRepository.findById(1) // Generalmente la primera ubicación
                                .orElse(null);

                VehiculoEntity nuevo = VehiculoEntity.builder()
                                .placa(plateClean)
                                .tipoVehiculo(tipoMoto)
                                .ubicacion(ubicacionDefault)
                                .activo(true)
                                .estadoVehiculo("Operativo")
                                .kilometrajeActual(0)
                                .loginUser("admin") // Usuario del sistema / inicial
                                .build();

                VehiculoEntity guardado = vehiculoRepository.save(nuevo);
                return new MotoPlacaResponse(guardado.getId(), guardado.getPlaca());
        }

        /** Retorna todas las ubicaciones activas */
        public List<UbicacionResponse> getUbicaciones() {
                return ubicacionRepository.findByActivoTrue()
                                .stream()
                                .map(u -> new UbicacionResponse(u.getId(), u.getNombreUbicacion()))
                                .toList();
        }

        /**
         * Retorna la documentación existente de una motocicleta según su placa.
         * Usada para el pre-llenado automático en la app.
         */
        public List<DocumentoExistenteResponse> getDocumentosByPlaca(String placa) {
                VehiculoEntity vehiculo = vehiculoRepository.findByPlacaAndActivoTrue(placa)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Motocicleta no encontrada: " + placa));

                return documentacionRepository.findByIdVehiculoAndActivoTrue(vehiculo.getId())
                                .stream()
                                .map(d -> {
                                        // Rescate global para la vista previa de la App: si activo es null, busca en el
                                        // historial completo
                                        String finalUrl = d.getImagenUrl();
                                        if (finalUrl == null || finalUrl.isBlank()) {
                                                finalUrl = documentacionRepository
                                                                .findFirstByIdVehiculoAndTipoDocumentoAndImagenUrlIsNotNullOrderByIdDesc(
                                                                                vehiculo.getId(), d.getTipoDocumento())
                                                                .map(DocumentacionEntity::getImagenUrl)
                                                                .orElse(null);
                                        }

                                        return new DocumentoExistenteResponse(
                                                        d.getId(),
                                                        d.getTipoDocumento(),
                                                        d.getFechaVencimiento(),
                                                        d.getMesyear() != null
                                                                        ? String.format("%04d-%02d",
                                                                                        d.getMesyear().getYear(),
                                                                                        d.getMesyear().getMonthValue())
                                                                        : null,
                                                        finalUrl);
                                })
                                .toList();
        }

        /** Guarda la inspección pre-operativa completa de la motocicleta */
        @Transactional
        public Long saveInspeccion(InspeccionMotoRequest req) {
                // Obtiene el usuario autenticado desde el JWT
                UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                                .getAuthentication()
                                .getPrincipal();
                String responsable = userPrincipal.username();

                VehiculoEntity vehiculo = vehiculoRepository.findById(req.idVehiculo())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Vehículo no encontrado: " + req.idVehiculo()));

                // Actualiza el vehículo existente (Snapshot)
                // Siempre se actualiza para evitar el error 500 por duplicado de placa
                UbicacionEntity ubicacion = ubicacionRepository.findById(req.idUbicacion())
                                .orElseThrow(() -> new ResourceNotFoundException("Ubicación no encontrada"));

                vehiculo.setUbicacion(ubicacion);
                vehiculo.setKilometrajeActual(req.kilometrajeReportado());
                vehiculo.setEstadoVehiculo(req.estadoGeneral());
                vehiculo.setLoginUser(responsable);
                vehiculo.setFechaUltimoReporte(LocalDateTime.now());
                vehiculo = vehiculoRepository.save(vehiculo);

                // Truco de historial: se antepone la ubicación a las observaciones ya que no se
                // puede cambiar el esquema de BD
                String observacionesConHistorial = String.format("[Ubicación: %s] | %s",
                                ubicacion.getNombreUbicacion(),
                                req.observacionesFinales());

                // Guarda el registro principal de inspección vinculado al vehículo
                InspeccionEntity inspeccion = InspeccionEntity.builder()
                                .vehiculo(vehiculo)
                                .fechaRegistro(LocalDateTime.now())
                                .responsableInspeccion(responsable)
                                .kilometrajeReportado(req.kilometrajeReportado())
                                .aprobadoRuta(null)
                                .observacionesFinales(observacionesConHistorial)
                                .build();

                inspeccionRepository.saveAndFlush(inspeccion);

                // Inserta o actualiza documentos (SOAT, REVISION_TECNO, LICENCIA)
                // Los documentos se guardan primero para tener las fechas calculadas en BD
                saveOrUpdateDocumento(vehiculo.getId(), "SOAT", req.vigenciaSoat(), req.imagenSoat());
                saveOrUpdateDocumento(vehiculo.getId(), "REVISION_TECNO", req.vigenciaRevision(),
                                req.imagenRevision());
                saveOrUpdateDocumento(vehiculo.getId(), "LICENCIA", req.vigenciaLicencia(),
                                req.imagenLicencia());

                // Recalcula los estados para los detalles de inspección (ignora el input del
                // frontend por seguridad/consistencia)
                String estadoSoat = calcularEstadoActual(vehiculo.getId(), "SOAT");
                String estadoTecno = calcularEstadoActual(vehiculo.getId(), "REVISION_TECNO");
                String estadoLicencia = calcularEstadoActual(vehiculo.getId(), "LICENCIA");

                // Guarda los estados de verificación de documentos en insp_detalle_documentos
                InspDetalleDocumentosEntity detalleDoc = InspDetalleDocumentosEntity.builder()
                                .idInspeccion(inspeccion.getId())
                                .checkSoat(estadoSoat)
                                .checkTecno(estadoTecno)
                                .checkLicencia(estadoLicencia)
                                .build();
                detalleDocumentosRepository.save(detalleDoc);

                return inspeccion.getId();
        }

        private void saveOrUpdateDocumento(Integer idVehiculo, String tipo, String vigencia, String imagenUrl) {
                if (vigencia == null || vigencia.isBlank()) {
                        return;
                }

                // Obtiene el registro activo existente para rescatar la fecha de vencimiento
                // actual
                DocumentacionEntity existingActive = documentacionRepository.findByIdVehiculoAndActivoTrue(idVehiculo)
                                .stream()
                                .filter(d -> tipo.equals(d.getTipoDocumento()))
                                .findFirst()
                                .orElse(null);

                // Parsea la fecha (soporta formato YYYY-MM o YYYY-MM-DD)
                String[] parts = vigencia.split("-");
                LocalDate finalDate;

                if (parts.length == 3) {
                        // Fecha completa recibida desde campo bloqueado de la App → se conserva exacta
                        finalDate = LocalDate.parse(vigencia);
                } else if (parts.length == 2) {
                        // Año-Mes aproximado (inicial, renovación o próximo a vencer)
                        int targetYear = Integer.parseInt(parts[0]);
                        int targetMonth = Integer.parseInt(parts[1]);

                        // Requerimiento: siempre usar el día 1
                        finalDate = LocalDate.of(targetYear, targetMonth, 1);
                } else {
                        return;
                }

                // Desactiva los registros activos actuales (usando la lista ya obtenida si es
                // posible)
                if (existingActive != null) {
                        existingActive.setActivo(false);
                        documentacionRepository.save(existingActive);
                }

                // Busca la URL de imagen existente en el historial completo (rescate robusto)
                String existingImageUrl = documentacionRepository
                                .findFirstByIdVehiculoAndTipoDocumentoAndImagenUrlIsNotNullOrderByIdDesc(idVehiculo,
                                                tipo)
                                .map(DocumentacionEntity::getImagenUrl)
                                .orElse(null);

                // URL de imagen final
                String finalImageUrl = (imagenUrl != null && !imagenUrl.isBlank()) ? imagenUrl : existingImageUrl;

                // Siempre inserta un registro nuevo fresco
                DocumentacionEntity nuevo = DocumentacionEntity.builder()
                                .idVehiculo(idVehiculo)
                                .tipoDocumento(tipo)
                                .mesyear(finalDate)
                                .fechaVencimiento(finalDate)
                                .imagenUrl(finalImageUrl)
                                .activo(true)
                                .build();
                documentacionRepository.save(nuevo);
        }

        /**
         * Calcula si un documento está "Vigente", "Próximo a vencer" (dentro de 1 mes)
         * o
         * "Vencido"
         */
        private String calcularEstadoActual(Integer idVehiculo, String tipo) {
                return documentacionRepository.findByIdVehiculoAndActivoTrue(idVehiculo)
                                .stream()
                                .filter(d -> tipo.equals(d.getTipoDocumento()))
                                .findFirst()
                                .map(d -> {
                                        LocalDate vencimiento = d.getFechaVencimiento();
                                        if (vencimiento == null)
                                                return "Sin registro";

                                        LocalDate hoy = LocalDate.now();
                                        LocalDate proximoVencer = hoy.plusMonths(1);

                                        if (vencimiento.isBefore(hoy)) {
                                                return "Vencido";
                                        } else if (!vencimiento.isAfter(proximoVencer)) {
                                                // hoy <= vencimiento <= proximoVencer
                                                return "Próximo a vencer";
                                        } else {
                                                return "Vigente";
                                        }
                                })
                                .orElse("Vencido"); // Por defecto si no se encuentra registro
        }
}
