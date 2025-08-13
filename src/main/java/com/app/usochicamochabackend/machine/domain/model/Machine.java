package com.app.usochicamochabackend.machine.domain.model;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

import java.time.LocalDate;

public class Machine {
    private Long id;
    private String name;
    private String model;
    private String numEngine;
    private String numInterIdentification;
    private LocalDate soat;
    private String brand;
    private LocalDate runt;
    private UserEntity user;
}
