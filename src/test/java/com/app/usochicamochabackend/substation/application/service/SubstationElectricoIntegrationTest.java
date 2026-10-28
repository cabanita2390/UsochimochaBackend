package com.app.usochicamochabackend.substation.application.service;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.exception.BadRequestException;
import com.app.usochicamochabackend.substation.application.dto.CambioCampo;
import com.app.usochicamochabackend.substation.application.dto.EjecucionEditRequest;
import com.app.usochicamochabackend.substation.application.dto.EjecucionRequest;
import com.app.usochicamochabackend.substation.application.dto.EjecucionResponse;
import com.app.usochicamochabackend.substation.application.dto.ElementoIntervenidoResponse;
import com.app.usochicamochabackend.substation.application.dto.ItemCatalogoRequest;
import com.app.usochicamochabackend.substation.application.dto.ItemCatalogoResponse;
import com.app.usochicamochabackend.substation.application.dto.ItemRefResponse;
import com.app.usochicamochabackend.substation.application.port.SubstationCatalogAdminUseCase;
import com.app.usochicamochabackend.substation.application.port.SubstationEjecucionUseCase;
import com.app.usochicamochabackend.substation.application.port.SubstationIndicadoresUseCase;
import com.app.usochicamochabackend.substation.infrastructure.entity.ActividadEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.ConjuntoEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.DisciplinaEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.ElementoEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.EstacionEntity;
import com.app.usochicamochabackend.substation.infrastructure.repository.ActividadRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.ConjuntoRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.DisciplinaRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.ElementoRepository;
import com.app.usochicamochabackend.substation.infrastructure.repository.EstacionRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Disciplina Eléctrico: conjuntos y elementos intervenidos en cada visita, su catálogo y el
 * indicador "elementos más intervenidos". Mismo esquema H2 que {@link SubstationServiceIntegrationTest},
 * con un fixture sintético propio.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestPropertySource(properties = "app.storage.uploads-root=${java.io.tmpdir}/subestaciones-test-uploads")
class SubstationElectricoIntegrationTest {

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private com.app.usochicamochabackend.substation.application.port.SubstationEventosPort eventos;

    @Autowired
    private SubstationEjecucionUseCase ejecucionUseCase;

    @Autowired
    private SubstationCatalogAdminUseCase catalogAdminUseCase;

    @Autowired
    private SubstationIndicadoresUseCase indicadoresUseCase;

    @Autowired
    private UserRepositoryJpa userRepositoryJpa;

    @Autowired
    private DisciplinaRepository disciplinaRepository;

    @Autowired
    private EstacionRepository estacionRepository;

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private ConjuntoRepository conjuntoRepository;

    @Autowired
    private ElementoRepository elementoRepository;

    @Autowired
    private EntityManager entityManager;

    private UserPrincipal usuario;
    private DisciplinaEntity electrico;
    private DisciplinaEntity civil;
    private EstacionEntity estacionUno;
    private EstacionEntity estacionDos;
    private ActividadEntity inspeccionGeneral;
    private ConjuntoEntity gabinetes;
    private ConjuntoEntity motores;
    private ElementoEntity breaker;
    private ElementoEntity variador;
    private ElementoEntity elementoCivil;

    @BeforeEach
    void setUp() {
        UserEntity user = userRepositoryJpa.save(UserEntity.builder()
                .username("electricista.test")
                .fullName("Electricista de Prueba")
                .email("electricista.test@example.com")
                .role("OPERARIO")
                .password("irrelevante")
                .status(true)
                .build());
        usuario = new UserPrincipal(user.getId(), user.getUsername());

        electrico = disciplinaRepository.save(DisciplinaEntity.builder().codigo("ELECTRICO").build());
        civil = disciplinaRepository.save(DisciplinaEntity.builder().codigo("CIVIL").build());

        estacionUno = estacionRepository.save(EstacionEntity.builder()
                .nombre("Estación Eléctrica Uno").tipo("BOMBEO").frecuenciaBase("MENSUAL").status(true).build());
        estacionDos = estacionRepository.save(EstacionEntity.builder()
                .nombre("Estación Eléctrica Dos").tipo("BOMBEO").frecuenciaBase("MENSUAL").status(true).build());
        inspeccionGeneral = actividadRepository.save(ActividadEntity.builder()
                .nombre("Inspección eléctrica general de prueba").disciplina(electrico)
                .capturaMovilHabilitada(true).status(true).build());

        gabinetes = conjuntoRepository.save(ConjuntoEntity.builder().nombre("Gabinetes de control").disciplina(electrico).build());
        motores = conjuntoRepository.save(ConjuntoEntity.builder().nombre("Motores").disciplina(electrico).build());
        breaker = elementoRepository.save(ElementoEntity.builder().nombre("Breaker").disciplina(electrico).build());
        variador = elementoRepository.save(ElementoEntity.builder().nombre("Variador de velocidad").disciplina(electrico).build());
        elementoCivil = elementoRepository.save(ElementoEntity.builder().nombre("Muro").disciplina(civil).build());
    }

    private EjecucionRequest visita(EstacionEntity estacion, String tipoActividad, List<Long> conjuntoIds,
            List<Long> elementoIds, String conjuntoOtro, String elementoOtro) {
        return new EjecucionRequest(LocalDate.of(2026, 9, 7), 9, 2, estacion.getId(), "ELECTRICO",
                "PREVENTIVO", tipoActividad, inspeccionGeneral.getId(), null, null, "CONFORME",
                "Se revisaron gabinetes y bornes", null, UUID.randomUUID(), null,
                conjuntoIds, elementoIds, conjuntoOtro, elementoOtro);
    }

    private EjecucionEditRequest edicion(EjecucionResponse e, List<Long> conjuntoIds, List<Long> elementoIds,
            String conjuntoOtro, String elementoOtro) {
        return new EjecucionEditRequest(e.fecha(), e.mesEjecucion(), e.semanaEjecucion(), e.tipoMantenimiento(),
                e.tipoActividad(), e.actividadId(), e.motivoNoCatalogado(), e.resultado(), e.observaciones(),
                e.descripcionLibre(), "Se corrigen los elementos intervenidos", conjuntoIds, elementoIds,
                conjuntoOtro, elementoOtro);
    }

    private static List<String> nombres(List<ItemRefResponse> items) {
        return items.stream().map(ItemRefResponse::nombre).toList();
    }

    // ---------------------------------------------------------------------
    // Registro y edición de la visita
    // ---------------------------------------------------------------------

    @Test
    void registrarVisitaElectrica_guardaConjuntosElementosYOtros() {
        EjecucionResponse creada = ejecucionUseCase.registrarEjecucion(visita(estacionUno, "INSPECCION",
                List.of(gabinetes.getId(), motores.getId()), List.of(variador.getId(), breaker.getId()),
                "  Tablero   auxiliar ", "   "), usuario);
        entityManager.flush();
        entityManager.clear();

        EjecucionResponse leida = ejecucionUseCase.obtenerEjecucion(creada.id());
        assertEquals(List.of("Gabinetes de control", "Motores"), nombres(leida.conjuntos()));
        assertEquals(List.of("Breaker", "Variador de velocidad"), nombres(leida.elementos()));
        assertEquals("Tablero auxiliar", leida.conjuntoOtro());
        assertNull(leida.elementoOtro(), "Un \"Otros\" en blanco no se guarda");
    }

    @Test
    void registrarVisitaElectrica_sinConjuntosNiElementos_esValida() {
        EjecucionResponse creada = ejecucionUseCase.registrarEjecucion(
                visita(estacionUno, "MANTENIMIENTO", null, null, null, null), usuario);

        assertTrue(creada.conjuntos().isEmpty());
        assertTrue(creada.elementos().isEmpty());
    }

    @Test
    void registrarDesdeElMovil_ignoraIdsDeOtraDisciplinaOInexistentes() {
        // Un catálogo viejo en el teléfono no debe dejar el registro rechazado para siempre en la cola.
        EjecucionResponse creada = ejecucionUseCase.registrarEjecucion(visita(estacionUno, "INSPECCION",
                List.of(gabinetes.getId(), 999_999L), List.of(breaker.getId(), elementoCivil.getId()),
                null, null), usuario);

        assertEquals(List.of("Gabinetes de control"), nombres(creada.conjuntos()));
        assertEquals(List.of("Breaker"), nombres(creada.elementos()));
    }

    @Test
    void registrarVisitaElectrica_aceptaUnElementoDesactivadoDespuesDeMarcarlo() {
        breaker.setStatus(false);
        elementoRepository.save(breaker);

        EjecucionResponse creada = ejecucionUseCase.registrarEjecucion(
                visita(estacionUno, "INSPECCION", null, List.of(breaker.getId()), null, null), usuario);

        assertEquals(List.of("Breaker"), nombres(creada.elementos()));
    }

    @Test
    void electrico_usaLasMismasReglasDeTipoDeActividadQueCivil() {
        assertThrows(BadRequestException.class, () -> ejecucionUseCase.registrarEjecucion(
                visita(estacionUno, "OTRO", null, null, null, null), usuario));
    }

    @Test
    void editarSinEnviarConjuntosNiElementos_losConserva() {
        EjecucionResponse creada = ejecucionUseCase.registrarEjecucion(visita(estacionUno, "INSPECCION",
                List.of(gabinetes.getId()), List.of(breaker.getId()), "Tablero", null), usuario);

        // El constructor sin conjuntos es el que usan Civil y el móvil anterior a Eléctrico.
        EjecucionEditRequest sinTocarlos = new EjecucionEditRequest(creada.fecha(), creada.mesEjecucion(),
                creada.semanaEjecucion(), creada.tipoMantenimiento(), creada.tipoActividad(), creada.actividadId(),
                null, creada.resultado(), "Observación corregida en la revisión", null,
                "Se corrige la observación de la visita");
        EjecucionResponse editada = ejecucionUseCase.editarEjecucion(creada.id(), sinTocarlos, usuario);

        assertEquals(List.of("Gabinetes de control"), nombres(editada.conjuntos()));
        assertEquals(List.of("Breaker"), nombres(editada.elementos()));
        assertEquals("Tablero", editada.conjuntoOtro());
    }

    @Test
    void editarConjuntosYElementos_losReemplazaYQuedaEnElHistorial() {
        EjecucionResponse creada = ejecucionUseCase.registrarEjecucion(visita(estacionUno, "INSPECCION",
                List.of(gabinetes.getId()), List.of(breaker.getId()), "Tablero", null), usuario);

        EjecucionResponse editada = ejecucionUseCase.editarEjecucion(creada.id(),
                edicion(creada, List.of(motores.getId()), List.of(), "", "Celda de medida"), usuario);

        assertEquals(List.of("Motores"), nombres(editada.conjuntos()));
        assertTrue(editada.elementos().isEmpty());
        assertNull(editada.conjuntoOtro());
        assertEquals("Celda de medida", editada.elementoOtro());
        List<CambioCampo> cambios = editada.ediciones().get(0).cambios();
        assertTrue(cambios.contains(new CambioCampo("conjuntos", "Gabinetes de control", "Motores")), cambios::toString);
        assertTrue(cambios.contains(new CambioCampo("elementos", "Breaker", null)), cambios::toString);
        assertTrue(cambios.contains(new CambioCampo("elementoOtro", null, "Celda de medida")), cambios::toString);
    }

    @Test
    void editarConUnElementoDeOtraDisciplina_esUn400() {
        EjecucionResponse creada = ejecucionUseCase.registrarEjecucion(
                visita(estacionUno, "INSPECCION", null, List.of(breaker.getId()), null, null), usuario);

        assertThrows(BadRequestException.class, () -> ejecucionUseCase.editarEjecucion(creada.id(),
                edicion(creada, null, List.of(elementoCivil.getId()), null, null), usuario));
    }

    // ---------------------------------------------------------------------
    // Listado e indicador
    // ---------------------------------------------------------------------

    @Test
    void listarEjecuciones_filtraPorElemento() {
        ejecucionUseCase.registrarEjecucion(visita(estacionUno, "INSPECCION", null,
                List.of(breaker.getId(), variador.getId()), null, null), usuario);
        ejecucionUseCase.registrarEjecucion(visita(estacionDos, "INSPECCION", null,
                List.of(variador.getId()), null, null), usuario);

        var conBreaker = ejecucionUseCase.listarEjecuciones(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                null, null, null, null, null, null, "ELECTRICO", null, breaker.getId(), PageRequest.of(0, 20));
        var conVariador = ejecucionUseCase.listarEjecuciones(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                null, null, null, null, null, null, "ELECTRICO", null, variador.getId(), PageRequest.of(0, 20));

        assertEquals(1, conBreaker.getTotalElements());
        assertEquals(estacionUno.getId(), conBreaker.getContent().get(0).estacionId());
        assertEquals(2, conVariador.getTotalElements());
    }

    @Test
    void elementosIntervenidos_cuentaVisitasYEstacionesDeMasAMenos() {
        ejecucionUseCase.registrarEjecucion(visita(estacionUno, "INSPECCION", null,
                List.of(breaker.getId(), variador.getId()), null, null), usuario);
        ejecucionUseCase.registrarEjecucion(visita(estacionDos, "INSPECCION", null,
                List.of(variador.getId()), null, null), usuario);
        ejecucionUseCase.registrarEjecucion(visita(estacionDos, "MANTENIMIENTO", null,
                List.of(variador.getId()), null, null), usuario);

        List<ElementoIntervenidoResponse> filas = indicadoresUseCase.elementosIntervenidos(2026, "ELECTRICO", null);

        assertEquals(2, filas.size());
        assertEquals(new ElementoIntervenidoResponse(variador.getId(), "Variador de velocidad", 3, 2), filas.get(0));
        assertEquals(new ElementoIntervenidoResponse(breaker.getId(), "Breaker", 1, 1), filas.get(1));
        assertEquals(1, indicadoresUseCase.elementosIntervenidos(2026, "ELECTRICO", estacionUno.getId())
                .stream().filter(f -> f.elementoId().equals(variador.getId())).findFirst().orElseThrow().intervenciones());
        assertTrue(indicadoresUseCase.elementosIntervenidos(2025, "ELECTRICO", null).isEmpty());
    }

    // ---------------------------------------------------------------------
    // Catálogo (Configuración)
    // ---------------------------------------------------------------------

    @Test
    void crearConjunto_normalizaElNombreYRechazaDuplicadosSinTildes() {
        ItemCatalogoResponse creado = catalogAdminUseCase.crearConjunto(
                new ItemCatalogoRequest("  red   de baja tensión ", "ELECTRICO"), usuario);

        assertEquals("Red de baja tensión", creado.nombre());
        assertEquals("ELECTRICO", creado.disciplina());
        assertFalse(creado.enUso());
        ResponseStatusException duplicado = assertThrows(ResponseStatusException.class, () ->
                catalogAdminUseCase.crearConjunto(new ItemCatalogoRequest("RED DE BAJA TENSION", "ELECTRICO"), usuario));
        assertEquals(409, duplicado.getStatusCode().value());
    }

    @Test
    void listarElementos_sinInactivosEsLoQueVeElMovil() {
        catalogAdminUseCase.cambiarEstadoElemento(breaker.getId(), false, usuario);

        List<String> movil = catalogAdminUseCase.listarElementos("ELECTRICO", false).stream()
                .map(ItemCatalogoResponse::nombre).toList();
        List<String> configuracion = catalogAdminUseCase.listarElementos("ELECTRICO", true).stream()
                .map(ItemCatalogoResponse::nombre).toList();

        assertEquals(List.of("Variador de velocidad"), movil);
        assertEquals(List.of("Breaker", "Variador de velocidad"), configuracion);
    }

    @Test
    void elementoYaUsado_noCambiaDeDisciplinaPeroSiDeNombre() {
        ejecucionUseCase.registrarEjecucion(
                visita(estacionUno, "INSPECCION", null, List.of(breaker.getId()), null, null), usuario);

        ResponseStatusException cambio = assertThrows(ResponseStatusException.class, () ->
                catalogAdminUseCase.actualizarElemento(breaker.getId(), new ItemCatalogoRequest("Breaker", "CIVIL"), usuario));
        assertEquals(409, cambio.getStatusCode().value());

        ItemCatalogoResponse renombrado = catalogAdminUseCase.actualizarElemento(breaker.getId(),
                new ItemCatalogoRequest("Breaker principal", "ELECTRICO"), usuario);
        assertEquals("Breaker principal", renombrado.nombre());
        assertTrue(renombrado.enUso());
    }

    @Test
    void conjuntoDesactivado_lasVisitasQueLoMarcaronLoConservan() {
        EjecucionResponse creada = ejecucionUseCase.registrarEjecucion(visita(estacionUno, "INSPECCION",
                List.of(motores.getId()), null, null, null), usuario);

        catalogAdminUseCase.cambiarEstadoConjunto(motores.getId(), false, usuario);
        entityManager.flush();
        entityManager.clear();

        assertEquals(List.of("Motores"), nombres(ejecucionUseCase.obtenerEjecucion(creada.id()).conjuntos()));
        assertTrue(Arrays.stream(catalogAdminUseCase.listarConjuntos("ELECTRICO", false).toArray(ItemCatalogoResponse[]::new))
                .noneMatch(c -> c.id().equals(motores.getId())));
    }

    @Test
    void conjunto_guardaElIconoElegido_vacioEsGenerico_yUnoDesconocidoEsUn400() {
        ItemCatalogoResponse creado = catalogAdminUseCase.crearConjunto(
                new ItemCatalogoRequest("Tablero de bombas", "ELECTRICO", "GABINETE"), usuario);
        assertEquals("GABINETE", creado.icono());

        ItemCatalogoResponse sinIcono = catalogAdminUseCase.actualizarConjunto(creado.id(),
                new ItemCatalogoRequest("Tablero de bombas", "ELECTRICO", " "), usuario);
        assertNull(sinIcono.icono());

        ResponseStatusException desconocido = assertThrows(ResponseStatusException.class, () ->
                catalogAdminUseCase.actualizarConjunto(creado.id(), new ItemCatalogoRequest("Tablero de bombas", "ELECTRICO", "CALAVERA"), usuario));
        assertEquals(400, desconocido.getStatusCode().value());
        assertNull(catalogAdminUseCase.crearElemento(new ItemCatalogoRequest("Celda", "ELECTRICO", "GABINETE"), usuario).icono());
    }

    @Test
    void listarSinFechas_muestraTambienUnaVisitaConFechaDeManana() {
        // El teléfono en UTC de noche guarda la fecha de mañana; igual debe aparecer en la lista.
        EjecucionRequest manana = new EjecucionRequest(LocalDate.now().plusDays(1), LocalDate.now().plusDays(1).getMonthValue(), 1,
                estacionUno.getId(), "ELECTRICO", "PREVENTIVO", "INSPECCION", inspeccionGeneral.getId(), null, null,
                "CONFORME", "Sin novedades", null, UUID.randomUUID(), null);
        EjecucionResponse creada = ejecucionUseCase.registrarEjecucion(manana, usuario);

        var pagina = ejecucionUseCase.listarEjecuciones(null, null, null, null, null, null, null, null, null,
                null, null, null, PageRequest.of(0, 20));

        assertTrue(pagina.getContent().stream().anyMatch(e -> e.id().equals(creada.id())));
    }
}
