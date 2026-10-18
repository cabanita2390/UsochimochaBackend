package com.app.usochicamochabackend.substation.application.port;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.substation.application.dto.ComentarioOrdenRequest;
import com.app.usochicamochabackend.substation.application.dto.CostoOrdenRequest;
import com.app.usochicamochabackend.substation.application.dto.CrearOrdenRequest;
import com.app.usochicamochabackend.substation.application.dto.EditarOrdenRequest;
import com.app.usochicamochabackend.substation.application.dto.OperarioResponse;
import com.app.usochicamochabackend.substation.application.dto.OrdenMovilResponse;
import com.app.usochicamochabackend.substation.application.dto.OrdenDetalleResponse;
import com.app.usochicamochabackend.substation.application.dto.OrdenResumenResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Órdenes de trabajo de estación (OT-EB) para corregir hallazgos. Las crean, editan, verifican y
 * anulan SUPERVISOR_OPERATIVO y ADMIN; el OPERARIO solo las ejecuta desde el móvil.
 */
public interface SubstationOrdenUseCase {

    OrdenResumenResponse crear(CrearOrdenRequest request, UserPrincipal usuario);

    Page<OrdenResumenResponse> listar(List<String> estados, Long estacionId, String prioridad, Boolean vencidas,
            String disciplina, Pageable pageable);

    OrdenDetalleResponse detalle(Long id);

    OrdenResumenResponse editar(Long id, EditarOrdenRequest request, UserPrincipal usuario);

    OrdenResumenResponse anular(Long id, ComentarioOrdenRequest request, UserPrincipal usuario);

    /** Verificada: POR_VERIFICAR → CERRADA y el hallazgo queda RESUELTO en la visita de la orden. */
    OrdenResumenResponse cerrar(Long id, ComentarioOrdenRequest request, UserPrincipal usuario);

    /** No quedó bien: POR_VERIFICAR → ASIGNADA, vuelve a la lista del operario con el motivo. */
    OrdenResumenResponse devolver(Long id, ComentarioOrdenRequest request, UserPrincipal usuario);

    /** Reemplaza los costos (mano de obra y materiales). Se pueden corregir aun cerrada; no en ANULADA. */
    OrdenDetalleResponse guardarCostos(Long id, List<CostoOrdenRequest> costos, UserPrincipal usuario);

    List<OperarioResponse> operarios();

    /** Órdenes ASIGNADA del operario (lista "Prioritarias" del móvil), primero las de prioridad alta. */
    List<OrdenMovilResponse> asignadas(UserPrincipal usuario);
}
