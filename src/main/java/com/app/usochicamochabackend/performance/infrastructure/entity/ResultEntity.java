package com.app.usochicamochabackend.performance.infrastructure.entity;

import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "results")
public class ResultEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime date;

    private String timeSpent;

    @OneToOne(mappedBy = "result")
    private OrderEntity order;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "result_id")
    private List<LaborEntity> laborForce;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "result_id")
    private List<SparePartEntity> sparePart;
}