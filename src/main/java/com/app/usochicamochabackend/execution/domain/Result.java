package com.app.usochicamochabackend.execution.domain;

import java.time.LocalDateTime;

public class Result {
    private Long id;
    private LocalDateTime fecha;
    private String tiempoEmpleado;
    private Long manosDeObraId;
    private Long repuestosId;

    // Getters y setters
    public Long getId() { return id; }

    public Result(Long id, LocalDateTime fecha, String tiempoEmpleado, Long manosDeObraId, Long repuestosId) {
        this.id = id;
        this.fecha = fecha;
        this.tiempoEmpleado = tiempoEmpleado;
        this.manosDeObraId = manosDeObraId;
        this.repuestosId = repuestosId;
    }

    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getTiempoEmpleado() { return tiempoEmpleado; }
    public void setTiempoEmpleado(String tiempoEmpleado) { this.tiempoEmpleado = tiempoEmpleado; }

    public Long getManosDeObraId() { return manosDeObraId; }
    public void setManosDeObraId(Long manosDeObraId) { this.manosDeObraId = manosDeObraId; }

    public Long getRepuestosId() { return repuestosId; }
    public void setRepuestosId(Long repuestosId) { this.repuestosId = repuestosId; }
}