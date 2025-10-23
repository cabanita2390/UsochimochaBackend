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
@Profile("dev")
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
            MachineEntity excavator1 = MachineEntity.builder()
                    .name("Excavator Principal")
                    .model("CAT320D")
                    .belongsTo("Constructora ACME")
                    .status(true)
                    .soat(LocalDate.now().plusYears(1))
                    .brand("Caterpillar")
                    .runt(LocalDate.now().plusYears(1))
                    .numEngine("ENG12345")
                    .numInterIdentification("CHASIS001")
                    .build();

            MachineEntity bulldozer1 = MachineEntity.builder()
                    .name("Bulldozer D6")
                    .model("D6R-XL")
                    .belongsTo("Constructora ACME")
                    .status(true)
                    .soat(LocalDate.now().plusYears(1))
                    .brand("Caterpillar")
                    .runt(LocalDate.now().plusYears(1))
                    .numEngine("ENG67890")
                    .numInterIdentification("CHASIS002")
                    .build();

            MachineEntity crane1 = MachineEntity.builder()
                    .name("Grúa Torre")
                    .model("TC5020")
                    .belongsTo("Infraestructura MAX")
                    .status(true)
                    .soat(LocalDate.now().plusYears(1))
                    .brand("Liebherr")
                    .runt(LocalDate.now().plusYears(1))
                    .numEngine("ENG11111")
                    .numInterIdentification("CHASIS003")
                    .build();

            MachineEntity loader1 = MachineEntity.builder()
                    .name("Cargador Frontal")
                    .model("WL950")
                    .belongsTo("Minería del Sur")
                    .status(true)
                    .soat(LocalDate.now().plusYears(1))
                    .brand("Komatsu")
                    .runt(LocalDate.now().plusYears(1))
                    .numEngine("ENG22222")
                    .numInterIdentification("CHASIS004")
                    .build();

            MachineEntity excavator2 = MachineEntity.builder()
                    .name("Excavadora Midi")
                    .model("ZX350LC")
                    .belongsTo("Constructora Beta")
                    .status(true)
                    .soat(LocalDate.now().plusYears(1))
                    .brand("Hitachi")
                    .runt(LocalDate.now().plusYears(1))
                    .numEngine("ENG33333")
                    .numInterIdentification("CHASIS005")
                    .build();

            MachineEntity compactor1 = MachineEntity.builder()
                    .name("Compactador de Suelo")
                    .model("CS56B")
                    .belongsTo("Vías Nacionales")
                    .status(true)
                    .soat(LocalDate.now().plusYears(1))
                    .brand("Caterpillar")
                    .runt(LocalDate.now().plusYears(1))
                    .numEngine("ENG44444")
                    .numInterIdentification("CHASIS006")
                    .build();

            MachineEntity drill1 = MachineEntity.builder()
                    .name("Perforadora Hidráulica")
                    .model("MD5150C")
                    .belongsTo("Minería del Sur")
                    .status(true)
                    .soat(LocalDate.now().plusYears(1))
                    .brand("Caterpillar")
                    .runt(LocalDate.now().plusYears(1))
                    .numEngine("ENG55555")
                    .numInterIdentification("CHASIS007")
                    .build();

            MachineEntity forklift1 = MachineEntity.builder()
                    .name("Montacargas")
                    .model("FD70N")
                    .belongsTo("Logística Express")
                    .status(true)
                    .soat(LocalDate.now().plusYears(1))
                    .brand("Komatsu")
                    .runt(LocalDate.now().plusYears(1))
                    .numEngine("ENG66666")
                    .numInterIdentification("CHASIS008")
                    .build();

            MachineEntity backhoe1 = MachineEntity.builder()
                    .name("Retroexcavadora")
                    .model("420F2")
                    .belongsTo("Constructora Gamma")
                    .status(true)
                    .soat(LocalDate.now().plusYears(1))
                    .brand("Caterpillar")
                    .runt(LocalDate.now().plusYears(1))
                    .numEngine("ENG77777")
                    .numInterIdentification("CHASIS009")
                    .build();

            MachineEntity grader1 = MachineEntity.builder()
                    .name("Motoconformadora")
                    .model("140M3")
                    .belongsTo("Vías Nacionales")
                    .status(true)
                    .soat(LocalDate.now().plusYears(1))
                    .brand("Caterpillar")
                    .runt(LocalDate.now().plusYears(1))
                    .numEngine("ENG88888")
                    .numInterIdentification("CHASIS010")
                    .build();

            machineRepository.saveAll(List.of(excavator1, bulldozer1, crane1, loader1, excavator2, compactor1, drill1, forklift1, backhoe1, grader1));

            // Fetch fresh machine entities from database to avoid detached entity issues
            MachineEntity freshExcavator1 = machineRepository.findById(excavator1.getId()).orElseThrow();
            MachineEntity freshBulldozer1 = machineRepository.findById(bulldozer1.getId()).orElseThrow();
            MachineEntity freshCrane1 = machineRepository.findById(crane1.getId()).orElseThrow();
            MachineEntity freshLoader1 = machineRepository.findById(loader1.getId()).orElseThrow();
            MachineEntity freshExcavator2 = machineRepository.findById(excavator2.getId()).orElseThrow();
            MachineEntity freshCompactor1 = machineRepository.findById(compactor1.getId()).orElseThrow();
            MachineEntity freshDrill1 = machineRepository.findById(drill1.getId()).orElseThrow();
            MachineEntity freshForklift1 = machineRepository.findById(forklift1.getId()).orElseThrow();
            MachineEntity freshBackhoe1 = machineRepository.findById(backhoe1.getId()).orElseThrow();
            MachineEntity freshGrader1 = machineRepository.findById(grader1.getId()).orElseThrow();

            // === Marcas de aceite ===
            BrandEntity motorOilBrand1 = BrandEntity.builder()
                    .name("Shell Helix Ultra")
                    .type("MOTOR_OIL")
                    .status(true)
                    .build();

            BrandEntity motorOilBrand2 = BrandEntity.builder()
                    .name("Mobil 1")
                    .type("MOTOR_OIL")
                    .status(true)
                    .build();

            BrandEntity hydraulicOilBrand1 = BrandEntity.builder()
                    .name("Shell Tellus")
                    .type("HYDRAULIC_OIL")
                    .status(true)
                    .build();

            BrandEntity hydraulicOilBrand2 = BrandEntity.builder()
                    .name("Mobil DTE")
                    .type("HYDRAULIC_OIL")
                    .status(true)
                    .build();

            // === Inspecciones ===
            InspectionEntity inspection1 = InspectionEntity.builder()
                    .UUID("UUID-EXC-001")
                    .dateStamp(LocalDateTime.now().minusDays(10))
                    .hourMeter(1500.0)
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
                    .machine(freshExcavator1)
                    .user(admin)
                    .observations("Inspección rutinaria")
                    .build();

            InspectionEntity inspection2 = InspectionEntity.builder()
                    .UUID("UUID-BULL-001")
                    .dateStamp(LocalDateTime.now().minusDays(5))
                    .hourMeter(800.0)
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
                    .machine(freshBulldozer1)
                    .user(mechanic)
                    .observations("Necesita reparación hidráulica")
                    .build();

            InspectionEntity inspection3 = InspectionEntity.builder()
                    .UUID("UUID-EXC-002")
                    .dateStamp(LocalDateTime.now().minusDays(20))
                    .hourMeter(1600.0) .leakStatus("Óptimo")
                    .brakeStatus("Óptimo")
                    .beltsPulleysStatus("Desgastado")
                    .carIgnitionStatus("Óptimo")
                    .coolantStatus("Óptimo")
                    .electricalStatus("Óptimo")
                    .expirationDateFireExtinguisher("2026-09")
                    .hydraulicStatus("Óptimo")
                    .mechanicalStatus("Óptimo")
                    .oilStatus("Óptimo")
                    .structuralStatus("Óptimo")
                    .temperatureStatus("Óptimo")
                    .tireLanesStatus("Óptimo")
                    .greasingAction("Lubricar")
                    .greasingObservations("Correa debe reemplazarse pronto")
                    .unexpected(false)
                    .machine(freshExcavator1)
                    .user(mechanic)
                    .observations("Correas desgastadas")
                    .build();

            InspectionEntity inspection4 = InspectionEntity.builder()
                    .UUID("UUID-BULL-002")
                    .dateStamp(LocalDateTime.now().minusDays(15))
                    .hourMeter(900.0)
                    .leakStatus("Óptimo")
                    .brakeStatus("Revisar")
                    .beltsPulleysStatus("Óptimo")
                    .carIgnitionStatus("Óptimo")
                    .coolantStatus("Óptimo")
                    .electricalStatus("Óptimo")
                    .expirationDateFireExtinguisher("2026-03")
                    .hydraulicStatus("Óptimo")
                    .mechanicalStatus("Óptimo")
                    .oilStatus("Bajo nivel")
                    .structuralStatus("Óptimo")
                    .temperatureStatus("Óptimo")
                    .tireLanesStatus("Óptimo")
                    .greasingAction("Agregar aceite")
                    .greasingObservations("Nivel de aceite bajo")
                    .unexpected(true)
                    .machine(freshBulldozer1)
                    .user(admin)
                    .observations("Requiere mantenimiento por aceite bajo")
                    .build();

            InspectionEntity inspection5 = InspectionEntity.builder()
                    .UUID("UUID-EXC-003")
                    .dateStamp(LocalDateTime.now().minusDays(25))
                    .hourMeter(1700.0)
                    .leakStatus("Fuga leve")
                    .brakeStatus("Óptimo")
                    .beltsPulleysStatus("Óptimo")
                    .carIgnitionStatus("Óptimo")
                    .coolantStatus("Requiere recarga")
                    .electricalStatus("Óptimo")
                    .expirationDateFireExtinguisher("2026-05")
                    .hydraulicStatus("Óptimo")
                    .mechanicalStatus("Óptimo")
                    .oilStatus("Óptimo")
                    .structuralStatus("Óptimo")
                    .temperatureStatus("Alta")
                    .tireLanesStatus("Óptimo")
                    .greasingAction("Revisar refrigerante")
                    .greasingObservations("Temperatura elevada en motor")
                    .unexpected(true)
                    .machine(freshExcavator1)
                    .user(admin)
                    .observations("Revisar sistema de refrigeración")
                    .build();

            InspectionEntity inspection6 = InspectionEntity.builder()
                    .UUID("UUID-BULL-003")
                    .dateStamp(LocalDateTime.now().minusDays(30))
                    .hourMeter(950.0)
                    .leakStatus("Óptimo")
                    .brakeStatus("Óptimo")
                    .beltsPulleysStatus("Óptimo")
                    .carIgnitionStatus("Lento")
                    .coolantStatus("Óptimo")
                    .electricalStatus("Óptimo")
                    .expirationDateFireExtinguisher("2027-02")
                    .hydraulicStatus("Óptimo")
                    .mechanicalStatus("Óptimo")
                    .oilStatus("Óptimo")
                    .structuralStatus("Revisión estructural pendiente")
                    .temperatureStatus("Óptimo")
                    .tireLanesStatus("Óptimo")
                    .greasingAction("Revisión")
                    .greasingObservations("Encendido lento")
                    .unexpected(true)
                    .machine(freshBulldozer1)
                    .user(mechanic)
                    .observations("Posible falla en el arranque")
                    .build();

            InspectionEntity inspection7 = InspectionEntity.builder()
                    .UUID("UUID-EXC-004")
                    .dateStamp(LocalDateTime.now().minusDays(35))
                    .hourMeter(1800.0)
                    .leakStatus("Óptimo")
                    .brakeStatus("Desgaste medio")
                    .beltsPulleysStatus("Óptimo")
                    .carIgnitionStatus("Óptimo")
                    .coolantStatus("Óptimo")
                    .electricalStatus("Óptimo")
                    .expirationDateFireExtinguisher("2025-12")
                    .hydraulicStatus("Óptimo")
                    .mechanicalStatus("Óptimo")
                    .oilStatus("Óptimo")
                    .structuralStatus("Óptimo")
                    .temperatureStatus("Óptimo")
                    .tireLanesStatus("Óptimo")
                    .greasingAction("Revisar frenos")
                    .greasingObservations("Discos de freno algo gastados")
                    .unexpected(false)
                    .machine(freshExcavator1)
                    .user(admin)
                    .observations("Necesitará cambio de frenos pronto")
                    .build();

            InspectionEntity inspection8 = InspectionEntity.builder()
                    .UUID("UUID-BULL-004")
                    .dateStamp(LocalDateTime.now().minusDays(40))
                    .hourMeter(1000.0)
                    .leakStatus("Óptimo")
                    .brakeStatus("Óptimo")
                    .beltsPulleysStatus("Óptimo")
                    .carIgnitionStatus("Óptimo")
                    .coolantStatus("Óptimo")
                    .electricalStatus("Revisar batería")
                    .expirationDateFireExtinguisher("2027-01")
                    .hydraulicStatus("Óptimo")
                    .mechanicalStatus("Óptimo")
                    .oilStatus("Óptimo")
                    .structuralStatus("Óptimo")
                    .temperatureStatus("Óptimo")
                    .tireLanesStatus("Óptimo")
                    .greasingAction("Revisar conexiones eléctricas")
                    .greasingObservations("Voltaje inestable")
                    .unexpected(true)
                    .machine(freshBulldozer1)
                    .user(admin)
                    .observations("Batería cercana a su vida útil")
                    .build();

            InspectionEntity inspection9 = InspectionEntity.builder()
                    .UUID("UUID-EXC-005")
                    .dateStamp(LocalDateTime.now().minusDays(45))
                    .hourMeter(1900.0)
                    .leakStatus("Óptimo")
                    .brakeStatus("Óptimo")
                    .beltsPulleysStatus("Óptimo")
                    .carIgnitionStatus("Óptimo")
                    .coolantStatus("Óptimo")
                    .electricalStatus("Óptimo")
                    .expirationDateFireExtinguisher("2026-11")
                    .hydraulicStatus("Óptimo")
                    .mechanicalStatus("Óptimo")
                    .oilStatus("Óptimo")
                    .structuralStatus("Grieta menor detectada")
                    .temperatureStatus("Óptimo")
                    .tireLanesStatus("Óptimo")
                    .greasingAction("Verificar estructura")
                    .greasingObservations("Se requiere reparación estructural ligera")
                    .unexpected(true)
                    .machine(freshExcavator1)
                    .user(mechanic)
                    .observations("Soldadura necesaria en brazo mecánico")
                    .build();

            InspectionEntity inspection10 = InspectionEntity.builder()
                    .UUID("UUID-BULL-005")
                    .dateStamp(LocalDateTime.now().minusDays(50))
                    .hourMeter(1100.0)
                    .leakStatus("Óptimo")
                    .brakeStatus("Óptimo")
                    .beltsPulleysStatus("Óptimo")
                    .carIgnitionStatus("Óptimo")
                    .coolantStatus("Óptimo")
                    .electricalStatus("Óptimo")
                    .expirationDateFireExtinguisher("2027-03")
                    .hydraulicStatus("Óptimo")
                    .mechanicalStatus("Óptimo")
                    .oilStatus("Bajo nivel")
                    .structuralStatus("Óptimo")
                    .temperatureStatus("Óptimo")
                    .tireLanesStatus("Óptimo")
                    .greasingAction("Agregar aceite")
                    .greasingObservations("Aceite casi agotado")
                    .unexpected(true)
                    .machine(freshBulldozer1)
                    .user(admin)
                    .observations("Requiere cambio de aceite inmediato")
                    .build();

            InspectionEntity inspection11 = InspectionEntity.builder()
                    .UUID("UUID-EXC-006")
                    .dateStamp(LocalDateTime.now().minusDays(55))
                    .hourMeter(2000.0)
                    .leakStatus("Óptimo")
                    .brakeStatus("Óptimo")
                    .beltsPulleysStatus("Requiere cambio pronto")
                    .carIgnitionStatus("Óptimo")
                    .coolantStatus("Óptimo")
                    .electricalStatus("Óptimo")
                    .expirationDateFireExtinguisher("2026-10")
                    .hydraulicStatus("Óptimo")
                    .mechanicalStatus("Óptimo")
                    .oilStatus("Óptimo")
                    .structuralStatus("Óptimo")
                    .temperatureStatus("Óptimo")
                    .tireLanesStatus("Desgaste medio")
                    .greasingAction("Revisar neumáticos")
                    .greasingObservations("Llantas desgastadas")
                    .unexpected(true)
                    .machine(freshExcavator1)
                    .user(admin)
                    .observations("Se recomienda reemplazar llantas")
                    .build();

            InspectionEntity inspection12 = InspectionEntity.builder()
                    .UUID("UUID-BULL-006")
                    .dateStamp(LocalDateTime.now().minusDays(60))
                    .hourMeter(1200.0)
                    .leakStatus("Óptimo")
                    .brakeStatus("Óptimo")
                    .beltsPulleysStatus("Óptimo")
                    .carIgnitionStatus("Óptimo")
                    .coolantStatus("Bajo nivel")
                    .electricalStatus("Óptimo")
                    .expirationDateFireExtinguisher("2025-09")
                    .hydraulicStatus("Óptimo")
                    .mechanicalStatus("Óptimo")
                    .oilStatus("Óptimo")
                    .structuralStatus("Óptimo")
                    .temperatureStatus("Alta")
                    .tireLanesStatus("Óptimo")
                    .greasingAction("Agregar refrigerante")
                    .greasingObservations("Nivel bajo de refrigerante")
                    .unexpected(true)
                    .machine(freshBulldozer1)
                    .user(mechanic)
                    .observations("Sistema de refrigeración en riesgo")
                    .build();

            InspectionEntity inspection13 = InspectionEntity.builder()
                    .UUID("UUID-EXC-007")
                    .dateStamp(LocalDateTime.now().minusDays(65))
                    .hourMeter(2100.0)
                    .leakStatus("Óptimo")
                    .brakeStatus("Óptimo")
                    .beltsPulleysStatus("Óptimo")
                    .carIgnitionStatus("Óptimo")
                    .coolantStatus("Óptimo")
                    .electricalStatus("Óptimo")
                    .expirationDateFireExtinguisher("2026-07")
                    .hydraulicStatus("Óptimo")
                    .mechanicalStatus("Vibraciones leves")
                    .oilStatus("Óptimo")
                    .structuralStatus("Óptimo")
                    .temperatureStatus("Óptimo")
                    .tireLanesStatus("Óptimo")
                    .greasingAction("Revisar motor")
                    .greasingObservations("Vibraciones en motor")
                    .unexpected(true)
                    .machine(freshExcavator1)
                    .user(admin)
                    .observations("Podría requerir alineación del motor")
                    .build();

            InspectionEntity inspection14 = InspectionEntity.builder()
                    .UUID("UUID-BULL-007")
                    .dateStamp(LocalDateTime.now().minusDays(70))
                    .hourMeter(1300.0)
                    .leakStatus("Fuga mínima")
                    .brakeStatus("Óptimo")
                    .beltsPulleysStatus("Óptimo")
                    .carIgnitionStatus("Óptimo")
                    .coolantStatus("Óptimo")
                    .electricalStatus("Óptimo")
                    .expirationDateFireExtinguisher("2026-06")
                    .hydraulicStatus("Óptimo")
                    .mechanicalStatus("Óptimo")
                    .oilStatus("Óptimo")
                    .structuralStatus("Óptimo")
                    .temperatureStatus("Óptimo")
                    .tireLanesStatus("Óptimo")
                    .greasingAction("Revisar fuga")
                    .greasingObservations("Se detectó fuga pequeña")
                    .unexpected(true)
                    .machine(freshBulldozer1)
                    .user(admin)
                    .observations("Controlar fuga en sistema hidráulico")
                    .build();

            InspectionEntity inspection15 = InspectionEntity.builder()
                    .UUID("UUID-EXC-008")
                    .dateStamp(LocalDateTime.now().minusDays(75))
                    .hourMeter(2200.0)
                    .leakStatus("Óptimo")
                    .brakeStatus("Revisar")
                    .beltsPulleysStatus("Óptimo")
                    .carIgnitionStatus("Óptimo")
                    .coolantStatus("Óptimo")
                    .electricalStatus("Óptimo")
                    .expirationDateFireExtinguisher("2025-11")
                    .hydraulicStatus("Óptimo")
                    .mechanicalStatus("Óptimo")
                    .oilStatus("Óptimo")
                    .structuralStatus("Óptimo")
                    .temperatureStatus("Óptimo")
                    .tireLanesStatus("Óptimo")
                    .greasingAction("Revisar frenos")
                    .greasingObservations("Pastillas desgastadas")
                    .unexpected(true)
                    .machine(freshExcavator2)
                    .user(mechanic)
                    .observations("Cambio de frenos recomendado")
                    .build();

            InspectionEntity inspection16 = InspectionEntity.builder()
                    .UUID("UUID-BULL-008")
                    .dateStamp(LocalDateTime.now().minusDays(80))
                    .hourMeter(1400.0)
                    .leakStatus("Óptimo")
                    .brakeStatus("Óptimo")
                    .beltsPulleysStatus("Óptimo")
                    .carIgnitionStatus("Óptimo")
                    .coolantStatus("Óptimo")
                    .electricalStatus("Óptimo")
                    .expirationDateFireExtinguisher("2026-04")
                    .hydraulicStatus("Óptimo")
                    .mechanicalStatus("Óptimo")
                    .oilStatus("Óptimo")
                    .structuralStatus("Óptimo")
                    .temperatureStatus("Alta")
                    .tireLanesStatus("Óptimo")
                    .greasingAction("Verificar sistema de refrigeración")
                    .greasingObservations("Temperatura sobre lo normal")
                    .unexpected(true)
                    .machine(freshBulldozer1)
                    .user(admin)
                    .observations("Posible obstrucción en radiador")
                    .build();

            InspectionEntity inspection17 = InspectionEntity.builder()
                    .UUID("UUID-EXC-009")
                    .dateStamp(LocalDateTime.now().minusDays(85))
                    .hourMeter(2300.0)
                    .leakStatus("Óptimo")
                    .brakeStatus("Óptimo")
                    .beltsPulleysStatus("Óptimo")
                    .carIgnitionStatus("Óptimo")
                    .coolantStatus("Óptimo")
                    .electricalStatus("Óptimo")
                    .expirationDateFireExtinguisher("2026-12")
                    .hydraulicStatus("Óptimo")
                    .mechanicalStatus("Óptimo")
                    .oilStatus("Óptimo")
                    .structuralStatus("Óptimo")
                    .temperatureStatus("Óptimo")
                    .tireLanesStatus("Revisar")
                    .greasingAction("Cambio de llantas")
                    .greasingObservations("Llantas desgastadas en exceso")
                    .unexpected(true)
                    .machine(freshExcavator2)
                    .user(mechanic)
                    .observations("Necesario reemplazar llantas inmediatamente")
                    .build();


            inspectionRepository.saveAll(List.of(inspection1, inspection2, inspection3, inspection4, inspection5, inspection6, inspection7, inspection8, inspection9, inspection10, inspection11, inspection12, inspection13, inspection14, inspection15, inspection16, inspection17));

            // === Cambios de aceite ===
            // Motor oil changes
            OilChangeEntity motorOilChange1 = OilChangeEntity.builder()
                    .dateStamp(LocalDateTime.now().minusDays(30))
                    .motorOil(true)
                    .hydraulicOil(false)
                    .brand(motorOilBrand1)
                    .quantity(15)
                    .hourMeter(1500.0)
                    .averageHoursChange(250)
                    .machine(freshExcavator1)
                    .build();

            OilChangeEntity motorOilChange2 = OilChangeEntity.builder()
                    .dateStamp(LocalDateTime.now().minusDays(25))
                    .motorOil(true)
                    .hydraulicOil(false)
                    .brand(motorOilBrand2)
                    .quantity(12)
                    .hourMeter(800.0)
                    .averageHoursChange(200)
                    .machine(freshBulldozer1)
                    .build();

            OilChangeEntity motorOilChange3 = OilChangeEntity.builder()
                    .dateStamp(LocalDateTime.now().minusDays(20))
                    .motorOil(true)
                    .hydraulicOil(false)
                    .brand(motorOilBrand1)
                    .quantity(8)
                    .hourMeter(600.0)
                    .averageHoursChange(300)
                    .machine(freshCrane1)
                    .build();

            OilChangeEntity motorOilChange4 = OilChangeEntity.builder()
                    .dateStamp(LocalDateTime.now().minusDays(35))
                    .motorOil(true)
                    .hydraulicOil(false)
                    .brand(motorOilBrand2)
                    .quantity(10)
                    .hourMeter(1200.0)
                    .averageHoursChange(200)
                    .machine(freshLoader1)
                    .build();

            OilChangeEntity motorOilChange5 = OilChangeEntity.builder()
                    .dateStamp(LocalDateTime.now().minusDays(28))
                    .motorOil(true)
                    .hydraulicOil(false)
                    .brand(motorOilBrand1)
                    .quantity(14)
                    .hourMeter(1600.0)
                    .averageHoursChange(250)
                    .machine(freshExcavator2)
                    .build();

            OilChangeEntity motorOilChange6 = OilChangeEntity.builder()
                    .dateStamp(LocalDateTime.now().minusDays(40))
                    .motorOil(true)
                    .hydraulicOil(false)
                    .brand(motorOilBrand2)
                    .quantity(6)
                    .hourMeter(900.0)
                    .averageHoursChange(150)
                    .machine(freshCompactor1)
                    .build();

            OilChangeEntity motorOilChange7 = OilChangeEntity.builder()
                    .dateStamp(LocalDateTime.now().minusDays(22))
                    .motorOil(true)
                    .hydraulicOil(false)
                    .brand(motorOilBrand1)
                    .quantity(18)
                    .hourMeter(1100.0)
                    .averageHoursChange(300)
                    .machine(freshDrill1)
                    .build();

            OilChangeEntity motorOilChange8 = OilChangeEntity.builder()
                    .dateStamp(LocalDateTime.now().minusDays(32))
                    .motorOil(true)
                    .hydraulicOil(false)
                    .brand(motorOilBrand2)
                    .quantity(5)
                    .hourMeter(700.0)
                    .averageHoursChange(150)
                    .machine(freshForklift1)
                    .build();

            OilChangeEntity motorOilChange9 = OilChangeEntity.builder()
                    .dateStamp(LocalDateTime.now().minusDays(26))
                    .motorOil(true)
                    .hydraulicOil(false)
                    .brand(motorOilBrand1)
                    .quantity(11)
                    .hourMeter(1300.0)
                    .averageHoursChange(200)
                    .machine(freshBackhoe1)
                    .build();

            OilChangeEntity motorOilChange10 = OilChangeEntity.builder()
                    .dateStamp(LocalDateTime.now().minusDays(38))
                    .motorOil(true)
                    .hydraulicOil(false)
                    .brand(motorOilBrand2)
                    .quantity(9)
                    .hourMeter(1000.0)
                    .averageHoursChange(180)
                    .machine(freshGrader1)
                    .build();

            // Hydraulic oil changes
            OilChangeEntity hydraulicOilChange1 = OilChangeEntity.builder()
                    .dateStamp(LocalDateTime.now().minusDays(45))
                    .motorOil(false)
                    .hydraulicOil(true)
                    .brand(hydraulicOilBrand1)
                    .quantity(200)
                    .hourMeter(1500.0)
                    .averageHoursChange(500)
                    .machine(freshExcavator1)
                    .build();

            OilChangeEntity hydraulicOilChange2 = OilChangeEntity.builder()
                    .dateStamp(LocalDateTime.now().minusDays(50))
                    .motorOil(false)
                    .hydraulicOil(true)
                    .brand(hydraulicOilBrand2)
                    .quantity(150)
                    .hourMeter(800.0)
                    .averageHoursChange(400)
                    .machine(freshBulldozer1)
                    .build();

            OilChangeEntity hydraulicOilChange3 = OilChangeEntity.builder()
                    .dateStamp(LocalDateTime.now().minusDays(55))
                    .motorOil(false)
                    .hydraulicOil(true)
                    .brand(hydraulicOilBrand1)
                    .quantity(300)
                    .hourMeter(600.0)
                    .averageHoursChange(600)
                    .machine(freshCrane1)
                    .build();

            OilChangeEntity hydraulicOilChange4 = OilChangeEntity.builder()
                    .dateStamp(LocalDateTime.now().minusDays(42))
                    .motorOil(false)
                    .hydraulicOil(true)
                    .brand(hydraulicOilBrand2)
                    .quantity(120)
                    .hourMeter(1200.0)
                    .averageHoursChange(350)
                    .machine(freshLoader1)
                    .build();

            OilChangeEntity hydraulicOilChange5 = OilChangeEntity.builder()
                    .dateStamp(LocalDateTime.now().minusDays(48))
                    .motorOil(false)
                    .hydraulicOil(true)
                    .brand(hydraulicOilBrand1)
                    .quantity(180)
                    .hourMeter(1600.0)
                    .averageHoursChange(450)
                    .machine(freshExcavator2)
                    .build();

            OilChangeEntity hydraulicOilChange6 = OilChangeEntity.builder()
                    .dateStamp(LocalDateTime.now().minusDays(60))
                    .motorOil(false)
                    .hydraulicOil(true)
                    .brand(hydraulicOilBrand2)
                    .quantity(80)
                    .hourMeter(900.0)
                    .averageHoursChange(300)
                    .machine(freshCompactor1)
                    .build();

            OilChangeEntity hydraulicOilChange7 = OilChangeEntity.builder()
                    .dateStamp(LocalDateTime.now().minusDays(52))
                    .motorOil(false)
                    .hydraulicOil(true)
                    .brand(hydraulicOilBrand1)
                    .quantity(250)
                    .hourMeter(1100.0)
                    .averageHoursChange(550)
                    .machine(freshDrill1)
                    .build();

            OilChangeEntity hydraulicOilChange8 = OilChangeEntity.builder()
                    .dateStamp(LocalDateTime.now().minusDays(58))
                    .motorOil(false)
                    .hydraulicOil(true)
                    .brand(hydraulicOilBrand2)
                    .quantity(60)
                    .hourMeter(700.0)
                    .averageHoursChange(250)
                    .machine(freshForklift1)
                    .build();

            OilChangeEntity hydraulicOilChange9 = OilChangeEntity.builder()
                    .dateStamp(LocalDateTime.now().minusDays(46))
                    .motorOil(false)
                    .hydraulicOil(true)
                    .brand(hydraulicOilBrand1)
                    .quantity(140)
                    .hourMeter(1300.0)
                    .averageHoursChange(400)
                    .machine(freshBackhoe1)
                    .build();

            OilChangeEntity hydraulicOilChange10 = OilChangeEntity.builder()
                    .dateStamp(LocalDateTime.now().minusDays(54))
                    .motorOil(false)
                    .hydraulicOil(true)
                    .brand(hydraulicOilBrand2)
                    .quantity(100)
                    .hourMeter(1000.0)
                    .averageHoursChange(350)
                    .machine(freshGrader1)
                    .build();

            oilChangeRepository.saveAll(List.of(
                motorOilChange1, motorOilChange2, motorOilChange3, motorOilChange4, motorOilChange5,
                motorOilChange6, motorOilChange7, motorOilChange8, motorOilChange9, motorOilChange10,
                hydraulicOilChange1, hydraulicOilChange2, hydraulicOilChange3, hydraulicOilChange4, hydraulicOilChange5,
                hydraulicOilChange6, hydraulicOilChange7, hydraulicOilChange8, hydraulicOilChange9, hydraulicOilChange10
            ));

            // === Images ===
            ImageEntity image1 = ImageEntity.builder()
                    .url("uploads/UUID-EXC-001/UUID-EXC-001-1.png")
                    .inspection(inspection1)
                    .build();

            imageRepository.save(image1);

            inspection1.setImages(List.of(image1));
            inspectionRepository.save(inspection1);

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
                    .sparePart(part1)
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
                    .sparePart(part2)
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
