package com.app.usochicamochabackend.vehicle.infrastructure.entity;

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
@Table(name = "cat_marcas_modelos")
public class MarcaModeloEntity {

    @Id
    @Column(name = "id_marca")
    private Integer idMarca;

    @Column(name = "descripcion")
    private String descripcion;
}
