package com.app.usochicamochabackend.performance.domain;

public class SparePart {
    private Long id;
    private String ref;
    private String nombre;
    private String cantidad;
    private String precio;

    public SparePart() {
    }

    public SparePart(Long id, String ref, String nombre, String cantidad, String precio) {
        this.id = id;
        this.ref = ref;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precio = precio;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCantidad() {
        return cantidad;
    }

    public void setCantidad(String cantidad) {
        this.cantidad = cantidad;
    }

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }
}
