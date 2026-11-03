package com.app.usochicamochabackend.common.text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InputTextNormalizerTest {

    @Test
    @DisplayName("normalizePlaca: convierte a mayúsculas y elimina espacios")
    void normalizePlaca_CasoNormal() {
        assertEquals("ABC123", InputTextNormalizer.normalizePlaca("abc 123"));
        assertEquals("ABC123", InputTextNormalizer.normalizePlaca("  ABC123  "));
        assertEquals("XYZ999", InputTextNormalizer.normalizePlaca("xyz999"));
    }

    @Test
    @DisplayName("normalizePlaca: null devuelve cadena vacía")
    void normalizePlaca_Null() {
        assertEquals("", InputTextNormalizer.normalizePlaca(null));
    }

    @Test
    @DisplayName("normalizeTitleWords: capitaliza primera letra de cada palabra")
    void normalizeTitleWords_CasoNormal() {
        assertEquals("Distrito De Riego", InputTextNormalizer.normalizeTitleWords("distrito de riego"));
        assertEquals("Usochicamocha", InputTextNormalizer.normalizeTitleWords("USOCHICAMOCHA"));
    }

    @Test
    @DisplayName("normalizeTitleWords: null o en blanco devuelve null")
    void normalizeTitleWords_NullBlank() {
        assertNull(InputTextNormalizer.normalizeTitleWords(null));
        assertNull(InputTextNormalizer.normalizeTitleWords("   "));
    }

    @Test
    @DisplayName("normalizeIdCode: elimina espacios y convierte a mayúsculas; null si queda vacío")
    void normalizeIdCode_CasoNormal() {
        assertEquals("ENG001", InputTextNormalizer.normalizeIdCode(" eng 001 "));
        assertNull(InputTextNormalizer.normalizeIdCode(""));
        assertNull(InputTextNormalizer.normalizeIdCode(null));
    }

    @Test
    @DisplayName("normalizeFreeTextPreserveCase: conserva mayúsculas/minúsculas, colapsa espacios")
    void normalizeFreeTextPreserveCase_CasoNormal() {
        assertEquals("15W-40 sintético", InputTextNormalizer.normalizeFreeTextPreserveCase("  15W-40   sintético  "));
        assertNull(InputTextNormalizer.normalizeFreeTextPreserveCase("   "));
    }

    @Test
    @DisplayName("errorPlaca: acepta letras, números y guion (máx. 10)")
    void errorPlaca_Validas() {
        assertNull(InputTextNormalizer.errorPlaca("ABC123"));
        assertNull(InputTextNormalizer.errorPlaca("SDZ53F"));
        assertNull(InputTextNormalizer.errorPlaca("R-12345"));
        assertNull(InputTextNormalizer.errorPlaca(InputTextNormalizer.normalizePlaca(" abc 12d ")));
    }

    @Test
    @DisplayName("errorPlaca: rechaza símbolos que rompen la URL, vacía y más de 10 caracteres")
    void errorPlaca_Invalidas() {
        assertTrue(InputTextNormalizer.errorPlaca("SDZ%#F").contains("solo puede tener letras, números y guion"));
        assertNotNull(InputTextNormalizer.errorPlaca("ABC/12"));
        assertNotNull(InputTextNormalizer.errorPlaca("ÑAB123"));
        assertEquals("La placa es obligatoria", InputTextNormalizer.errorPlaca(""));
        assertEquals("La placa es obligatoria", InputTextNormalizer.errorPlaca(null));
        assertEquals("La placa admite máximo 10 caracteres", InputTextNormalizer.errorPlaca("ABCDEFGHIJK"));
    }
}
