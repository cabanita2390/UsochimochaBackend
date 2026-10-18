package com.app.usochicamochabackend.substation.application.dto;

import com.app.usochicamochabackend.substation.infrastructure.entity.OrdenEventoEntity;

import java.time.LocalDateTime;

public record OrdenEventoResponse(Long id, String tipo, String comentario, Long ejecucionId, String usuario,
        LocalDateTime creadoEn) {

    public static OrdenEventoResponse fromEntity(OrdenEventoEntity e) {
        return new OrdenEventoResponse(e.getId(), e.getTipo(), e.getComentario(),
                e.getEjecucion() != null ? e.getEjecucion().getId() : null,
                Nombres.de(e.getUsuario()), e.getCreadoEn());
    }
}
