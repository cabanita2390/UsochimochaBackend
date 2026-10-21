package com.app.usochicamochabackend.config;

import com.app.usochicamochabackend.exception.BadRequestException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Locale;
import java.util.Set;

/**
 * Spring Data deja ordenar por cualquier campo, también de tablas relacionadas
 * (?sort=usuario.password). Ordenar por el hash de la contraseña no lo muestra, pero deja
 * deducirlo comparando posiciones en la lista. Se rechaza en todos los listados paginados.
 */
@Component
public class SortSeguroInterceptor implements HandlerInterceptor {

    /** Campos por los que nunca se ordena, en cualquier nivel (usuario.password, x.creadoPor.password). */
    static final Set<String> CAMPOS_PROHIBIDOS = Set.of("password");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String[] valores = request.getParameterValues("sort");
        if (valores == null) {
            return true;
        }
        for (String valor : valores) {
            // "campo,desc" o varios campos separados por coma: "fecha,id,desc".
            for (String parte : valor.split(",")) {
                for (String segmento : parte.trim().split("\\.")) {
                    if (CAMPOS_PROHIBIDOS.contains(segmento.trim().toLowerCase(Locale.ROOT))) {
                        throw new BadRequestException("No se puede ordenar por " + parte.trim() + ".");
                    }
                }
            }
        }
        return true;
    }
}
