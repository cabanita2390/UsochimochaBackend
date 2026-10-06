package com.app.usochicamochabackend.web;

import com.app.usochicamochabackend.exception.BadRequestException;
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
}
