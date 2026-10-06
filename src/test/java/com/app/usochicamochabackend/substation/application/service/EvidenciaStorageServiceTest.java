package com.app.usochicamochabackend.substation.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvidenciaStorageServiceTest {

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 16, 'J', 'F', 'I', 'F'};

    @TempDir
    Path raiz;

    @Test
    void guardaConLaExtensionDelTipo_noLaDelNombre() throws Exception {
        var servicio = new EvidenciaStorageService(raiz.toString());

        var guardado = servicio.store(new MockMultipartFile("file", "x.html", "image/jpeg", JPEG), 7L);

        assertTrue(guardado.rutaRelativa().endsWith(".jpg"), guardado.rutaRelativa());
        assertEquals("x.html", guardado.nombreOriginal());
        try (var archivos = Files.list(raiz.resolve("subestaciones/ejecuciones/7"))) {
            assertTrue(archivos.allMatch(p -> p.toString().endsWith(".jpg")));
        }
    }

    @Test
    void rechazaHtmlDeclaradoComoImagen() {
        var servicio = new EvidenciaStorageService(raiz.toString());
        var html = new MockMultipartFile("file", "x.html", "image/png", "<script>alert(1)</script>".getBytes());

        assertThrows(IllegalArgumentException.class, () -> servicio.store(html, 7L));
        assertTrue(Files.notExists(raiz.resolve("subestaciones")));
    }

    @Test
    void reconoceLasFirmasDeLosTresFormatos() {
        assertTrue(EvidenciaStorageService.contenidoCoincide("image/jpeg", JPEG));
        assertTrue(EvidenciaStorageService.contenidoCoincide("image/png",
                new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A}));
        assertTrue(EvidenciaStorageService.contenidoCoincide("image/webp",
                new byte[]{'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'}));
        assertTrue(!EvidenciaStorageService.contenidoCoincide("image/png", JPEG));
    }
}
