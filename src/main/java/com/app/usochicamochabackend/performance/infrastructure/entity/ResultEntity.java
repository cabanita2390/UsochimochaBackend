package com.app.usochicamochabackend.performance.infrastructure.entity;

import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;
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

    @Column(name = "time_spent")
    private String timeSpent;

    @OneToOne
    @JoinColumn(name = "labor_force_id")
    private LaborEntity laborForceId;

    @OneToMany
    @JoinColumn(name = "result_id")
    private List<SparePartEntity> resultId;
}