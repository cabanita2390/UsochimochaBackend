package com.app.usochicamochabackend.substation.infrastructure.entity;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mant_programacion")
public class ProgramacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer anio;

    @Column(nullable = false)
    private Integer mes;

    @ManyToOne
    @JoinColumn(name = "estacion_id", nullable = false)
    private EstacionEntity estacion;

    @ManyToOne
    @JoinColumn(name = "actividad_id", nullable = false)
    private ActividadEntity actividad;

    @Builder.Default
    private Boolean status = true;

    /** BORRADOR / PUBLICADA / RETIRADA (CHECK en BD). Solo las PUBLICADA llegan al móvil. */
    @Column(nullable = false, length = 12)
    @Builder.Default
    private String estado = PUBLICADA;

    /** Sobre una PUBLICADA: "se quitará al publicar". El móvil la sigue viendo hasta publicar. */
    @Column(name = "pendiente_retiro", nullable = false)
    @Builder.Default
    private Boolean pendienteRetiro = false;

    @ManyToOne
    @JoinColumn(name = "publicada_en_id")
    private PublicacionEntity publicadaEn;

    @ManyToOne
    @JoinColumn(name = "retirada_en_id")
    private PublicacionEntity retiradaEn;

    @ManyToOne
    @JoinColumn(name = "creada_por")
    private UserEntity creadaPor;

    public static final String BORRADOR = "BORRADOR";
    public static final String PUBLICADA = "PUBLICADA";
    public static final String RETIRADA = "RETIRADA";
}
