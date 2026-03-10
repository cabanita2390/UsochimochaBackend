package com.app.usochicamochabackend.moto.infrastructure.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity(name = "MotoInspDetalleDocumentosEntity")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "insp_detalle_documentos")
public class InspDetalleDocumentosEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_doc_check")
    private Long id;

    @Column(name = "id_inspeccion")
    private Long idInspeccion;

    @Column(name = "check_soat")
    private String checkSoat;

    @Column(name = "check_tecno")
    private String checkTecno;

    @Column(name = "check_licencia")
    private String checkLicencia;

    @Column(name = "check_extintor")
    private String checkExtintor;
}
