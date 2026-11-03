package com.app.usochicamochabackend.common.text;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Normalización de textos de entrada para evitar duplicados por mayúsculas/minúsculas
 * y unificar presentación (placas, nombres de catálogo, etc.).
 */
public final class InputTextNormalizer {

    private InputTextNormalizer() {}

    private static final Pattern PLACA_VALIDA = Pattern.compile("^[A-Z0-9-]{1,10}$");

    /**
     * Placa: sin espacios, todo en mayúsculas (letras y números tal cual tras limpiar espacios).
     */
    public static String normalizePlaca(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }

    /**
     * Placa ya normalizada: solo letras, números y guion, máximo 10 caracteres. Un símbolo
     * como "%" deja la placa inutilizable en las rutas que la llevan (GET /moto/{placa}): el
     * firewall de Spring Security rechaza la URL antes de llegar al controlador.
     *
     * @return el mensaje para el usuario, o {@code null} si la placa es válida
     */
    public static String errorPlaca(String placaNormalizada) {
        if (placaNormalizada == null || placaNormalizada.isEmpty()) {
            return "La placa es obligatoria";
        }
        if (placaNormalizada.length() > 10) {
            return "La placa admite máximo 10 caracteres";
        }
        if (!PLACA_VALIDA.matcher(placaNormalizada).matches()) {
            return "La placa '" + placaNormalizada + "' solo puede tener letras, números y guion (ej. ABC123 o ABC12D)";
        }
        return null;
    }

    /**
     * Códigos de catálogo (p. ej. tipo de vehículo): trim, espacios colapsados, mayúsculas.
     */
    public static String normalizeUpperToken(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return raw.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }

    /**
     * Texto libre sin cambiar mayúsculas/minúsculas relativas (p. ej. viscosidad {@code 15W-40}): trim y espacios colapsados.
     */
    public static String normalizeFreeTextPreserveCase(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return raw.trim().replaceAll("\\s+", " ");
    }

    /**
     * Identificadores alfanuméricos (motor, interno): sin espacios, mayúsculas; {@code null} si queda vacío.
     */
    public static String normalizeIdCode(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String t = normalizePlaca(raw);
        return t.isEmpty() ? null : t;
    }

    /**
     * Nombres legibles (marca, área, ubicación, pertenece a): primera letra de cada palabra en mayúscula.
     * Tokens que empiezan por dígito no se alteran en mayúsculas/minúsculas (p. ej. "15W-40").
     * {@code null} si queda vacío tras trim.
     */
    public static String normalizeTitleWords(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String collapsed = raw.trim().replaceAll("\\s+", " ");
        return Arrays.stream(collapsed.split(" "))
                .filter(w -> !w.isEmpty())
                .map(InputTextNormalizer::hyphenAwareTitleWord)
                .collect(Collectors.joining(" "));
    }

    private static String hyphenAwareTitleWord(String word) {
        if (!word.contains("-")) {
            return smartWordTitle(word);
        }
        return Arrays.stream(word.split("-"))
                .map(s -> s.isEmpty() ? s : smartWordTitle(s))
                .collect(Collectors.joining("-"));
    }

    private static String smartWordTitle(String word) {
        if (word.isEmpty()) {
            return word;
        }
        char c0 = word.charAt(0);
        if (Character.isDigit(c0)) {
            return word;
        }
        return Character.toUpperCase(c0) + word.substring(1).toLowerCase(Locale.ROOT);
    }
}
