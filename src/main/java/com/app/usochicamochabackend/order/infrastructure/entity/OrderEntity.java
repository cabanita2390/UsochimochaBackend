package com.app.usochicamochabackend.order.infrastructure.entity;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.performance.infrastructure.entity.ResultEntity;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String status;
    private LocalDateTime date;
    private String description;

    @OneToOne
    @JoinColumn(name = "inspection_id")
    private InspectionEntity inspection;

    @OneToOne
    @JoinColumn(name = "result_id")
    private ResultEntity result;

    @ManyToOne
    @JoinColumn(name = "assigner_user_id")
    private UserEntity assignerUser;

    @ManyToOne
    @JoinColumn(name = "assigned_user_id")
    private UserEntity assignedUser;
}
