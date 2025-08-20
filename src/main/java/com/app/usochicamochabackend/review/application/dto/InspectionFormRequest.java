package com.app.usochicamochabackend.review.application.dto;

import java.util.List;

public record InspectionFormRequest(
        String horometro,
        String estadoFugas,
        String estadoFrenos,
        String estadoCorreasPoleas,
        String estadoLlantasCarriles,
        String estadoEncendido,
        String estadoElectrico,
        String estadoMecanico,
        String estadoTemperatura,
        String estadoAceite,
        String estadoHidraulico,
        String estadoRefrigerante,
        String estadoEstructural,
        String vigenciaExtintor,
        String observaciones,
        Long userId,
        Long machineId
) {}
