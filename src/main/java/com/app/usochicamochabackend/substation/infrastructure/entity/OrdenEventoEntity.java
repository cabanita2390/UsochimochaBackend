package com.app.usochicamochabackend.substation.infrastructure.entity;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/** Un paso en la línea de tiempo de una orden de trabajo (también es su auditoría). */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mant_orden_evento")
public class OrdenEventoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "orden_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OrdenTrabajoEntity orden;

    @Column(nullable = false, length = 30)
    private String tipo; // CREADA / EDITADA / EJECUTADA / DEVUELTA / CERRADA / ANULADA / EJECUCION_FUERA_DE_ESTADO

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @ManyToOne
    @JoinColumn(name = "ejecucion_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private EjecucionEntity ejecucion;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private UserEntity usuario;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;
}
