package com.app.usochicamochabackend.auth.security.filter;

import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.auth.utils.JwtUtils;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * /uploads/** (fotos de evidencia, documentos de vehículos, licencias, facturas) era de acceso
 * público: cualquiera con la URL veía el archivo. Ahora se exige una credencial: el header
 * Authorization (lo valida JwtTokenValidator antes de este filtro) o el parámetro {@code ?k=}
 * con el token de archivos que entregan el login y el refresh (las etiquetas img y
 * window.open no pueden enviar headers).
 *
 * Modos (propiedad {@code app.uploads.proteccion}, por defecto {@code registrar}):
 * <ul>
 *   <li>{@code registrar}: deja pasar todo, pero anota en el log cada acceso sin credencial. Sirve
 *       para desplegar sin romper nada y ver si algún cliente o pantalla quedó sin actualizar.</li>
 *   <li>{@code exigir}: responde 401 a lo que llegue sin credencial válida. Activarlo cuando la web y
 *       el APK que envían {@code ?k=} ya estén desplegados y el log no muestre accesos sin credencial.</li>
 * </ul>
 */
public class UploadsAccessFilter extends OncePerRequestFilter {

    private static final Logger logger = Logger.getLogger(UploadsAccessFilter.class.getName());
    public static final String MODO_EXIGIR = "exigir";

    private final JwtUtils jwtUtils;
    private final UserRepositoryJpa userRepository;
    private final boolean exigir;

    public UploadsAccessFilter(JwtUtils jwtUtils, UserRepositoryJpa userRepository, String modo) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.exigir = MODO_EXIGIR.equalsIgnoreCase(modo == null ? "" : modo.trim());
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !request.getRequestURI().startsWith(request.getContextPath() + "/uploads/");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (autenticadoPorHeader() || tokenDeArchivosValido(request.getParameter("k"))) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!exigir) {
            logger.warning("⚠️ [UPLOADS] Acceso sin credencial a " + request.getRequestURI()
                    + " (modo registrar: se permite; con app.uploads.proteccion=exigir sería 401)");
            filterChain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\": \"Se requiere autenticación para ver este archivo\"}");
    }

    private boolean autenticadoPorHeader() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }

    private boolean tokenDeArchivosValido(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            DecodedJWT jwt = jwtUtils.verifyToken(token);
            Long userId = jwt.getClaim("userId").asLong();
            return jwtUtils.sirveParaArchivos(jwt) && userId != null && userRepository.existsActiveById(userId);
        } catch (Exception e) {
            return false;
        }
    }
}
