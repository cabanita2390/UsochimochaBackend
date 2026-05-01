package com.app.usochicamochabackend.vehicleinspection.application.dto;

import java.time.LocalDateTime;

public record VehicleInspectionReportDTO(
    Long idInspeccion,
    LocalDateTime fechaRegistro,
    String placa,
    String marca,
    String tipoVehiculo,
    String responsable,
    Integer kilometraje,
    Boolean aprobadoRuta,
    String observacionesFinales,
    
    // Detalle Mecánico
    String nivelAceite,
    String nivelRefrigerante,
    String nivelFrenos,
    String estadoLlantas,
    String lucesGeneral,
    String estadoVisual,
    String limpiezaGeneral,
    
    // Detalle Documentos (Checks visuales)
    String checkSoat,
    String checkTecno,
    String checkLicencia,
    String checkExtintor,
    
    // Detalle Elementos
    Boolean tieneBotiquin,
    Boolean tieneSeñalizacion,
    Boolean tieneLineasEmergencia,
    String tieneLlantaRepuesto,
    String tieneGatoHidraulico,
    
    // Detalle Salud
    String saludFisica,
    String saludMental,
    String sobrio,
    String medicamentos,
    String condicionParaConducir,
    Boolean conscienteResponsabilidad
) {}
