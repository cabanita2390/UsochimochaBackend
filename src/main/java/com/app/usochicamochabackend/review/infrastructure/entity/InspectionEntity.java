package com.app.usochicamochabackend.review.infrastructure.entity;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "inspecciones")
public class InspectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uuid;

    @Column(name = "date_stamp")
    private LocalDateTime dateStamp;

    private String hourmeter;

    @Column(name = "estado_fugas")             private String estadoFugas;
    @Column(name = "estado_frenos")            private String estadoFrenos;
    @Column(name = "estado_correas_poleas")    private String estadoCorreasPoleas;
    @Column(name = "estado_llantas_carriles")  private String estadoLlantasCarriles;
    @Column(name = "estado_encendido")         private String estadoEncendido;
    @Column(name = "estado_electrico")         private String estadoElectrico;
    @Column(name = "estado_mecanico")          private String estadoMecanico;
    @Column(name = "estado_temperatura")       private String estadoTemperatura;
    @Column(name = "estado_aceite")            private String estadoAceite;
    @Column(name = "estado_hidraulico")        private String estadoHidraulico;
    @Column(name = "estado_refrigerante")      private String estadoRefrigerante;
    @Column(name = "estado_estructural")       private String estadoEstructural;
    @Column(name = "vigencia_extintor")        private String vigenciaExtintor;
    private String observaciones;

    @ManyToOne
    @JoinColumn(name = "equipo_id")
    private MachineEntity machine;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
