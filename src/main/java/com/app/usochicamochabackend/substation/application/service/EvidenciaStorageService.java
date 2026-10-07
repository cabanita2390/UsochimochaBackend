package com.app.usochicamochabackend.substation.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Almacena fotos de evidencia bajo {@code uploads/subestaciones/ejecuciones/{ejecucionId}/{uuid}.ext}.
 * A diferencia de los documentos de vehículo, aquí no hay semántica de "reemplazar la actual":
 * cada foto subida es un archivo nuevo e independiente (1..N por ejecución).
 */
@Service
public class EvidenciaStorageService {

    private static final long MAX_BYTES = 15 * 1024 * 1024L;
    /** Tipos permitidos y la extensión con la que se guardan (nunca la del nombre original). */
    private static final Map<String, String> EXTENSION_POR_TIPO = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp");

    private final Path uploadsRoot;

    public EvidenciaStorageService(@Value("${app.storage.uploads-root:uploads}") String uploadsRootProperty) {
        this.uploadsRoot = Paths.get(uploadsRootProperty).toAbsolutePath().normalize();
    }

    public record StoredFile(String rutaRelativa, String nombreOriginal, String hashSha256) {
    }

    /**
     * Guarda el archivo y calcula su SHA-256 (usado por SubstationService para detectar
     * subidas duplicadas de la misma foto para la misma ejecución — ver V39). Siempre
     * escribe a disco; si el llamador determina después que era un duplicado, debe
     * limpiar el archivo con {@link #delete(String)}.
     */
    public StoredFile store(MultipartFile file, Long ejecucionId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Archivo vacío.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("El archivo supera el tamaño máximo permitido (15 MB).");
        }
        String mime = file.getContentType() != null ? file.getContentType().toLowerCase(Locale.ROOT) : "";
        if (!EXTENSION_POR_TIPO.containsKey(mime)) {
            throw new IllegalArgumentException("Tipo de archivo no permitido. Use JPEG, PNG o WebP.");
        }

        byte[] bytes = file.getBytes();
        // El Content-Type lo declara el cliente: se comprueba que el contenido sea de verdad
        // esa imagen. Sin esto, un HTML enviado como image/png se guardaba y se servía.
        if (!contenidoCoincide(mime, bytes)) {
            throw new IllegalArgumentException("El archivo no es una imagen JPEG, PNG o WebP válida.");
        }
        String hash = sha256Hex(bytes);

        Path dir = uploadsRoot.resolve("subestaciones").resolve("ejecuciones").resolve(String.valueOf(ejecucionId));
        Files.createDirectories(dir);

        String fileName = UUID.randomUUID() + EXTENSION_POR_TIPO.get(mime);
        Path destino = dir.resolve(fileName);

        Files.write(destino, bytes);

        // Ruta PÚBLICA con el prefijo "/uploads/" incluido — igual convención que
        // VehicleDocumentStorageService/FuelDocumentStorageService (ruta hardcodeada,
        // no relativize() contra uploadsRoot, que por definición nunca incluye ese
        // prefijo). Antes de este fix, rutaArchivo se guardaba SIN el prefijo — el
        // navegador/app pedían la imagen sin "/uploads/" y el backend respondía 403.
        String rutaRelativa = "/uploads/subestaciones/ejecuciones/" + ejecucionId + "/" + fileName;
        String nombreOriginal = file.getOriginalFilename() != null ? file.getOriginalFilename() : fileName;
        return new StoredFile(rutaRelativa, nombreOriginal, hash);
    }

    /**
     * Borra un archivo ya guardado, identificado por la ruta pública de {@link StoredFile}
     * (con o sin el prefijo "/uploads/" — acepta ambas para no romper con filas guardadas
     * antes de V40, que no lo tenían).
     */
    public void delete(String rutaRelativa) throws IOException {
        String rutaFisica = rutaRelativa.startsWith("/uploads/")
                ? rutaRelativa.substring("/uploads/".length())
                : rutaRelativa;
        Files.deleteIfExists(uploadsRoot.resolve(rutaFisica));
    }

    private static String sha256Hex(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    /** Firma de los primeros bytes de cada formato permitido. */
    static boolean contenidoCoincide(String mime, byte[] b) {
        return switch (mime) {
            case "image/jpeg" -> empiezaCon(b, 0, 0xFF, 0xD8, 0xFF);
            case "image/png" -> empiezaCon(b, 0, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A);
            case "image/webp" -> empiezaCon(b, 0, 'R', 'I', 'F', 'F') && empiezaCon(b, 8, 'W', 'E', 'B', 'P');
            default -> false;
        };
    }

    private static boolean empiezaCon(byte[] b, int desde, int... firma) {
        if (b.length < desde + firma.length) {
            return false;
        }
        for (int i = 0; i < firma.length; i++) {
            if ((b[desde + i] & 0xFF) != firma[i]) {
                return false;
            }
        }
        return true;
    }
}
