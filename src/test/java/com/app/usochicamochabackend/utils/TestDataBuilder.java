package com.app.usochicamochabackend.utils;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import com.app.usochicamochabackend.order.infrastructure.entity.OrderEntity;
import com.app.usochicamochabackend.performance.infrastructure.entity.ResultEntity;
import com.app.usochicamochabackend.performance.infrastructure.entity.LaborEntity;
import com.app.usochicamochabackend.performance.infrastructure.entity.SparePartEntity;
import com.app.usochicamochabackend.update.infrastructure.entity.BrandEntity;
import com.app.usochicamochabackend.update.infrastructure.entity.OilChangeEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TestDataBuilder {

    public static UserEntity createTestUser() {
        return UserEntity.builder()
                .id(1L)
                .fullName("Test User")
                .username("testuser")
                .email("test@example.com")
                .role("ADMIN")
                .password("$2a$10$encoded.password")
                .status(true)
                .build();
    }

    public static UserEntity createTestMechanic() {
        return UserEntity.builder()
                .id(2L)
                .fullName("Test Mechanic")
                .username("mechanic")
                .email("mechanic@example.com")
                .role("MECHANIC")
                .password("$2a$10$encoded.password")
                .status(true)
                .build();
    }

    public static MachineEntity createTestMachine() {
        return MachineEntity.builder()
                .id(1L)
                .name("Test Machine")
                .model("Model X")
                .belongsTo("Test Company")
                .soat(LocalDate.now().plusMonths(6))
                .brand("Test Brand")
                .runt(LocalDate.now().plusMonths(12))
                .status(true)
                .numEngine("ENG123")
                .numInterIdentification("ID123")
                .build();
    }

    public static InspectionEntity createTestInspection(MachineEntity machine, UserEntity user) {
        return InspectionEntity.builder()
                .id(1L)
                .UUID("test-uuid-123")
                .unexpected(false)
                .dateStamp(LocalDateTime.now())
                .hourMeter(100.0)
                .leakStatus("GOOD")
                .brakeStatus("GOOD")
                .beltsPulleysStatus("GOOD")
                .tireLanesStatus("GOOD")
                .carIgnitionStatus("GOOD")
                .electricalStatus("GOOD")
                .mechanicalStatus("GOOD")
                .temperatureStatus("GOOD")
                .oilStatus("GOOD")
                .hydraulicStatus("GOOD")
                .coolantStatus("GOOD")
                .structuralStatus("GOOD")
                .expirationDateFireExtinguisher("2024-12-31")
                .observations("Test observations")
                .greasingAction("Applied")
                .greasingObservations("All points greased")
                .machine(machine)
                .user(user)
                .build();
    }

    public static OrderEntity createTestOrder(InspectionEntity inspection, UserEntity assignerUser) {
        return OrderEntity.builder()
                .id(1L)
                .status("PENDING")
                .date(LocalDateTime.now())
                .description("Test order description")
                .inspection(inspection)
                .assignerUser(assignerUser)
                .build();
    }

    public static ResultEntity createTestResult(OrderEntity order) {
        return ResultEntity.builder()
                .id(1L)
                .date(LocalDateTime.now())
                .description("Test result description")
                .timeSpent("2 hours")
                .order(order)
                .laborForce(createTestLabor())
                .sparePart(createTestSparePart())
                .build();
    }

    public static LaborEntity createTestLabor() {
        return LaborEntity.builder()
                .id(1L)
                .date(LocalDateTime.now())
                .price(new BigDecimal("150.00"))
                .sameMecanic(true)
                .contractor("Test Contractor")
                .observations("Labor completed successfully")
                .build();
    }

    public static SparePartEntity createTestSparePart() {
        return SparePartEntity.builder()
                .id(1L)
                .ref("SP001")
                .name("Test Spare Part")
                .quantity("2")
                .price(new BigDecimal("50.00"))
                .build();
    }

    public static BrandEntity createTestBrand() {
        return BrandEntity.builder()
                .id(1L)
                .type("OIL")
                .name("Test Brand")
                .status(true)
                .build();
    }

    public static OilChangeEntity createTestOilChange(MachineEntity machine, BrandEntity brand) {
        return OilChangeEntity.builder()
                .id(1L)
                .dateStamp(LocalDateTime.now())
                .hydraulicOil(true)
                .motorOil(false)
                .brand(brand)
                .quantity(5)
                .hourMeter(100.0)
                .averageHoursChange(250)
                .machine(machine)
                .build();
    }
}
