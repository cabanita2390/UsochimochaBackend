package com.app.usochicamochabackend.execution.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @Column(name = "manos_de_obra_id")
    private Long manosDeObraId;

    @Column(name = "repuestos_id")
    private Long repuestosId;
}