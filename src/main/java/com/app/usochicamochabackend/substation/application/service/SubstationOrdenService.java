package com.app.usochicamochabackend.substation.application.service;

import com.app.usochicamochabackend.actions.application.port.SaveActionUseCase;
import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.exception.BadRequestException;
import com.app.usochicamochabackend.exception.ResourceNotFoundException;
import com.app.usochicamochabackend.order.infrastructure.entity.OrderCounterEntity;
import com.app.usochicamochabackend.order.infrastructure.repository.OrderCounterRepository;
import com.app.usochicamochabackend.substation.application.dto.ComentarioOrdenRequest;
import com.app.usochicamochabackend.substation.application.dto.CrearOrdenRequest;
import com.app.usochicamochabackend.substation.application.dto.EditarOrdenRequest;
import com.app.usochicamochabackend.substation.application.dto.EvidenciaResponse;
import com.app.usochicamochabackend.substation.application.dto.OperarioResponse;
import com.app.usochicamochabackend.substation.application.dto.OrdenDetalleResponse;
import com.app.usochicamochabackend.substation.application.dto.OrdenEventoResponse;
import com.app.usochicamochabackend.substation.application.dto.OrdenMovilResponse;
import com.app.usochicamochabackend.substation.application.dto.OrdenResumenResponse;
import com.app.usochicamochabackend.substation.application.port.SubstationEjecucionUseCase;
import com.app.usochicamochabackend.substation.application.port.SubstationEventosPort;
import com.app.usochicamochabackend.substation.application.port.SubstationOrdenUseCase;
import com.app.usochicamochabackend.substation.infrastructure.entity.ActividadEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.EjecucionEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.HallazgoSeguimientoEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.OrdenEventoEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.OrdenTrabajoEntity;
import com.app.usochicamochabackend.substation.infrastructure.repository.ActividadRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.EjecucionRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.EvidenciaRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.HallazgoSeguimientoRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.OrdenEventoRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.OrdenTrabajoRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.OrdenTrabajoSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SubstationOrdenService implements SubstationOrdenUseCase {

    /** Código del consecutivo en order_counters: OT-EB-00001 (EB = estaciones de bombeo). */
    static final String MODULO = "EB";
    private static final Set<String> PRIORIDADES = Set.of("ALTA", "MEDIA");
    private static final int MAX_TEXTO = 2000;
    private static final int MAX_DIAS_PLAZO = 365;
    private static final DateTimeFormatter DD_MM = DateTimeFormatter.ofPattern("dd/MM/yyyy");


    private final OrdenTrabajoRepository ordenRepository;
    private final OrdenEventoRepository eventoRepository;
    private final EvidenciaRepository evidenciaRepository;
    private final EjecucionRepository ejecucionRepository;
    private final HallazgoSeguimientoRepository seguimientoRepository;
    private final ActividadRepository actividadRepository;
    private final UserRepositoryJpa userRepositoryJpa;
    private final OrderCounterRepository orderCounterRepository;
    private final SubstationEjecucionUseCase ejecucionUseCase;
    private final SaveActionUseCase saveActionUseCase;
    private final CalendarioMantenimiento calendario;
    private final SubstationEventosPort eventos;

    @Override
    @Transactional
    public OrdenResumenResponse crear(CrearOrdenRequest request, UserPrincipal usuario) {
        if (request == null || request.ejecucionOrigenId() == null) {
            throw new BadRequestException("ejecucionOrigenId es obligatorio.");
        }
        EjecucionEntity hallazgo = ejecucionRepository.findById(request.ejecucionOrigenId())
                .orElseThrow(() -> new ResourceNotFoundException("Ejecución no encontrada: id=" + request.ejecucionOrigenId()));
        HallazgoSeguimientoEntity seguimiento = seguimientoRepository.findByEjecucion_Id(hallazgo.getId())
                .filter(HallazgoSeguimientoEntity::getStatus)
                .orElseThrow(() -> conflicto("La ejecución #" + hallazgo.getId() + " es CONFORME: no tiene hallazgo que corregir."));
        if ("RESUELTO".equals(seguimiento.getEstado())) {
            throw conflicto("El hallazgo de la ejecución #" + hallazgo.getId() + " ya está resuelto.");
        }
        ordenRepository.vigenteDeHallazgo(hallazgo.getId()).ifPresent(o -> {
            throw conflicto("Este hallazgo ya tiene la orden " + o.getConsecutivo() + ".");
        });

        String prioridad = prioridadValida(request.prioridad());
        LocalDate fechaLimite = fechaLimiteValida(request.fechaLimite());
        UserEntity asignado = asignadoDe(request.asignadoAId(), request.paraCualquiera());
        ActividadEntity actividad = actividadValida(request.actividadId(), hallazgo);
        String instrucciones = textoValido(request.instrucciones(), "instrucciones");

        UserEntity creador = userRepositoryJpa.getUserEntityById(usuario.id());
        LocalDateTime ahora = calendario.ahora();
        OrdenTrabajoEntity orden = ordenRepository.save(OrdenTrabajoEntity.builder()
                .consecutivo(siguienteConsecutivo())
                .ejecucionOrigen(hallazgo)
                .estacion(hallazgo.getEstacion())
                .disciplina(hallazgo.getDisciplina())
                .actividad(actividad)
                .prioridad(prioridad)
                .fechaLimite(fechaLimite)
                .asignadoA(asignado)
                .instrucciones(instrucciones)
                .creadoPor(creador)
                .creadoEn(ahora)
                .actualizadoEn(ahora)
                .build());

        registrarEvento(orden, "CREADA", destino(asignado) + " · prioridad " + prioridad.toLowerCase()
                + " · vence el " + fechaLimite.format(DD_MM), null, creador, ahora);

        // El hallazgo ya se está atendiendo.
        if ("ABIERTO".equals(seguimiento.getEstado())) {
            seguimiento.setEstado("EN_PROCESO");
        }
        seguimiento.setActualizadoPor(creador);
        seguimiento.setActualizadoEn(ahora);
        seguimientoRepository.save(seguimiento);

        saveActionUseCase.save("El usuario " + creador.getUsername() + " creó la orden de trabajo " + orden.getConsecutivo()
                + " para el hallazgo de la ejecución #" + hallazgo.getId() + " en la estación " + hallazgo.getEstacion().getNombre());
        eventos.ordenCambio(orden.getEstacion().getId(), orden.getId(), "CREADA");
        return OrdenResumenResponse.fromEntity(orden, calendario.hoy());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrdenResumenResponse> listar(List<String> estados, Long estacionId, String prioridad, Boolean vencidas,
            String disciplina, Pageable pageable) {
        // Sin orden pedido: las más recientes primero.
        Pageable pagina = pageable.getSort().isSorted() ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "creadoEn", "id"));
        LocalDate hoy = calendario.hoy();
        return ordenRepository.findAll(OrdenTrabajoSpecifications.conFiltros(estados, estacionId, prioridad, vencidas,
                        disciplina, null, hoy), pagina)
                .map(o -> OrdenResumenResponse.fromEntity(o, hoy));
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenDetalleResponse detalle(Long id) {
        OrdenTrabajoEntity orden = buscar(id);
        return new OrdenDetalleResponse(
                OrdenResumenResponse.fromEntity(orden, calendario.hoy()),
                ejecucionUseCase.obtenerEjecucion(orden.getEjecucionOrigen().getId()),
                orden.getEjecucionCierre() != null ? ejecucionUseCase.obtenerEjecucion(orden.getEjecucionCierre().getId()) : null,
                orden.getObservacionCierre(),
                nombre(orden.getVerificadoPor()),
                orden.getVerificadoEn(),
                orden.getMotivoAnulacion(),
                eventoRepository.findByOrden_IdOrderByCreadoEnAscIdAsc(id).stream().map(OrdenEventoResponse::fromEntity).toList());
    }

    @Override
    @Transactional
    public OrdenResumenResponse editar(Long id, EditarOrdenRequest request, UserPrincipal usuario) {
        OrdenTrabajoEntity orden = buscar(id);
        if (!OrdenTrabajoEntity.ASIGNADA.equals(orden.getEstado())) {
            throw conflicto("La orden " + orden.getConsecutivo() + " está " + legible(orden.getEstado())
                    + ": solo se edita mientras está asignada.");
        }
        if (request == null) {
            throw new BadRequestException("No hay cambios.");
        }
        List<String> cambios = new ArrayList<>();
        if (request.prioridad() != null && !request.prioridad().equals(orden.getPrioridad())) {
            String nueva = prioridadValida(request.prioridad());
            cambios.add("Prioridad " + orden.getPrioridad().toLowerCase() + " → " + nueva.toLowerCase());
            orden.setPrioridad(nueva);
        }
        if (request.fechaLimite() != null && !request.fechaLimite().equals(orden.getFechaLimite())) {
            LocalDate nueva = fechaLimiteValida(request.fechaLimite());
            cambios.add("Vence el " + orden.getFechaLimite().format(DD_MM) + " → " + nueva.format(DD_MM));
            orden.setFechaLimite(nueva);
        }
        boolean aCualquiera = Boolean.TRUE.equals(request.paraCualquiera());
        Long actualId = orden.getAsignadoA() != null ? orden.getAsignadoA().getId() : null;
        if (aCualquiera || request.asignadoAId() != null) {
            UserEntity nuevo = asignadoDe(request.asignadoAId(), request.paraCualquiera());
            Long nuevoId = nuevo != null ? nuevo.getId() : null;
            if (!Objects.equals(actualId, nuevoId)) {
                cambios.add("Reasignada de " + paraQuien(orden.getAsignadoA()) + " a " + paraQuien(nuevo));
                orden.setAsignadoA(nuevo);
            }
        }
        if (request.actividadId() != null && !request.actividadId().equals(orden.getActividad().getId())) {
            ActividadEntity nueva = actividadValida(request.actividadId(), orden.getEjecucionOrigen());
            cambios.add("Actividad: " + nueva.getNombre());
            orden.setActividad(nueva);
        }
        if (request.instrucciones() != null && !request.instrucciones().trim().equals(orden.getInstrucciones())) {
            orden.setInstrucciones(textoValido(request.instrucciones(), "instrucciones"));
            cambios.add("Instrucciones actualizadas");
        }
        if (cambios.isEmpty()) {
            return OrdenResumenResponse.fromEntity(orden, calendario.hoy());
        }
        UserEntity editor = userRepositoryJpa.getUserEntityById(usuario.id());
        LocalDateTime ahora = calendario.ahora();
        orden.setActualizadoEn(ahora);
        ordenRepository.save(orden);
        registrarEvento(orden, "EDITADA", String.join(" · ", cambios), null, editor, ahora);
        saveActionUseCase.save("El usuario " + editor.getUsername() + " editó la orden de trabajo " + orden.getConsecutivo()
                + ": " + String.join("; ", cambios));
        eventos.ordenCambio(orden.getEstacion().getId(), orden.getId(), "EDITADA");
        return OrdenResumenResponse.fromEntity(orden, calendario.hoy());
    }

    @Override
    @Transactional
    public OrdenResumenResponse anular(Long id, ComentarioOrdenRequest request, UserPrincipal usuario) {
        OrdenTrabajoEntity orden = buscar(id);
        if (!orden.enCurso()) {
            throw conflicto("La orden " + orden.getConsecutivo() + " ya está " + legible(orden.getEstado()) + ".");
        }
        String motivo = textoValido(request != null ? request.comentario() : null, "El motivo");
        UserEntity quien = userRepositoryJpa.getUserEntityById(usuario.id());
        LocalDateTime ahora = calendario.ahora();
        orden.setEstado(OrdenTrabajoEntity.ANULADA);
        orden.setMotivoAnulacion(motivo);
        orden.setActualizadoEn(ahora);
        ordenRepository.save(orden);
        registrarEvento(orden, "ANULADA", motivo, null, quien, ahora);

        // El hallazgo vuelve a estar pendiente: se puede resolver sin orden o crearle otra.
        seguimientoRepository.findByEjecucion_Id(orden.getEjecucionOrigen().getId())
                .filter(s -> s.getStatus() && !"RESUELTO".equals(s.getEstado()))
                .ifPresent(s -> {
                    s.setEstado("ABIERTO");
                    s.setActualizadoPor(quien);
                    s.setActualizadoEn(ahora);
                    seguimientoRepository.save(s);
                });

        saveActionUseCase.save("El usuario " + quien.getUsername() + " anuló la orden de trabajo " + orden.getConsecutivo()
                + ": " + motivo);
        eventos.ordenCambio(orden.getEstacion().getId(), orden.getId(), "ANULADA");
        return OrdenResumenResponse.fromEntity(orden, calendario.hoy());
    }

    @Override
    @Transactional
    public OrdenResumenResponse cerrar(Long id, ComentarioOrdenRequest request, UserPrincipal usuario) {
        OrdenTrabajoEntity orden = porVerificar(id, "cerrar");
        String observacion = textoValido(request != null ? request.comentario() : null, "La observación");
        UserEntity quien = userRepositoryJpa.getUserEntityById(usuario.id());
        LocalDateTime ahora = calendario.ahora();
        orden.setEstado(OrdenTrabajoEntity.CERRADA);
        orden.setObservacionCierre(observacion);
        orden.setVerificadoPor(quien);
        orden.setVerificadoEn(ahora);
        orden.setActualizadoEn(ahora);
        ordenRepository.save(orden);
        registrarEvento(orden, "CERRADA", observacion, orden.getEjecucionCierre(), quien, ahora);

        // El hallazgo queda resuelto en la visita de la orden.
        seguimientoRepository.findByEjecucion_Id(orden.getEjecucionOrigen().getId())
                .filter(HallazgoSeguimientoEntity::getStatus)
                .ifPresent(s -> {
                    s.setEstado("RESUELTO");
                    s.setObservacionesCierre(orden.getConsecutivo() + ": " + observacion);
                    s.setResueltoEnEjecucion(orden.getEjecucionCierre());
                    s.setResueltoMismaVisita(false);
                    s.setCerradoPor(quien);
                    s.setCerradoEn(ahora);
                    s.setActualizadoPor(quien);
                    s.setActualizadoEn(ahora);
                    seguimientoRepository.save(s);
                });

        saveActionUseCase.save("El usuario " + quien.getUsername() + " verificó y cerró la orden de trabajo "
                + orden.getConsecutivo());
        eventos.ordenCambio(orden.getEstacion().getId(), orden.getId(), "CERRADA");
        return OrdenResumenResponse.fromEntity(orden, calendario.hoy());
    }

    @Override
    @Transactional
    public OrdenResumenResponse devolver(Long id, ComentarioOrdenRequest request, UserPrincipal usuario) {
        OrdenTrabajoEntity orden = porVerificar(id, "devolver");
        String motivo = textoValido(request != null ? request.comentario() : null, "El motivo");
        UserEntity quien = userRepositoryJpa.getUserEntityById(usuario.id());
        LocalDateTime ahora = calendario.ahora();
        // La visita sigue enlazada a la orden (y en la línea de tiempo); la próxima la reemplaza.
        EjecucionEntity visita = orden.getEjecucionCierre();
        orden.setEstado(OrdenTrabajoEntity.ASIGNADA);
        orden.setEjecucionCierre(null);
        orden.setActualizadoEn(ahora);
        ordenRepository.save(orden);
        registrarEvento(orden, "DEVUELTA", motivo, visita, quien, ahora);
        saveActionUseCase.save("El usuario " + quien.getUsername() + " devolvió la orden de trabajo "
                + orden.getConsecutivo() + " a " + paraQuien(orden.getAsignadoA()) + ": " + motivo);
        eventos.ordenCambio(orden.getEstacion().getId(), orden.getId(), "DEVUELTA");
        return OrdenResumenResponse.fromEntity(orden, calendario.hoy());
    }


    @Override
    @Transactional(readOnly = true)
    public List<OperarioResponse> operarios() {
        return userRepositoryJpa.findAllActive().stream()
                .filter(u -> "OPERARIO".equals(u.getRole()))
                .map(u -> new OperarioResponse(u.getId(), nombre(u), u.getUsername()))
                .sorted(Comparator.comparing(OperarioResponse::nombre, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrdenMovilResponse> asignadas(UserPrincipal usuario) {
        LocalDate hoy = calendario.hoy();
        return ordenRepository.asignadasA(usuario.id()).stream()
                .map(o -> new OrdenMovilResponse(
                        OrdenResumenResponse.fromEntity(o, hoy),
                        evidenciaRepository.findByEjecucion_Id(o.getEjecucionOrigen().getId()).stream()
                                .map(EvidenciaResponse::fromEntity).toList(),
                        ultimaDevolucion(o.getId())))
                .toList();
    }

    /** El motivo de la última devolución, si la orden volvió al operario después de ejecutarla. */
    private String ultimaDevolucion(Long ordenId) {
        List<OrdenEventoEntity> eventos = eventoRepository.findByOrden_IdOrderByCreadoEnAscIdAsc(ordenId);
        for (int i = eventos.size() - 1; i >= 0; i--) {
            String tipo = eventos.get(i).getTipo();
            if ("DEVUELTA".equals(tipo)) {
                return eventos.get(i).getComentario();
            }
            if ("CREADA".equals(tipo) || "EJECUTADA".equals(tipo)) {
                return null;
            }
        }
        return null;
    }

    // ---- Apoyo ----

    /** La orden bloqueada y en POR_VERIFICAR (cerrar y devolver solo se hacen ahí). */
    private OrdenTrabajoEntity porVerificar(Long id, String accion) {
        OrdenTrabajoEntity orden = ordenRepository.findByIdParaActualizar(id).filter(OrdenTrabajoEntity::getStatus)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de trabajo no encontrada: id=" + id));
        if (!OrdenTrabajoEntity.POR_VERIFICAR.equals(orden.getEstado())) {
            throw conflicto("La orden " + orden.getConsecutivo() + " está " + legible(orden.getEstado())
                    + ": solo se puede " + accion + " cuando el operario ya registró el trabajo.");
        }
        return orden;
    }


    OrdenTrabajoEntity buscar(Long id) {
        return ordenRepository.findById(id).filter(OrdenTrabajoEntity::getStatus)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de trabajo no encontrada: id=" + id));
    }

    void registrarEvento(OrdenTrabajoEntity orden, String tipo, String comentario, EjecucionEntity ejecucion,
            UserEntity usuario, LocalDateTime cuando) {
        eventoRepository.save(OrdenEventoEntity.builder()
                .orden(orden).tipo(tipo).comentario(comentario).ejecucion(ejecucion).usuario(usuario).creadoEn(cuando)
                .build());
    }

    private String siguienteConsecutivo() {
        OrderCounterEntity contador = orderCounterRepository.findByModuleCodeForUpdate(MODULO)
                .orElse(new OrderCounterEntity(MODULO, 0));
        int valor = contador.getLastValue() + 1;
        contador.setLastValue(valor);
        orderCounterRepository.save(contador);
        return "OT-" + MODULO + "-" + String.format("%05d", valor);
    }

    private static String prioridadValida(String prioridad) {
        if (prioridad == null || !PRIORIDADES.contains(prioridad)) {
            throw new BadRequestException("La prioridad debe ser ALTA o MEDIA.");
        }
        return prioridad;
    }

    private LocalDate fechaLimiteValida(LocalDate fecha) {
        if (fecha == null) {
            throw new BadRequestException("La fecha límite es obligatoria.");
        }
        if (fecha.isBefore(calendario.hoy())) {
            throw new BadRequestException("La fecha límite no puede ser anterior a hoy.");
        }
        // Una corrección no se planea a años: un 2062 por un error de tecleo nunca saldría como vencida.
        if (fecha.isAfter(calendario.hoy().plusDays(MAX_DIAS_PLAZO))) {
            throw new BadRequestException("La fecha límite puede ser hasta dentro de un año.");
        }
        return fecha;
    }

    /** El operario elegido, o null si es para cualquiera; nunca las dos cosas ni ninguna. */
    private UserEntity asignadoDe(Long usuarioId, Boolean paraCualquiera) {
        if (Boolean.TRUE.equals(paraCualquiera)) {
            if (usuarioId != null) {
                throw new BadRequestException("Elija un operario o \"cualquier operario\", no ambos.");
            }
            return null;
        }
        return operarioValido(usuarioId);
    }

    private static String destino(UserEntity asignado) {
        return asignado == null ? "Para cualquier operario" : "Asignada a " + nombre(asignado);
    }

    private static String paraQuien(UserEntity asignado) {
        return asignado == null ? "cualquier operario" : nombre(asignado);
    }

    private UserEntity operarioValido(Long usuarioId) {
        if (usuarioId == null) {
            throw new BadRequestException("Asigne la orden a un operario.");
        }
        return userRepositoryJpa.findActiveById(usuarioId)
                .filter(u -> "OPERARIO".equals(u.getRole()))
                .orElseThrow(() -> new BadRequestException("La orden se asigna a un operario activo."));
    }

    private ActividadEntity actividadValida(Long actividadId, EjecucionEntity hallazgo) {
        if (actividadId == null) {
            throw new BadRequestException("Elija la actividad con la que se registra la corrección.");
        }
        ActividadEntity actividad = actividadRepository.findById(actividadId)
                .filter(a -> Boolean.TRUE.equals(a.getStatus()))
                .orElseThrow(() -> new BadRequestException("La actividad no existe o está inactiva."));
        if (!Objects.equals(actividad.getDisciplina().getId(), hallazgo.getDisciplina().getId())) {
            throw new BadRequestException("La actividad debe ser de la misma disciplina del hallazgo.");
        }
        return actividad;
    }

    private static String textoValido(String texto, String campo) {
        if (texto == null || texto.isBlank()) {
            throw new BadRequestException(campo + " es obligatorio.");
        }
        String limpio = texto.trim();
        if (limpio.length() > MAX_TEXTO) {
            throw new BadRequestException(campo + " admite hasta " + MAX_TEXTO + " caracteres.");
        }
        return limpio;
    }


    static ResponseStatusException conflicto(String mensaje) {
        return new ResponseStatusException(HttpStatus.CONFLICT, mensaje);
    }

    static String legible(String estado) {
        return switch (estado) {
            case OrdenTrabajoEntity.ASIGNADA -> "asignada";
            case OrdenTrabajoEntity.POR_VERIFICAR -> "por verificar";
            case OrdenTrabajoEntity.CERRADA -> "cerrada";
            case OrdenTrabajoEntity.ANULADA -> "anulada";
            default -> estado.toLowerCase();
        };
    }

    static String nombre(UserEntity usuario) {
        if (usuario == null) {
            return null;
        }
        return usuario.getFullName() != null && !usuario.getFullName().isBlank() ? usuario.getFullName() : usuario.getUsername();
    }
}
