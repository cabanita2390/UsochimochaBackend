package com.app.usochicamochabackend.inspection.domain;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;

import java.time.LocalDateTime;

public class Inspection {
    private Long id;
    private String uuid;
    private LocalDateTime dateStamp;
    private String hourMeter;

    private String estadoFugas;
    private String estadoFrenos;
    private String estadoCorreasPoleas;
    private String estadoLlantasCarriles;
    private String estadoEncendido;
    private String estadoElectrico;
    private String estadoMecanico;
    private String estadoTemperatura;
    private String estadoAceite;
    private String estadoHidraulico;
    private String estadoRefrigerante;
    private String estadoEstructural;
    private String vigenciaExtintor;
    private String observaciones;

    private MachineEntity machine;
    private UserEntity user;
}
