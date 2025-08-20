package com.app.usochicamochabackend.order.infrastructure.entity;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.execution.domain.Result;
import com.app.usochicamochabackend.execution.infrastructure.entity.ResultEntity;
import com.app.usochicamochabackend.inspection.infrastructure.entity.InspectionEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ordenes")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String status;
    private LocalDateTime orderDate;
    private String description;

    @OneToOne
    @JoinColumn(name = "inspeccion_id")
    private InspectionEntity inspection;

    @OneToOne
    @JoinColumn(name = "resultado_id")
    private ResultEntity result;

    @ManyToOne
    @JoinColumn(name = "usuario_asignador_id")
    private UserEntity assignerUser;

    @ManyToOne
    @JoinColumn(name = "usuario_asignado_id")
    private UserEntity assignedUser;
}
