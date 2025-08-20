package com.app.usochicamochabackend.execution.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "resultados")
public class ResultEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fecha;

    @Column(name = "tiempo_empleado")
    private String tiempoEmpleado;

    @OneToOne
    @JoinColumn(name = "mano_obra_id")
    private LaborEntity manoId;

    @OneToMany
    @JoinColumn(name = "resultado_id")
    private List<SparePartEntity> repuestoId;
}