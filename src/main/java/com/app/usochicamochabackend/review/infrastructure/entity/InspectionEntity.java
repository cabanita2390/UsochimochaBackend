package com.app.usochicamochabackend.review.infrastructure.entity;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(name = "date_stamp")
    private LocalDateTime dateStamp;

    private String hourmeter;

    @Column(name = "leak_status")
    private String leakStatus;
    @Column(name = "brake_status")
    private String brakeStatus;
    @Column(name = "belts_pulleys_status")
    private String beltsPulleysStatus;
    @Column(name = "tire_lanes_status")
    private String tireLanesStatus;
    @Column(name = "car_ignition_status")
    private String carIgnitionStatus;
    @Column(name = "electrical_status")
    private String electricalStatus;
    @Column(name = "mechanical_status")
    private String mechanicalStatus;
    @Column(name = "temperature_status")
    private String temperatureStatus;
    @Column(name = "oil_status")
    private String oilStatus;
    @Column(name = "hydraulic_status")
    private String hydraulicStatus;
    @Column(name = "coolant_status")
    private String coolantStatus;
    @Column(name = "structuralStatus")
    private String structuralStatus;
    @Column(name = "expiration_date_fire_extinguisher")
    private String expirationDateFireExtinguisher;

    private String observations;

    @ManyToOne
    @JoinColumn(name = "machine_id")
    private MachineEntity machine;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @OneToOne(mappedBy = "inspection")
    private OrderEntity order;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "inspection_id")
    private List<ImageEntity> images;
}
