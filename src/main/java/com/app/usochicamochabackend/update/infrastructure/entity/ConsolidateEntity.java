package com.app.usochicamochabackend.update.infrastructure.entity;

import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "consolidates")
public class ConsolidateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    @Column(name = "date_time")
    private String dateTime;

    @Column(name = "next_time")
    private String dateTimex;

    private String hourmeter;

    @ManyToOne
    @JoinColumn(name = "machine_id")
    private MachineEntity machine;
}
