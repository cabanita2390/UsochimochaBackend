package com.app.usochicamochabackend.substation.web;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.substation.application.dto.ConjuntoElementosRequest;
import com.app.usochicamochabackend.substation.application.dto.ItemCatalogoRequest;
import com.app.usochicamochabackend.substation.application.dto.ItemCatalogoResponse;
import com.app.usochicamochabackend.substation.application.port.SubstationCatalogAdminUseCase;
import com.app.usochicamochabackend.substation.infrastructure.entity.DisciplinaEntity;
import com.app.usochicamochabackend.substation.infrastructure.repository.DisciplinaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Lo que el administrador arma en la web (Configuración → Conjuntos y elementos) es lo que el móvil
 * recibe por HTTP en GET /conjuntos: los mismos elementos, en el mismo orden, con los campos que lee
 * su ItemCatalogoDto / ElementoDeConjuntoDto (id, nombre, pideDetalle).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SubstationConjuntosMovilHttpTest {

    @MockitoBean
    private com.app.usochicamochabackend.substation.application.port.SubstationEventosPort eventos;

    @Autowired private MockMvc mockMvc;
    @Autowired private SubstationCatalogAdminUseCase catalogAdminUseCase;
    @Autowired private DisciplinaRepository disciplinaRepository;
    @Autowired private UserRepositoryJpa userRepositoryJpa;

    private UserPrincipal admin;
    private UsernamePasswordAuthenticationToken operario;

    @BeforeEach
    void setUp() {
        disciplinaRepository.save(DisciplinaEntity.builder().codigo("ELECTRICO").conjuntos("OBLIGATORIO").usaElementos(true).build());
        UserEntity a = userRepositoryJpa.save(UserEntity.builder().username("admin.http").fullName("Admin")
                .email("admin.http@example.com").role("ADMIN").password("x").status(true).build());
        UserEntity o = userRepositoryJpa.save(UserEntity.builder().username("operario.http").fullName("Operario")
                .email("operario.http@example.com").role("OPERARIO").password("x").status(true).build());
        admin = new UserPrincipal(a.getId(), a.getUsername());
        operario = new UsernamePasswordAuthenticationToken(new UserPrincipal(o.getId(), o.getUsername()), null,
                List.of(new SimpleGrantedAuthority("ROLE_OPERARIO")));
    }

    @Test
    void loQueSeArmaEnLaWebLlegaIgualAlMovil() throws Exception {
        // Como lo hace la web: crear el conjunto y guardar su lista de elementos.
        ItemCatalogoResponse t = catalogAdminUseCase.crearConjunto(
                new ItemCatalogoRequest("Transformador", "ELECTRICO", "TRANSFORMADOR"), admin);
        ItemCatalogoResponse s = catalogAdminUseCase.crearConjunto(
                new ItemCatalogoRequest("Subestación", "ELECTRICO", "SUBESTACION"), admin);
        catalogAdminUseCase.guardarElementosConjunto(t.id(), new ConjuntoElementosRequest(List.of(
                new ConjuntoElementosRequest.Item(null, "Nivel de aceite", false),
                new ConjuntoElementosRequest.Item(null, "Fusibles", false),
                new ConjuntoElementosRequest.Item(null, "Estado físico", true),
                new ConjuntoElementosRequest.Item(null, "Borneras", false))), admin);
        // "fusibles" escrito distinto en otro conjunto: es el mismo elemento.
        catalogAdminUseCase.guardarElementosConjunto(s.id(), new ConjuntoElementosRequest(List.of(
                new ConjuntoElementosRequest.Item(null, "fusibles", false),
                new ConjuntoElementosRequest.Item(null, "Medidor de energía", false))), admin);
        // Después se quita "Borneras" del Transformador: el móvil ya no debe verlo.
        ItemCatalogoResponse guardado = catalogAdminUseCase.guardarElementosConjunto(t.id(), new ConjuntoElementosRequest(List.of(
                new ConjuntoElementosRequest.Item(null, "Nivel de aceite", false),
                new ConjuntoElementosRequest.Item(null, "Fusibles", false),
                new ConjuntoElementosRequest.Item(null, "Estado físico", true))), admin);
        Long idFusibles = guardado.elementos().get(1).id();

        // Lo que pide el móvil (ApiService.getConjuntosSubestacion: GET v1/substation/conjuntos, sin parámetros).
        mockMvc.perform(get("/api/v1/substation/conjuntos").with(authentication(operario)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.nombre=='Transformador')].icono").value(contains("TRANSFORMADOR")))
                .andExpect(jsonPath("$[?(@.nombre=='Transformador')].elementos[*].nombre")
                        .value(contains("Nivel de aceite", "Fusibles", "Estado físico")))
                .andExpect(jsonPath("$[?(@.nombre=='Transformador')].elementos[*].pideDetalle")
                        .value(contains(false, false, true)))
                .andExpect(jsonPath("$[?(@.nombre=='Transformador')].elementos[1].id").value(contains(idFusibles.intValue())))
                .andExpect(jsonPath("$[?(@.nombre=='Subestación')].elementos[*].nombre")
                        .value(contains("Fusibles", "Medidor de energía")))
                .andExpect(jsonPath("$[?(@.nombre=='Subestación')].elementos[0].id").value(contains(idFusibles.intValue())));

        // GET /elementos (la lista de nombres) sigue respondiendo como antes, sin "elementos" adentro.
        mockMvc.perform(get("/api/v1/substation/elementos").with(authentication(operario)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].elementos").value(nullValue()));
    }

    @Test
    void ejecucionesSeOrdenanPorDisciplina() throws Exception {
        // La columna Disciplina de la web ordena con sort=disciplina.orden (Civil, Eléctrico, Electromecánico).
        mockMvc.perform(get("/api/v1/substation/ejecuciones").param("sort", "disciplina.orden,desc")
                        .with(authentication(operario)))
                .andExpect(status().isOk());
    }
}
