package com.app.usochicamochabackend.tire.application.service;

import com.app.usochicamochabackend.tire.application.dto.LecturaPresionLlantaRequest;
import com.app.usochicamochabackend.tire.application.dto.LecturaPresionLlantaResponse;
import com.app.usochicamochabackend.tire.application.dto.PosicionLlantaResponse;
import com.app.usochicamochabackend.tire.application.port.PresionLlantasInspeccionUseCase;
import com.app.usochicamochabackend.tire.infrastructure.entity.AssetLlantaConfigEntity;
import com.app.usochicamochabackend.tire.infrastructure.entity.InspPresionLlantaEntity;
import com.app.usochicamochabackend.tire.infrastructure.repository.AssetLlantaConfigRepository;
import com.app.usochicamochabackend.tire.infrastructure.repository.InspPresionLlantaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresionLlantasService implements PresionLlantasInspeccionUseCase {

    // Lecturas fuera de esto son errores de digitación: se omiten en vez de tumbar la
    // sincronización de una inspección hecha sin conexión.
    private static final BigDecimal PSI_MAXIMO_LECTURA = new BigDecimal("300");
    // Postgres limita los parámetros por sentencia; los reportes pueden traer miles de inspecciones.
    private static final int TAMANO_LOTE = 1000;

    private final AssetLlantaConfigRepository assetConfigRepository;
    private final InspPresionLlantaRepository lecturaRepository;

    @Override
    @Transactional
    public List<LecturaPresionLlantaResponse> registrarParaInspeccionVehiculo(
            Integer vehicleId, Long idInspeccion, List<LecturaPresionLlantaRequest> lecturas) {
        Optional<AssetLlantaConfigEntity> config = assetConfigRepository.findByVehicleId(vehicleId);
        return registrar(config, lecturas, e -> e.setIdInspeccionVehiculo(idInspeccion));
    }

    @Override
    @Transactional
    public List<LecturaPresionLlantaResponse> registrarParaInspeccionMaquina(
            Long machineId, Long idInspeccion, List<LecturaPresionLlantaRequest> lecturas) {
        // Si la inspección ya llegó antes (reintento del móvil) no se duplican sus lecturas.
        if (lecturaRepository.existsByIdInspeccionMaquina(idInspeccion)) {
            return List.of();
        }
        Optional<AssetLlantaConfigEntity> config = assetConfigRepository.findByMachineId(machineId);
        return registrar(config, lecturas, e -> e.setIdInspeccionMaquina(idInspeccion));
    }

    private List<LecturaPresionLlantaResponse> registrar(Optional<AssetLlantaConfigEntity> config,
                                                         List<LecturaPresionLlantaRequest> lecturas,
                                                         Consumer<InspPresionLlantaEntity> ligarInspeccion) {
        if (lecturas == null || lecturas.isEmpty()) {
            return List.of();
        }
        Map<String, PosicionLlantaResponse> posiciones = config
                .filter(c -> Boolean.TRUE.equals(c.getStatus()) && Boolean.TRUE.equals(c.getPerfil().getStatus()))
                .map(c -> PerfilLlantasService.posicionesVigentes(c.getPerfil()).stream()
                        .collect(Collectors.toMap(PosicionLlantaResponse::codigo, Function.identity())))
                .orElse(Map.of());

        LocalDateTime ahora = LocalDateTime.now();
        Set<String> vistos = new HashSet<>();
        List<InspPresionLlantaEntity> entidades = new ArrayList<>();
        for (LecturaPresionLlantaRequest lectura : lecturas) {
            InspPresionLlantaEntity entidad = construir(lectura, posiciones, vistos, ahora);
            if (entidad != null) {
                ligarInspeccion.accept(entidad);
                entidades.add(entidad);
            }
        }
        return lecturaRepository.saveAll(entidades).stream().map(PresionLlantasService::toResponse).toList();
    }

    private static InspPresionLlantaEntity construir(LecturaPresionLlantaRequest lectura,
                                                     Map<String, PosicionLlantaResponse> posiciones,
                                                     Set<String> vistos, LocalDateTime ahora) {
        if (lectura == null || lectura.codigoPosicion() == null || lectura.codigoPosicion().isBlank()) {
            log.warn("[PresionLlantas] Lectura sin posición omitida");
            return null;
        }
        String codigo = lectura.codigoPosicion().trim().toUpperCase(Locale.ROOT);
        BigDecimal psi = lectura.psi();
        if (psi == null || psi.signum() <= 0 || psi.compareTo(PSI_MAXIMO_LECTURA) > 0) {
            log.warn("[PresionLlantas] Lectura inválida omitida: posición={} psi={}", codigo, psi);
            return null;
        }
        if (!vistos.add(codigo)) {
            log.warn("[PresionLlantas] Posición repetida omitida: {}", codigo);
            return null;
        }
        psi = psi.setScale(1, RoundingMode.HALF_UP);

        PosicionLlantaResponse posicion = posiciones.get(codigo);
        String etiqueta;
        Integer orden;
        BigDecimal min;
        BigDecimal max;
        if (posicion != null) {
            etiqueta = posicion.etiqueta();
            orden = posicion.orden();
            min = posicion.psiMin();
            max = posicion.psiMax();
        } else {
            // La posición ya no está en el perfil vigente (el móvil tenía otra configuración en
            // caché): se conserva la lectura con el rango que vio el operario, si es coherente.
            etiqueta = lectura.etiquetaPosicion() != null && !lectura.etiquetaPosicion().isBlank()
                    ? truncar(lectura.etiquetaPosicion().trim(), 80)
                    : codigo;
            orden = null;
            boolean rangoValido = lectura.psiMinReferencia() != null && lectura.psiMaxReferencia() != null
                    && lectura.psiMinReferencia().signum() > 0
                    && lectura.psiMinReferencia().compareTo(lectura.psiMaxReferencia()) <= 0
                    && lectura.psiMaxReferencia().compareTo(PSI_MAXIMO_LECTURA) <= 0;
            min = rangoValido ? lectura.psiMinReferencia() : null;
            max = rangoValido ? lectura.psiMaxReferencia() : null;
        }

        return InspPresionLlantaEntity.builder()
                .codigoPosicion(truncar(codigo, 30))
                .etiquetaPosicion(etiqueta)
                .orden(orden)
                .psiMedido(psi)
                .psiMin(min)
                .psiMax(max)
                .resultado(EvaluadorPresionLlanta.evaluar(psi, min, max).name())
                .creadoEn(ahora)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<LecturaPresionLlantaResponse>> lecturasPorInspeccionVehiculo(Collection<Long> idsInspeccion) {
        return agrupar(idsInspeccion, lecturaRepository::findByIdInspeccionVehiculoInOrderByOrdenAsc,
                InspPresionLlantaEntity::getIdInspeccionVehiculo);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<LecturaPresionLlantaResponse>> lecturasPorInspeccionMaquina(Collection<Long> idsInspeccion) {
        return agrupar(idsInspeccion, lecturaRepository::findByIdInspeccionMaquinaInOrderByOrdenAsc,
                InspPresionLlantaEntity::getIdInspeccionMaquina);
    }

    @Override
    public String resumen(List<LecturaPresionLlantaResponse> lecturas) {
        if (lecturas == null || lecturas.isEmpty()) {
            return null;
        }
        ResultadoPresionLlanta r = EvaluadorPresionLlanta.resumir(lecturas.stream()
                .map(l -> ResultadoPresionLlanta.valueOf(l.resultado()))
                .toList());
        return r != null ? r.name() : null;
    }

    private static Map<Long, List<LecturaPresionLlantaResponse>> agrupar(
            Collection<Long> ids,
            Function<Collection<Long>, List<InspPresionLlantaEntity>> consulta,
            Function<InspPresionLlantaEntity, Long> clave) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        List<Long> distintos = ids.stream().filter(Objects::nonNull).distinct().toList();
        Map<Long, List<LecturaPresionLlantaResponse>> resultado = new HashMap<>();
        for (int i = 0; i < distintos.size(); i += TAMANO_LOTE) {
            List<Long> lote = distintos.subList(i, Math.min(i + TAMANO_LOTE, distintos.size()));
            for (InspPresionLlantaEntity e : consulta.apply(lote)) {
                resultado.computeIfAbsent(clave.apply(e), k -> new ArrayList<>()).add(toResponse(e));
            }
        }
        return resultado;
    }

    private static LecturaPresionLlantaResponse toResponse(InspPresionLlantaEntity e) {
        return new LecturaPresionLlantaResponse(e.getCodigoPosicion(), e.getEtiquetaPosicion(), e.getOrden(),
                e.getPsiMedido(), e.getPsiMin(), e.getPsiMax(), e.getResultado());
    }

    private static String truncar(String s, int max) {
        return s.length() > max ? s.substring(0, max) : s;
    }
}
