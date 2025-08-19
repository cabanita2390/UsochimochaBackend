package com.app.usochicamochabackend.execution.domain;

import java.time.LocalDateTime;

public class Labor {
    private Long id;
    private LocalDateTime fecha;
    private String precio;
    private Boolean mecanico;
    private String contratista;

    public Labor() {
    }

    public Labor(Long id, LocalDateTime fecha, String precio, Boolean mecanico, String contratista) {
        this.id = id;
        this.fecha = fecha;
        this.precio = precio;
        this.mecanico = mecanico;
        this.contratista = contratista;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }

    public Boolean getMecanico() {
        return mecanico;
    }

    public void setMecanico(Boolean mecanico) {
        this.mecanico = mecanico;
    }

    public String getContratista() {
        return contratista;
    }

    public void setContratista(String contratista) {
        this.contratista = contratista;
    }

}
