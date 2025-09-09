package com.app.usochicamochabackend.config;

import com.app.usochicamochabackend.actions.infrastructure.repository.ActionRepository;
import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import com.app.usochicamochabackend.machine.infrastructure.repository.MachineRepository;
import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;
import com.app.usochicamochabackend.order.infrastructure.repository.OrderRepository;
import com.app.usochicamochabackend.performance.infrastructure.entity.LaborEntity;
import com.app.usochicamochabackend.performance.infrastructure.entity.ResultEntity;
import com.app.usochicamochabackend.performance.infrastructure.entity.SparePartEntity;
import com.app.usochicamochabackend.performance.infrastructure.repository.ResultRepository;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
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
@Profile("dev")
public class DataSeeder {

    @Bean
    CommandLineRunner initData(
            UserRepositoryJpa userRepository,
            MachineRepository machineRepository,
            InspectionRepository inspectionRepository,
            OrderRepository orderRepository,
            ResultRepository resultRepository,
            ActionRepository actionRepository
    ) {
        return args -> {
            // Limpiar repos
            resultRepository.deleteAll();
            orderRepository.deleteAll();
            inspectionRepository.deleteAll();
            machineRepository.deleteAll();
            userRepository.deleteAll();

            // === Usuarios ===
            UserEntity admin = UserEntity.builder()
                    .fullName("Admin User")
                    .username("admin")
                    .password("$2a$10$cMY29RPYoIHMJSuwRfoD3eQxU1J5Rww4VnNOUOAEPqCBshkNfrEf6") // "password"
                    .email("admin@example.com")
                    .role("ADMIN")
                    .status(true)
                    .build();

            UserEntity mechanic = UserEntity.builder()
                    .fullName("Mechanic User")
                    .username("mechanic")
                    .password("$2a$10$cMY29RPYoIHMJSuwRfoD3eQxU1J5Rww4VnNOUOAEPqCBshkNfrEf6") // "password"
                    .email("mech@example.com")
                    .role("MECHANIC")
                    .status(true)
                    .build();

            userRepository.saveAll(List.of(admin, mechanic));

            // === Máquinas ===
            MachineEntity excavator = MachineEntity.builder()
                    .name("Excavator")
                    .model("CAT320")
                    .belongsTo("Distrito")
                    .status(true)
                    .soat(LocalDate.now().plusYears(1))
                    .brand("Caterpillar")
                    .runt(LocalDate.now().plusYears(1))
                    .numEngine("ENG12345")
                    .numInterIdentification("CHASIS123")
                    .build();

            MachineEntity bulldozer = MachineEntity.builder()
                    .name("Bulldozer")
                    .model("D6R")
                    .belongsTo("Constructora ACME")
                    .status(true)
                    .soat(LocalDate.now().plusYears(1))
                    .brand("Caterpillar")
                    .runt(LocalDate.now().plusYears(1))
                    .numEngine("ENG67890")
                    .numInterIdentification("CHASIS999")
                    .build();

            machineRepository.saveAll(List.of(excavator, bulldozer));

            // === Inspecciones ===
            InspectionEntity inspection1 = InspectionEntity.builder()
                    .UUID("UUID-EXC-001")
                    .dateStamp(LocalDateTime.now().minusDays(10))
                    .hourMeter(1500)
                    .leakStatus("Óptimo")
                    .brakeStatus("Óptimo")
                    .beltsPulleysStatus("Óptimo")
                    .carIgnitionStatus("Óptimo")
                    .coolantStatus("Óptimo")
                    .electricalStatus("Óptimo")
                    .expirationDateFireExtinguisher("2026-08")
                    .hydraulicStatus("Óptimo")
                    .mechanicalStatus("Óptimo")
                    .oilStatus("Óptimo")
                    .structuralStatus("Óptimo")
                    .temperatureStatus("Óptimo")
                    .tireLanesStatus("Óptimo")
                    .greasingAction("OK")
                    .greasingObservations("Todo en orden")
                    .unexpected(false)
                    .machine(excavator)
                    .user(admin)
                    .observations("Inspección rutinaria")
                    .build();

            InspectionEntity inspection2 = InspectionEntity.builder()
                    .UUID("UUID-BULL-001")
                    .dateStamp(LocalDateTime.now().minusDays(5))
                    .hourMeter(800)
                    .leakStatus("Pequeña fuga")
                    .brakeStatus("Óptimo")
                    .beltsPulleysStatus("Óptimo")
                    .carIgnitionStatus("Óptimo")
                    .coolantStatus("Óptimo")
                    .electricalStatus("Óptimo")
                    .expirationDateFireExtinguisher("2026-01")
                    .hydraulicStatus("Requiere revisión")
                    .mechanicalStatus("Óptimo")
                    .oilStatus("Óptimo")
                    .structuralStatus("Óptimo")
                    .temperatureStatus("Óptimo")
                    .tireLanesStatus("Óptimo")
                    .greasingAction("Revisión adicional")
                    .greasingObservations("Fugas menores detectadas")
                    .unexpected(true)
                    .machine(bulldozer)
                    .user(mechanic)
                    .observations("Necesita reparación hidráulica")
                    .build();

            inspectionRepository.saveAll(List.of(inspection1, inspection2));

            // === Órdenes y Resultados ===
            OrderEntity order1 = OrderEntity.builder()
                    .description("Cambio de aceite motor")
                    .inspection(inspection1)
                    .build();

            OrderEntity order2 = OrderEntity.builder()
                    .description("Revisión sistema hidráulico")
                    .inspection(inspection2)
                    .build();

            orderRepository.saveAll(List.of(order1, order2));

            // Resultados con mano de obra + repuestos
            LaborEntity labor1 = LaborEntity.builder()
                    .price(new BigDecimal(5000000))
                    .build();

            SparePartEntity part1 = SparePartEntity.builder()
                    .name("Filtro de aceite CAT")
                    .price(new BigDecimal(5000000))
                    .build();

            ResultEntity result1 = ResultEntity.builder()
                    .order(order1)
                    .laborForce(labor1)
                    .sparePart(List.of(part1))
                    .description("Aceite cambiado con éxito")
                    .build();

            LaborEntity labor2 = LaborEntity.builder()
                    .price(new BigDecimal(5000000))
                    .build();

            SparePartEntity part2 = SparePartEntity.builder()
                    .name("Manguera hidráulica")
                    .price(new BigDecimal(5000000))
                    .build();

            ResultEntity result2 = ResultEntity.builder()
                    .order(order2)
                    .laborForce(labor2)
                    .sparePart(List.of(part2))
                    .description("Sistema hidráulico reparado")
                    .build();

            resultRepository.saveAll(List.of(result1, result2));

            // Vincular resultados a las órdenes
            order1.setResult(result1);
            order2.setResult(result2);
            orderRepository.saveAll(List.of(order1, order2));
        };
    }
}
