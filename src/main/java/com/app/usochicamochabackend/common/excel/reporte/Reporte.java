package com.app.usochicamochabackend.common.excel.reporte;

import com.app.usochicamochabackend.common.excel.LibroExcel;

import java.io.IOException;
import java.util.Set;

/**
 * Un reporte armado con un constructor, para no escribir una clase por cada uno:
 * <pre>
 * Reporte.builder("ordenes-maquinaria", "Órdenes de trabajo · Maquinaria", ReporteExcel.FLOTA)
 *         .conPeriodo().roles(ReporteExcel.ROLES_TODOS)
 *         .llenando((libro, p) -> excel.llenarOrdenesMaquinaria(libro, cargar(), p.periodo()))
 *         .build();
 * </pre>
 * Los reportes con filtros o columnas propias implementan {@link ReporteExcel} directamente.
 */
public final class Reporte implements ReporteExcel {

    /** Cómo llena el libro. */
    @FunctionalInterface
    public interface Llenado {
        void llenar(LibroExcel libro, PeticionReporte p) throws IOException;
    }

    private final String clave;
    private final String titulo;
    private final String modulo;
    private final boolean usaPeriodo;
    private final Set<String> roles;
    private final Llenado llenado;

    private Reporte(Builder b) {
        this.clave = b.clave;
        this.titulo = b.titulo;
        this.modulo = b.modulo;
        this.usaPeriodo = b.usaPeriodo;
        this.roles = b.roles;
        this.llenado = b.llenado;
    }

    public static Builder builder(String clave, String titulo, String modulo) {
        return new Builder(clave, titulo, modulo);
    }

    @Override public String clave() { return clave; }
    @Override public String titulo() { return titulo; }
    @Override public String modulo() { return modulo; }
    @Override public boolean usaPeriodo() { return usaPeriodo; }
    @Override public Set<String> roles() { return roles; }

    @Override
    public void llenar(LibroExcel libro, PeticionReporte p) throws IOException {
        llenado.llenar(libro, p);
    }

    public static final class Builder {
        private final String clave;
        private final String titulo;
        private final String modulo;
        private boolean usaPeriodo;
        private Set<String> roles = ReporteExcel.ROLES_WEB;
        private Llenado llenado;

        private Builder(String clave, String titulo, String modulo) {
            this.clave = clave;
            this.titulo = titulo;
            this.modulo = modulo;
        }

        /** Pide periodo; el panel abre en "Este año". */
        public Builder conPeriodo() {
            this.usaPeriodo = true;
            return this;
        }

        public Builder roles(Set<String> r) {
            this.roles = r;
            return this;
        }

        public Builder llenando(Llenado l) {
            this.llenado = l;
            return this;
        }

        public Reporte build() {
            if (llenado == null) {
                throw new IllegalStateException("El reporte " + clave + " no dice cómo se llena");
            }
            return new Reporte(this);
        }
    }
}
