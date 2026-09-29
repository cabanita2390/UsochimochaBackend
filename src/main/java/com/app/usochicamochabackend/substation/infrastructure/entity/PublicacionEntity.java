package com.app.usochicamochabackend.substation.infrastructure.entity;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Una publicación del cronograma de un año hacia el móvil. La "Carga inicial" (inicial = true)
 * agrupa las citas que ya existían al migrar: no tiene usuario y nunca se deshace.
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mant_publicacion")
public class PublicacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer anio;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private UserEntity usuario; // null solo en la carga inicial (CHECK en BD)

    @Column(name = "publicado_en", nullable = false)
    private LocalDateTime publicadoEn;

    @Column(nullable = false)
    private Integer altas;

    @Column(nullable = false)
    private Integer bajas;

    @Column(nullable = false)
    @Builder.Default
    private Boolean inicial = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean revertida = false;

    @ManyToOne
    @JoinColumn(name = "revertida_por")
    private UserEntity revertidaPor;

    @Column(name = "revertida_en")
    private LocalDateTime revertidaEn;
}
