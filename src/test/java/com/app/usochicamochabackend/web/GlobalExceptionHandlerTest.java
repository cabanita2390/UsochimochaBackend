package com.app.usochicamochabackend.web;

import com.app.usochicamochabackend.exception.BadRequestException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Códigos HTTP que GlobalExceptionHandler asigna a cada excepción. */
class GlobalExceptionHandlerTest {

    @RestController
    static class Lanzador {
        @GetMapping("/bad-request")
        String badRequest() {
            throw new BadRequestException("resultado inválido: MAL");
        }

        @GetMapping("/archivo-grande")
        String archivoGrande() {
            throw new MaxUploadSizeExceededException(15 * 1024 * 1024L);
        }

        @GetMapping("/orden-invalido")
        String ordenInvalido() {
            throw new PropertyReferenceException("noexiste",
                    org.springframework.data.util.TypeInformation.of(Lanzador.class), java.util.List.of());
        }
    }

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new Lanzador())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void badRequestException_responde400ConSuMensaje() throws Exception {
        mockMvc.perform(get("/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("resultado inválido: MAL"));
    }

    @Test
    void archivoDemasiadoGrande_responde413ConMensaje() throws Exception {
        mockMvc.perform(get("/archivo-grande"))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(content().string("El archivo supera el tamaño máximo permitido (15 MB)."));
    }

    @Test
    void ordenamientoPorCampoInexistente_responde400() throws Exception {
        mockMvc.perform(get("/orden-invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Campo de ordenamiento inválido: noexiste"));
    }
}
