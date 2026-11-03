package com.app.usochicamochabackend.substation.application.dto;

import com.app.usochicamochabackend.substation.infrastructure.entity.OrdenTrabajoEntity;

/** La orden de trabajo vigente de un hallazgo, embebida en el seguimiento y en la ejecución. */
public record OrdenRefResponse(Long id, String consecutivo, String estado) {

    public static OrdenRefResponse fromEntity(OrdenTrabajoEntity orden) {
        return orden == null ? null : new OrdenRefResponse(orden.getId(), orden.getConsecutivo(), orden.getEstado());
    }
}
