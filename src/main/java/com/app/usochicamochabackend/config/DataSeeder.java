package com.app.usochicamochabackend.config;

import com.app.usochicamochabackend.actions.infrastructure.repository.ActionRepository;
import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.machine.infrastructure.entity.MachineEntity;
import com.app.usochicamochabackend.machine.infrastructure.repository.MachineRepository;
import com.app.usochicamochabackend.order.infrastructure.repository.OrderRepository;
import com.app.usochicamochabackend.performance.infrastructure.repository.ResultRepository;
import com.app.usochicamochabackend.review.infrastructure.entity.InspectionEntity;
import com.app.usochicamochabackend.review.infrastructure.repository.InspectionRepository;
import com.app.usochicamochabackend.update.infrastructure.repository.ConsolidateRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@Profile("dev") // Es buena práctica ejecutar los seeders solo en entornos de desarrollo
public class DataSeeder {
    @Bean
    CommandLineRunner initData(
            UserRepositoryJpa userRepository,
            MachineRepository machineRepository,
            InspectionRepository inspectionRepository,
            OrderRepository orderRepository,
            ResultRepository resultRepository,
            ConsolidateRepository consolidateRepository,
            ActionRepository actionRepository
    ) {
        return args -> {
            // Limpiar repositorios para evitar conflictos al reiniciar
            inspectionRepository.deleteAll();
            // Puede que necesites borrar otras entidades dependientes primero
            // orderRepository.deleteAll();
            machineRepository.deleteAll();
            userRepository.deleteAll();

            // USUARIOS
            UserEntity admin = UserEntity.builder()
                    .fullName("Admin User")
                    .username("admin")
                    .password("$2a$10$cMY29RPYoIHMJSuwRfoD3eQxU1J5Rww4VnNOUOAEPqCBshkNfrEf6") // password is "password"
                    .email("admin@example.com")
                    .role("ADMIN")
                    .status(true)
                    .build();

            UserEntity mechanic = UserEntity.builder()
                    .fullName("Mechanic User")
                    .username("mechanic")
                    .password("$2a$10$cMY29RPYoIHMJSuwRfoD3eQxU1J5Rww4VnNOUOAEPqCBshkNfrEf6") // password is "password"
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
            // Esta entidad se actualiza para reflejar los campos que se muestran en el log de error.
            // Si ejecutas la aplicación con este seeder activo, fallará al arrancar si el esquema de la BD no es correcto,
            // lo que ayuda a identificar el problema antes.
            InspectionEntity inspection = InspectionEntity.builder()
                    .UUID("UUID-12345-SEEDER")
                    .dateStamp(LocalDateTime.now())
                    .hourMeter(new BigInteger("1234"))
                    .leakStatus("Óptimo")
                    .brakeStatus("Óptimo")
                    .beltsPulleysStatus("Óptimo")
                    .carIgnitionStatus("Óptimo")
                    .coolantStatus("Óptimo")
                    .electricalStatus("Óptimo")
                    .expirationDateFireExtinguisher("2025-08")
                    .hydraulicStatus("Óptimo")
                    .mechanicalStatus("Óptimo")
                    .oilStatus("Óptimo")
                    .structuralStatus("Óptimo")
                    .temperatureStatus("Óptimo")
                    .tireLanesStatus("Óptimo")
                    .greasingAction("No action required")
                    .greasingObservations("Greasing points checked, all OK.")
                    .unexpected(false)
                    .machine(machine1)
                    .user(admin)
                    .observations("Everything fine from seeder")
                    .build();

            inspectionRepository.save(inspection);

        };
    }
}
