package com.app.usochicamochabackend.vehicle.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cat_referencias_aceite")
public class ReferenciaAceiteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aceite")
    private Integer idAceite;

    private String descripcion;

    @Column(name = "filtro_aire")
    private String filtroAire;

    @Column(name = "estado_aceite")
    private String estadoAceite;

    @Column(name = "fecha_cambio")
    private java.time.LocalDate fechaCambio;

    @Column(name = "ultimo_kilometro")
    private String ultimoKilometro;

    @Column(name = "kilometrocambio")
    private String kilometroCambio;

    @Column(name = "kilometrocambioprox")
    private String kilometroCambioProx;

    private Boolean activo;
}
