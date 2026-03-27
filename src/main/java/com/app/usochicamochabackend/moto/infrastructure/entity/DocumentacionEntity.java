package com.app.usochicamochabackend.moto.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "documentacion_y_elementos")
public class DocumentacionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_documento")
    private Integer id;

    @Column(name = "id_vehiculo")
    private Integer idVehiculo;

    @Column(name = "tipo_documento", insertable = true, updatable = true)
    private String tipoDocumento;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "imagen_url")
    private String imagenUrl;

    private Boolean activo;

    // Stores first day of the validity month (YYYY-MM-01)
    // Column name without special characters to avoid JDBC issues
    @Column(name = "mesyear")
    private LocalDate mesyear;
}
