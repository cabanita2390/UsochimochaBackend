package com.app.usochicamochabackend.substation.application.dto;

import com.app.usochicamochabackend.substation.infrastructure.entity.OrdenCostoEntity;

import java.math.BigDecimal;

public record OrdenCostoResponse(Long id, String tipo, String descripcion, String proveedor, BigDecimal cantidad,
        BigDecimal valorTotal) {

    public static OrdenCostoResponse fromEntity(OrdenCostoEntity c) {
        return new OrdenCostoResponse(c.getId(), c.getTipo(), c.getDescripcion(), c.getProveedor(), c.getCantidad(),
                c.getValorTotal());
    }
}
