package com.app.usochicamochabackend.tire.application.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * PSI de una lectura: número o texto ("35", "35,5"). Lo que no se puede leer queda null y esa lectura
 * se omite; antes un solo valor mal escrito rechazaba con 400 toda la inspección preoperativa.
 */
public class PsiFlexibleDeserializer extends JsonDeserializer<BigDecimal> {

    @Override
    public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken t = p.currentToken();
        if (t == JsonToken.VALUE_NUMBER_INT || t == JsonToken.VALUE_NUMBER_FLOAT) {
            return p.getDecimalValue();
        }
        if (t == JsonToken.VALUE_STRING) {
            try {
                return new BigDecimal(p.getText().trim().replace(',', '.'));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        p.skipChildren(); // objeto, arreglo o booleano: no es una presión
        return null;
    }
}
