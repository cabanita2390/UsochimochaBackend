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
                    .machine(excavator)
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
                    .machine(bulldozer)
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
                    .machine(excavator)
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
                    .machine(bulldozer)
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
                    .machine(excavator)
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
                    .machine(bulldozer)
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
                    .machine(excavator)
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
                    .machine(bulldozer)
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
                    .machine(excavator)
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
                    .machine(bulldozer)
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
                    .machine(excavator)
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
                    .machine(bulldozer)
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
                    .machine(excavator)
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
                    .machine(bulldozer)
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
                    .machine(excavator)
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
                    .machine(bulldozer)
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
                    .machine(excavator)
                    .user(mechanic)
                    .observations("Necesario reemplazar llantas inmediatamente")
                    .build();


            inspectionRepository.saveAll(List.of(inspection1, inspection2, inspection3, inspection4, inspection5, inspection6, inspection7, inspection8, inspection9, inspection10, inspection11, inspection12, inspection13, inspection14, inspection15, inspection16, inspection17));

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
