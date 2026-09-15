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
import java.util.Set;
import java.util.UUID;

/**
 * Almacena fotos de evidencia bajo {@code uploads/subestaciones/ejecuciones/{ejecucionId}/{uuid}.ext}.
 * A diferencia de los documentos de vehículo, aquí no hay semántica de "reemplazar la actual":
 * cada foto subida es un archivo nuevo e independiente (1..N por ejecución).
 */
@Service
public class EvidenciaStorageService {

    private static final long MAX_BYTES = 15 * 1024 * 1024L;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

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
        if (!ALLOWED_TYPES.contains(mime)) {
            throw new IllegalArgumentException("Tipo de archivo no permitido. Use JPEG, PNG o WebP.");
        }

        byte[] bytes = file.getBytes();
        String hash = sha256Hex(bytes);

        Path dir = uploadsRoot.resolve("subestaciones").resolve("ejecuciones").resolve(String.valueOf(ejecucionId));
        Files.createDirectories(dir);

        String ext = resolveExtension(file.getOriginalFilename(), mime);
        String fileName = UUID.randomUUID() + ext;
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

    private static String resolveExtension(String originalFilename, String mime) {
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        }
        return switch (mime) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }
}
