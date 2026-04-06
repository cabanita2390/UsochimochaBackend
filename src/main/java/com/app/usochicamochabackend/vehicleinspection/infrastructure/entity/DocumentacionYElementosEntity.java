package com.app.usochicamochabackend.vehicleinspection.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Tabla: documentacion_y_elementos
 * Almacena un registro por cada tipo de documento del vehículo:
 * SOAT, TECNO, LICENCIA, EXTINTOR.
 * Campo fecha_vencimiento es NOT NULL en la BD.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "documentacion_y_elementos")
public class DocumentacionYElementosEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_documento")
    private Integer idDocumento;

    @Column(name = "id_vehiculo")
    private Integer idVehiculo;

    /** Valores esperados: 'SOAT', 'TECNO', 'LICENCIA', 'EXTINTOR' */
    @Column(name = "tipo_documento", length = 50)
    private String tipoDocumento;

    /** NOT NULL en BD — siempre debe tener un valor */
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    /** URI de la imagen enviada desde el dispositivo */
    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;

    /** Estado del documento: Vigente / Próximo a Vencer / Vencido */
    @Column(name = "estadodatos", length = 255)
    private String estadoDatos;

    /**
     * Vigencia del extintor en formato date (derivado de vigenciaExtintor
     * "YYYY-MM")
     */
    @Column(name = "fecha_extintor")
    private LocalDate fechaExtintor;

    @Column(name = "activo")
    private Boolean activo;

    /** Mes/año adicional (usado para extintor) */
    @Column(name = "mesyear")
    private LocalDate mesyear;
}
