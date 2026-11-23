package com.app.usochicamochabackend.substation.application.service;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.exception.BadRequestException;
import com.app.usochicamochabackend.substation.application.dto.ConjuntoElementosRequest;
import com.app.usochicamochabackend.substation.application.dto.EjecucionEditRequest;
import com.app.usochicamochabackend.substation.application.dto.EjecucionRequest;
import com.app.usochicamochabackend.substation.application.dto.EjecucionResponse;
import com.app.usochicamochabackend.substation.application.dto.ElementoDeConjuntoResponse;
import com.app.usochicamochabackend.substation.application.dto.ElementoIntervenidoResponse;
import com.app.usochicamochabackend.substation.application.dto.ElementoMarcadoRequest;
import com.app.usochicamochabackend.substation.application.dto.ElementoMarcadoResponse;
import com.app.usochicamochabackend.substation.application.dto.ElementoOtroRequest;
import com.app.usochicamochabackend.substation.application.dto.ItemCatalogoResponse;
import com.app.usochicamochabackend.substation.application.port.SubstationCatalogAdminUseCase;
import com.app.usochicamochabackend.substation.application.port.SubstationEjecucionUseCase;
import com.app.usochicamochabackend.substation.application.port.SubstationIndicadoresUseCase;
import com.app.usochicamochabackend.substation.infrastructure.entity.ActividadEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.ConjuntoElementoEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.ConjuntoEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.DisciplinaEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.ElementoEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.EstacionEntity;
import com.app.usochicamochabackend.substation.infrastructure.repository.ActividadRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.ConjuntoElementoRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.ConjuntoRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.DisciplinaRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.ElementoRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.EstacionRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Ajuste de Eléctrico: cada conjunto con sus elementos adentro (V54). */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SubstationConjuntoElementosIntegrationTest {

    @MockitoBean
    private com.app.usochicamochabackend.substation.application.port.SubstationEventosPort eventos;

    @Autowired private SubstationEjecucionUseCase ejecucionUseCase;
    @Autowired private SubstationCatalogAdminUseCase catalogAdminUseCase;
    @Autowired private SubstationIndicadoresUseCase indicadoresUseCase;
    @Autowired private UserRepositoryJpa userRepositoryJpa;
    @Autowired private DisciplinaRepository disciplinaRepository;
    @Autowired private EstacionRepository estacionRepository;
    @Autowired private ActividadRepository actividadRepository;
    @Autowired private ConjuntoRepository conjuntoRepository;
    @Autowired private ElementoRepository elementoRepository;
    @Autowired private ConjuntoElementoRepository conjuntoElementoRepository;
    @Autowired private EntityManager entityManager;

    private UserPrincipal usuario;
    private EstacionEntity estacion;
    private ActividadEntity inspeccion;
    private ConjuntoEntity transformador;
    private ConjuntoEntity subestacion;
    private ElementoEntity fusibles;
    private ElementoEntity estadoFisico;
    private ElementoEntity medidor;

    @BeforeEach
    void setUp() {
        UserEntity user = userRepositoryJpa.save(UserEntity.builder()
                .username("electricista.conjuntos").fullName("Electricista").email("conjuntos@example.com")
                .role("OPERARIO").password("x").status(true).build());
        usuario = new UserPrincipal(user.getId(), user.getUsername());

        DisciplinaEntity electrico = disciplinaRepository.save(DisciplinaEntity.builder()
                .codigo("ELECTRICO").conjuntos(DisciplinaEntity.CONJUNTOS_OBLIGATORIO).usaElementos(true).build());
        estacion = estacionRepository.save(EstacionEntity.builder()
                .nombre("Estación Conjuntos").tipo("BOMBEO").frecuenciaBase("MENSUAL").status(true).build());
        inspeccion = actividadRepository.save(ActividadEntity.builder()
                .nombre("Inspección eléctrica").disciplina(electrico).capturaMovilHabilitada(true).status(true).build());

        transformador = conjuntoRepository.save(ConjuntoEntity.builder().nombre("Transformador").disciplina(electrico).build());
        subestacion = conjuntoRepository.save(ConjuntoEntity.builder().nombre("Subestación").disciplina(electrico).build());
        fusibles = elementoRepository.save(ElementoEntity.builder().nombre("Fusibles").disciplina(electrico).build());
        estadoFisico = elementoRepository.save(ElementoEntity.builder().nombre("Estado físico").disciplina(electrico).build());
        medidor = elementoRepository.save(ElementoEntity.builder().nombre("Medidor de energía").disciplina(electrico).build());

        meter(transformador, fusibles, false);
        meter(transformador, estadoFisico, true);
        meter(subestacion, fusibles, false);
        meter(subestacion, medidor, false);
    }

    private void meter(ConjuntoEntity conjunto, ElementoEntity elemento, boolean pideDetalle) {
        conjuntoElementoRepository.save(ConjuntoElementoEntity.builder()
                .conjunto(conjunto).elemento(elemento).pideDetalle(pideDetalle).build());
    }

    private EjecucionRequest visita(List<Long> conjuntoIds, List<ElementoMarcadoRequest> marcados,
            List<ElementoOtroRequest> otros) {
        return new EjecucionRequest(LocalDate.of(2026, 9, 7), 9, 2, estacion.getId(), "ELECTRICO",
                "PREVENTIVO", "INSPECCION", inspeccion.getId(), null, null, "CONFORME",
                "Revisión general", null, UUID.randomUUID(), null,
                conjuntoIds, null, null, null, marcados, otros);
    }

    private EjecucionEditRequest edicion(EjecucionResponse e, List<Long> conjuntoIds,
            List<ElementoMarcadoRequest> marcados, List<ElementoOtroRequest> otros) {
        return new EjecucionEditRequest(e.fecha(), e.mesEjecucion(), e.semanaEjecucion(), e.tipoMantenimiento(),
                e.tipoActividad(), e.actividadId(), e.motivoNoCatalogado(), e.resultado(), e.observaciones(),
                e.descripcionLibre(), "Se corrige lo que se intervino", conjuntoIds, null, null, null, marcados, otros);
    }

    private static ElementoMarcadoRequest marca(ConjuntoEntity c, ElementoEntity e) {
        return new ElementoMarcadoRequest(c.getId(), e.getId(), null);
    }

    private static List<String> etiquetas(List<ElementoMarcadoResponse> elementos) {
        return elementos.stream()
                .map(m -> (m.conjuntoNombre() != null ? m.conjuntoNombre() : "-") + " › " + m.nombre()
                        + (m.detalle() != null ? " (" + m.detalle() + ")" : ""))
                .toList();
    }

    // ---------------------------------------------------------------------
    // Configuración: lo que tiene cada conjunto
    // ---------------------------------------------------------------------

    @Test
    void listarConjuntos_traeLosElementosDeCadaUno() {
        ItemCatalogoResponse t = catalogAdminUseCase.listarConjuntos("ELECTRICO", false).stream()
                .filter(c -> c.id().equals(transformador.getId())).findFirst().orElseThrow();

        assertEquals(List.of("Fusibles", "Estado físico"), t.elementos().stream().map(ElementoDeConjuntoResponse::nombre).toList());
        assertTrue(t.elementos().get(1).pideDetalle());
    }

    @Test
    void guardarElementos_reutilizaNombresOrdenaYQuitaLoQueNoViene() {
        ItemCatalogoResponse r = catalogAdminUseCase.guardarElementosConjunto(transformador.getId(),
                new ConjuntoElementosRequest(List.of(
                        new ConjuntoElementosRequest.Item(null, "  MEDIDOR de energia ", false),
                        new ConjuntoElementosRequest.Item(null, "nivel   de aceite", false),
                        new ConjuntoElementosRequest.Item(estadoFisico.getId(), null, true))),
                usuario);

        // "MEDIDOR de energia" es el mismo nombre que "Medidor de energía": se reutiliza, no se duplica.
        assertEquals(List.of("Medidor de energía", "Nivel de aceite", "Estado físico"),
                r.elementos().stream().map(ElementoDeConjuntoResponse::nombre).toList());
        assertEquals(medidor.getId(), r.elementos().get(0).id());
        // Fusibles se quitó del Transformador, pero sigue en la Subestación y no se borra.
        ConjuntoElementoEntity quitado = conjuntoElementoRepository
                .findByConjunto_IdAndElemento_Id(transformador.getId(), fusibles.getId()).orElseThrow();
        assertFalse(quitado.getStatus());
        assertTrue(conjuntoElementoRepository.findByConjunto_IdAndElemento_Id(subestacion.getId(), fusibles.getId())
                .orElseThrow().getStatus());
    }

    // ---------------------------------------------------------------------
    // Registro desde el móvil
    // ---------------------------------------------------------------------

    @Test
    void registrar_elMismoElementoEnDosConjuntosSonDosRegistros() {
        EjecucionResponse r = ejecucionUseCase.registrarEjecucion(visita(
                List.of(transformador.getId(), subestacion.getId()),
                List.of(marca(transformador, fusibles), marca(subestacion, fusibles),
                        new ElementoMarcadoRequest(transformador.getId(), estadoFisico.getId(), "  Con   óxido ")),
                null), usuario);

        assertEquals(List.of("Subestación › Fusibles", "Transformador › Estado físico (Con óxido)",
                "Transformador › Fusibles"), etiquetas(r.elementos()));
    }

    @Test
    void registrar_unElementoDeUnConjuntoNoMarcadoAgregaElConjunto() {
        EjecucionResponse r = ejecucionUseCase.registrarEjecucion(visita(
                List.of(), List.of(marca(subestacion, medidor)), null), usuario);

        assertEquals(List.of("Subestación"), r.conjuntos().stream().map(c -> c.nombre()).toList());
        assertEquals(List.of("Subestación › Medidor de energía"), etiquetas(r.elementos()));
    }

    @Test
    void registrar_otroCreaElElementoDentroDelConjuntoYLuegoSeReutiliza() {
        ejecucionUseCase.registrarEjecucion(visita(List.of(transformador.getId()), null,
                List.of(new ElementoOtroRequest(transformador.getId(), "  bornera   AUXILIAR "))), usuario);
        EjecucionResponse segunda = ejecucionUseCase.registrarEjecucion(visita(List.of(transformador.getId()), null,
                List.of(new ElementoOtroRequest(transformador.getId(), "Bornera auxiliar"))), usuario);

        // El nombre queda como lo escribió el primero (sin espacios de más y con mayúscula inicial; las
        // siglas se respetan): el segundo, escrito distinto, reutiliza ese mismo elemento.
        assertEquals(List.of("Transformador › Bornera AUXILIAR"), etiquetas(segunda.elementos()));
        List<ElementoEntity> borneras = elementoRepository.findAll().stream()
                .filter(e -> e.getNombre().equalsIgnoreCase("bornera auxiliar")).toList();
        assertEquals(1, borneras.size(), "El mismo texto no crea dos elementos");
        assertTrue(borneras.get(0).getProvisional());
        ConjuntoElementoEntity ce = conjuntoElementoRepository
                .findByConjunto_IdAndElemento_Id(transformador.getId(), borneras.get(0).getId()).orElseThrow();
        assertTrue(ce.getProvisional(), "Queda para revisión en Configuración");
    }

    @Test
    void registrar_conLaListaViejaSinConjuntoSigueFuncionando() {
        EjecucionRequest vieja = new EjecucionRequest(LocalDate.of(2026, 9, 7), 9, 2, estacion.getId(), "ELECTRICO",
                "PREVENTIVO", "INSPECCION", inspeccion.getId(), null, null, "CONFORME", "Revisión", null,
                UUID.randomUUID(), null, List.of(transformador.getId()), List.of(fusibles.getId()), null, null);

        EjecucionResponse r = ejecucionUseCase.registrarEjecucion(vieja, usuario);

        assertEquals(1, r.elementos().size());
        assertNull(r.elementos().get(0).conjuntoId(), "Sin conjunto, como las visitas viejas");
    }

    @Test
    void registrar_sinConjuntoEnEléctricoSeGuardaIgual() {
        EjecucionResponse r = ejecucionUseCase.registrarEjecucion(visita(List.of(), null, null), usuario);

        assertTrue(r.conjuntos().isEmpty(), "Desde el móvil no se rechaza: quedaría reintentando para siempre");
    }

    // ---------------------------------------------------------------------
    // Edición
    // ---------------------------------------------------------------------

    @Test
    void editar_cambiaElTextoSinPerderElRegistro() {
        EjecucionResponse creada = ejecucionUseCase.registrarEjecucion(visita(List.of(transformador.getId()),
                List.of(new ElementoMarcadoRequest(transformador.getId(), estadoFisico.getId(), "Bien")), null), usuario);
        entityManager.flush();

        EjecucionResponse editada = ejecucionUseCase.editarEjecucion(creada.id(), edicion(creada, null,
                List.of(new ElementoMarcadoRequest(transformador.getId(), estadoFisico.getId(), "Con fuga de aceite"),
                        marca(transformador, fusibles)), null), usuario);
        entityManager.flush();

        assertEquals(List.of("Transformador › Estado físico (Con fuga de aceite)", "Transformador › Fusibles"),
                etiquetas(editada.elementos()));
    }

    @Test
    void editar_quitarUnConjuntoSeLlevaSusElementos() {
        EjecucionResponse creada = ejecucionUseCase.registrarEjecucion(visita(
                List.of(transformador.getId(), subestacion.getId()),
                List.of(marca(transformador, fusibles), marca(subestacion, medidor)), null), usuario);

        EjecucionResponse editada = ejecucionUseCase.editarEjecucion(creada.id(),
                edicion(creada, List.of(subestacion.getId()), null, null), usuario);

        assertEquals(List.of("Subestación › Medidor de energía"), etiquetas(editada.elementos()));
    }

    @Test
    void editar_unaVisitaViejaConservaSusElementosSinConjunto() {
        EjecucionResponse vieja = ejecucionUseCase.registrarEjecucion(new EjecucionRequest(LocalDate.of(2026, 9, 7), 9,
                2, estacion.getId(), "ELECTRICO", "PREVENTIVO", "INSPECCION", inspeccion.getId(), null, null,
                "CONFORME", "Revisión", null, UUID.randomUUID(), null, List.of(transformador.getId()),
                List.of(medidor.getId()), null, null), usuario);
        entityManager.flush();

        EjecucionEditRequest edicion = new EjecucionEditRequest(vieja.fecha(), vieja.mesEjecucion(),
                vieja.semanaEjecucion(), vieja.tipoMantenimiento(), vieja.tipoActividad(), vieja.actividadId(),
                vieja.motivoNoCatalogado(), vieja.resultado(), vieja.observaciones(), vieja.descripcionLibre(),
                "Se agrega lo del transformador", null, List.of(medidor.getId()), null, null,
                List.of(marca(transformador, fusibles)), null);
        EjecucionResponse editada = ejecucionUseCase.editarEjecucion(vieja.id(), edicion, usuario);

        assertEquals(List.of("Transformador › Fusibles", "- › Medidor de energía"), etiquetas(editada.elementos()));
    }

    @Test
    void editar_unElementoQueNoEsDelConjuntoEsUn400() {
        EjecucionResponse creada = ejecucionUseCase.registrarEjecucion(visita(List.of(transformador.getId()),
                List.of(marca(transformador, fusibles)), null), usuario);

        assertThrows(BadRequestException.class, () -> ejecucionUseCase.editarEjecucion(creada.id(),
                edicion(creada, null, List.of(marca(transformador, medidor)), null), usuario));
    }

    @Test
    void editar_unElementoDeUnConjuntoNoMarcadoEsUn400() {
        EjecucionResponse creada = ejecucionUseCase.registrarEjecucion(visita(List.of(transformador.getId()),
                List.of(marca(transformador, fusibles)), null), usuario);

        assertThrows(BadRequestException.class, () -> ejecucionUseCase.editarEjecucion(creada.id(),
                edicion(creada, null, List.of(marca(subestacion, medidor)), null), usuario));
    }

    @Test
    void editar_dejarSinConjuntosEnEléctricoEsUn400() {
        EjecucionResponse creada = ejecucionUseCase.registrarEjecucion(visita(List.of(transformador.getId()),
                List.of(marca(transformador, fusibles)), null), usuario);

        assertThrows(BadRequestException.class, () -> ejecucionUseCase.editarEjecucion(creada.id(),
                edicion(creada, List.of(), List.of(), null), usuario));
    }

    // ---------------------------------------------------------------------
    // Indicadores y filtros
    // ---------------------------------------------------------------------

    @Test
    void elementosMasIntervenidos_cuentaVisitasNoMarcas() {
        ejecucionUseCase.registrarEjecucion(visita(List.of(transformador.getId(), subestacion.getId()),
                List.of(marca(transformador, fusibles), marca(subestacion, fusibles)), null), usuario);
        entityManager.flush();

        ElementoIntervenidoResponse f = indicadoresUseCase.elementosIntervenidos(2026, "ELECTRICO", null).stream()
                .filter(x -> x.elementoId().equals(fusibles.getId())).findFirst().orElseThrow();
        assertEquals(1, f.intervenciones(), "Una visita que lo marcó en dos conjuntos cuenta una vez");
    }
}
