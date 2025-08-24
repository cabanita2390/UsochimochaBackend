package com.app.usochicamochabackend.config;

import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;

import com.app.usochicamochabackend.machine.infrastructure.repository.MachineRepository;
import com.app.usochicamochabackend.review.infrastructure.repository.InspectionRepository;
import com.app.usochicamochabackend.order.infrastructure.repository.OrderRepository;
import com.app.usochicamochabackend.performance.infrastructure.repository.LaborRepository;
import com.app.usochicamochabackend.performance.infrastructure.repository.ResultRepository;
import com.app.usochicamochabackend.performance.infrastructure.repository.SparePartRepository;
import com.app.usochicamochabackend.update.infrastructure.repository.ConsolidateRepository;
import com.app.usochicamochabackend.actions.infrastructure.repository.ActionRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataSeeder {
/*
    @Bean
    CommandLineRunner initData(
            UserRepositoryJpa userRepository,
            MachineRepository machineRepository,
            InspectionRepository inspectionRepository,
            OrderRepository orderRepository,
            LaborRepository laborRepository,
            ResultRepository resultRepository,
            SparePartRepository sparePartRepository,
            ConsolidateRepository consolidateRepository,
            ActionRepository actionRepository
    ) {
        return args -> {

            // USUARIOS
            UserEntity admin = UserEntity.builder()
                    .fullName("Admin User")
                    .username("admin")
                    .password("$2a$10$cMY29RPYoIHMJSuwRfoD3eQxU1J5Rww4VnNOUOAEPqCBshkNfrEf6")
                    .email("admin@example.com")
                    .role("ADMIN")
                    .status(true)
                    .build();

            UserEntity mechanic = UserEntity.builder()
                    .fullName("Mechanic User")
                    .username("mechanic")
                    .password("$2a$10$cMY29RPYoIHMJSuwRfoD3eQxU1J5Rww4VnNOUOAEPqCBshkNfrEf6")
                    .email("mech@example.com")
                    .role("MECHANIC")
                    .status(true)
                    .build();

            userRepository.saveAll(List.of(admin, mechanic));

            // MÁQUINAS
            MachineEntity machine1 = MachineEntity.builder()
                    .name("Excavator")
                    .model("CAT320")
                    .soat(LocalDate.now().plusYears(1))
                    .brand("Caterpillar")
                    .runt(LocalDate.now().plusYears(1))
                    .numEngine("ENG12345")
                    .numInterIdentification("CHASIS123")
                    .build();

            machineRepository.save(machine1);

            // INSPECCIÓN
            InspectionEntity inspection = InspectionEntity.builder()
                    .UUID("UUID-12345")
                    .dateStamp(LocalDateTime.now())
                    .hourmeter("1200h")
                    .leakStatus("OK")
                    .brakeStatus("OK")
                    .machine(machine1)
                    .user(admin)
                    .observations("Everything fine")
                    .build();

            inspectionRepository.save(inspection);

        };
    }

 */
}
