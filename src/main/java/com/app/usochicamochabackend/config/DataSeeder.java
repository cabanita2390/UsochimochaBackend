package com.app.usochicamochabackend.config;

import com.app.usochicamochabackend.actions.infrastructure.repository.ActionRepository;
import com.app.usochicamochabackend.auth.infrastructure.entity.UserEntity;
import com.app.usochicamochabackend.auth.infrastructure.repository.UserRepositoryJpa;
import com.app.usochicamochabackend.machine.infrastructure.repository.MachineRepository;
import com.app.usochicamochabackend.update.infrastructure.repository.OilChangeRepository;
import com.app.usochicamochabackend.order.infrastructure.repository.OrderRepository;
import com.app.usochicamochabackend.performance.infrastructure.repository.ResultRepository;
import com.app.usochicamochabackend.review.infrastructure.repository.ImageRepository;
import com.app.usochicamochabackend.review.infrastructure.repository.InspectionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

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
            // ==========================================
            // 1. LIMPIEZA DE BASE DE DATOS (Orden Estricto)
            // ==========================================

            // Paso 0: Eliminar Auditoría (Rompe la relación con Users)
            // Sin esto, falla al intentar borrar usuarios.
            actionRepository.deleteAllInBatch();

            // Paso 1: Eliminar dependencias profundas (Ordenes y Resultados)
            orderRepository.deleteAllInBatch();
            resultRepository.deleteAllInBatch();

            // Paso 2: Eliminar dependencias medias (Imágenes, Inspecciones, Cambios de aceite)
            imageRepository.deleteAllInBatch();
            inspectionRepository.deleteAllInBatch();
            oilChangeRepository.deleteAllInBatch();

            // Paso 3: Eliminar entidades base (Máquinas y Usuarios)
            machineRepository.deleteAllInBatch();
            userRepository.deleteAllInBatch();

            // ==========================================
            // 2. CREACIÓN DE DATOS MÍNIMOS
            // ==========================================

            // Crear solo el Usuario Admin
            UserEntity admin = UserEntity.builder()
                    .fullName("Admin User")
                    .username("admin")
                    // El password es el hash de "password"
                    .password("$2a$10$cMY29RPYoIHMJSuwRfoD3eQxU1J5Rww4VnNOUOAEPqCBshkNfrEf6")
                    .email("admin@example.com")
                    .role("ADMIN")
                    .status(true)
                    .build();

            userRepository.save(admin);

            System.out.println("--> Base de datos limpiada exitosamente. Usuario Admin restaurado.");
        };
    }
}