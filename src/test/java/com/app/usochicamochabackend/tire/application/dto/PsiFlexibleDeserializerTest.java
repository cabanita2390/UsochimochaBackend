package com.app.usochicamochabackend.tire.application.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PsiFlexibleDeserializerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private LecturaPresionLlantaRequest leer(String psi) throws Exception {
        return mapper.readValue("{\"codigoPosicion\":\"DI\",\"psi\":" + psi + "}", LecturaPresionLlantaRequest.class);
    }

    @Test
    void aceptaNumerosYTextoConPuntoOComa() throws Exception {
        assertEquals(new BigDecimal("35.5"), leer("35.5").psi());
        assertEquals(new BigDecimal("35"), leer("\"35\"").psi());
        assertEquals(new BigDecimal("35.5"), leer("\" 35,5 \"").psi());
    }

    @Test
    void loQueNoEsUnaPresionQuedaNullSinRechazarLaInspeccion() throws Exception {
        assertNull(leer("\"abc\"").psi());
        assertNull(leer("true").psi());
        assertNull(leer("{\"x\":1}").psi());
        assertNull(leer("null").psi());
    }
}
