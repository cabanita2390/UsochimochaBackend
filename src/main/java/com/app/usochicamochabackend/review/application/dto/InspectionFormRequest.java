package com.app.usochicamochabackend.review.application.dto;

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
        Long userId, //usa el 10
        Long machineId //usa el 5
) {}
