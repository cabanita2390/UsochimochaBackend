package com.app.usochicamochabackend.review.infrastructure.entity;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "inspections")
public class InspectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String UUID;
    @JsonProperty("isUnexpected")
    @Column(name = "is_unexpected")
    private Boolean unexpected;
    @Column(nullable = false)
    private LocalDateTime dateStamp;
    private Double hourMeter;
    private String leakStatus;
    private String brakeStatus;
    private String beltsPulleysStatus;
    private String tireLanesStatus;
    private String carIgnitionStatus;
    private String electricalStatus;
    private String mechanicalStatus;
    private String temperatureStatus;
    private String oilStatus;
    private String hydraulicStatus;
    private String coolantStatus;
    private String structuralStatus;
    private String expirationDateFireExtinguisher;
    private String observations;
    private String greasingAction;
    private String greasingObservations;

    @ManyToOne
    @JoinColumn(name = "machine_id")
    private MachineEntity machine;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @OneToMany(mappedBy = "inspection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderEntity> orders;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "inspection_id")
    private List<ImageEntity> images;
}
