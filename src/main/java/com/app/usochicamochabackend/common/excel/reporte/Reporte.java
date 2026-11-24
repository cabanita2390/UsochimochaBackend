package com.app.usochicamochabackend.common.excel.reporte;

import com.app.usochicamochabackend.common.excel.LibroExcel;
import com.app.usochicamochabackend.common.excel.Periodo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Un reporte armado con un constructor, para no escribir una clase por cada uno:
 * <pre>
 * Reporte.builder("ordenes-maquinaria", "Órdenes de trabajo · Maquinaria", ReporteExcel.FLOTA)
 *         .conPeriodo().roles(ReporteExcel.ROLES_TODOS)
 *         .llenando((libro, p) -> excel.ordenesMaquinaria(libro, cargar(), p.periodo()))
 *         .build();
 * </pre>
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
    private final String periodoInicial;
    private final List<FiltroReporte> filtros;
    private final List<ColumnaReporte> columnas;
    private final Set<String> roles;
    private final Function<PeticionReporte, Periodo> periodo;
    private final Function<PeticionReporte, String> tituloLibro;
    private final Function<PeticionReporte, String> nombre;
    private final Llenado llenado;

    private Reporte(Builder b) {
        this.clave = b.clave;
        this.titulo = b.titulo;
        this.modulo = b.modulo;
        this.usaPeriodo = b.usaPeriodo;
        this.periodoInicial = b.periodoInicial;
        this.filtros = List.copyOf(b.filtros);
        this.columnas = List.copyOf(b.columnas);
        this.roles = b.roles;
        this.periodo = b.periodo;
        this.tituloLibro = b.tituloLibro;
        this.nombre = b.nombre;
        this.llenado = b.llenado;
    }

    public static Builder builder(String clave, String titulo, String modulo) {
        return new Builder(clave, titulo, modulo);
    }

    @Override public String clave() { return clave; }
    @Override public String titulo() { return titulo; }
    @Override public String modulo() { return modulo; }
    @Override public boolean usaPeriodo() { return usaPeriodo; }
    @Override public String periodoInicial() { return periodoInicial; }
    @Override public List<FiltroReporte> filtros() { return filtros; }
    @Override public List<ColumnaReporte> columnas() { return columnas; }
    @Override public Set<String> roles() { return roles; }

    @Override
    public Periodo periodo(PeticionReporte p) {
        return periodo != null ? periodo.apply(p) : p.periodo();
    }

    @Override
    public String tituloLibro(PeticionReporte p) {
        return tituloLibro != null ? tituloLibro.apply(p) : titulo;
    }

    @Override
    public String nombreArchivo(PeticionReporte p) {
        return nombre != null ? nombre.apply(p) : ReporteExcel.super.nombreArchivo(p);
    }

    @Override
    public void llenar(LibroExcel libro, PeticionReporte p) throws IOException {
        llenado.llenar(libro, p);
    }

    public static final class Builder {
        private final String clave;
        private final String titulo;
        private final String modulo;
        private boolean usaPeriodo;
        private String periodoInicial = "anio";
        private final List<FiltroReporte> filtros = new ArrayList<>();
        private final List<ColumnaReporte> columnas = new ArrayList<>();
        private Set<String> roles = ReporteExcel.ROLES_WEB;
        private Function<PeticionReporte, Periodo> periodo;
        private Function<PeticionReporte, String> tituloLibro;
        private Function<PeticionReporte, String> nombre;
        private Llenado llenado;

        private Builder(String clave, String titulo, String modulo) {
            this.clave = clave;
            this.titulo = titulo;
            this.modulo = modulo;
        }

        /** Pide periodo; el panel abre en "Este año". */
        public Builder conPeriodo() {
            return conPeriodo("anio");
        }

        /** Pide periodo y abre en ese atajo ("mes", "todo"…). */
        public Builder conPeriodo(String inicial) {
            this.usaPeriodo = true;
            this.periodoInicial = inicial;
            return this;
        }

        public Builder filtro(FiltroReporte f) {
            filtros.add(f);
            return this;
        }

        public Builder columnas(List<ColumnaReporte> c) {
            columnas.addAll(c);
            return this;
        }

        public Builder roles(Set<String> r) {
            this.roles = r;
            return this;
        }

        public Builder periodo(Function<PeticionReporte, Periodo> f) {
            this.periodo = f;
            return this;
        }

        public Builder tituloLibro(Function<PeticionReporte, String> f) {
            this.tituloLibro = f;
            return this;
        }

        public Builder nombreArchivo(Function<PeticionReporte, String> f) {
            this.nombre = f;
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
