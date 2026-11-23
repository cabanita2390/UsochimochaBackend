package com.app.usochicamochabackend.common.excel;

import com.app.usochicamochabackend.auth.application.dto.UserPrincipal;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;

/** Lo que repiten los controladores al devolver un Excel: encabezados, nombre del archivo y quién descarga. */
public final class RespuestaExcel {

    private RespuestaExcel() {
    }

    /** El archivo como descarga, con el nombre dado (sin tildes raras en el encabezado). */
    public static ResponseEntity<byte[]> de(byte[] contenido, String nombre) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.parseMediaType(LibroExcel.TIPO_CONTENIDO));
        h.setContentDisposition(ContentDisposition.attachment().filename(nombre, StandardCharsets.UTF_8).build());
        h.setContentLength(contenido.length);
        return ResponseEntity.ok().headers(h).body(contenido);
    }

    /** "estaciones_ejecuciones" + periodo → "estaciones_ejecuciones_2026-01-01_a_2026-12-31.xlsx". */
    public static String nombre(String base, Periodo periodo) {
        return base + (periodo == null ? "" : periodo.sufijoArchivo()) + ".xlsx";
    }

    /** Quién está descargando (sale en la portada del Excel). */
    public static String usuarioActual() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null) {
            return "—";
        }
        return a.getPrincipal() instanceof UserPrincipal u ? u.username() : a.getName();
    }
}
