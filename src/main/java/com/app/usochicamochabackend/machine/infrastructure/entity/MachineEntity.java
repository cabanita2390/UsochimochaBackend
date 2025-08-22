package com.app.usochicamochabackend.machine.infrastructure.entity;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "machines")
public class MachineEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String model;
    private LocalDate soat;
    private String brand;
    private LocalDate runt;

    @Column(name = "num_engine")
    private String numEngine;

    @Column(name = "num_inter_identification")
    private String numInterIdentification;
}
