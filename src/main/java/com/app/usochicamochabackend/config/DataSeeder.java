package com.app.usochicamochabackend.config;

import com.app.usochicamochabackend.actions.infrastructure.repository.ActionRepository;
import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import com.app.usochicamochabackend.machine.infrastructure.repository.MachineRepository;
import com.app.usochicamochabackend.update.infrastructure.entity.BrandEntity;
import com.app.usochicamochabackend.update.infrastructure.entity.OilChangeEntity;
import com.app.usochicamochabackend.update.infrastructure.repository.OilChangeRepository;
import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;
import com.app.usochicamochabackend.order.infrastructure.repository.OrderRepository;
import com.app.usochicamochabackend.performance.infrastructure.entity.LaborEntity;
import com.app.usochicamochabackend.performance.infrastructure.entity.ResultEntity;
import com.app.usochicamochabackend.performance.infrastructure.entity.SparePartEntity;
import com.app.usochicamochabackend.performance.infrastructure.repository.ResultRepository;
import com.app.usochicamochabackend.review.infrastructure.entity.ImageEntity;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import com.app.usochicamochabackend.review.infrastructure.repository.ImageRepository;
import com.app.usochicamochabackend.review.infrastructure.repository.InspectionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@Profile("prod")
public class DataSeeder {

    @Bean
    CommandLineRunner initData(
            UserRepositoryJpa userRepository,
            MachineRepository machineRepository,
            InspectionRepository inspectionRepository,
            ImageRepository imageRepository,
            OrderRepository orderRepository,
            ResultRepository resultRepository,
            ActionRepository actionRepository,
            OilChangeRepository oilChangeRepository
    ) {
        return args -> {
            // Limpiar repos - Order matters for foreign key constraints
            // Delete entities that reference others first, then the referenced entities

            // First, break relationships by setting foreign keys to null where possible
            // But for simplicity, delete in the correct dependency order:

            // 1. Delete Orders first (they reference Results, Inspections, and Users)
            orderRepository.deleteAllInBatch();

            // 2. Now delete Results (nothing should reference them now)
            resultRepository.deleteAllInBatch();

            // 3. Delete Images (they reference Inspections)
            imageRepository.deleteAllInBatch();

            // 4. Delete Inspections (they reference Machines and Users)
            inspectionRepository.deleteAllInBatch();

            // 5. Delete OilChanges (they reference Machines and Brands)
            oilChangeRepository.deleteAllInBatch();

            // 6. Delete Machines and Users (base entities)
            machineRepository.deleteAllInBatch();
            userRepository.deleteAllInBatch();

            // === Usuarios ===
            UserEntity admin = UserEntity.builder()
                    .fullName("Admin User")
                    .username("admin")
                    .password("$2a$10$cMY29RPYoIHMJSuwRfoD3eQxU1J5Rww4VnNOUOAEPqCBshkNfrEf6") // "password"
                    .email("admin@example.com")
                    .role("ADMIN")
                    .status(true)
                    .build();

            userRepository.save(admin);
        };
    }
}
