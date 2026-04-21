package com.app.usochicamochabackend.vehicle.infrastructure.repository;

import com.app.usochicamochabackend.vehicle.infrastructure.entity.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {

    @Query(nativeQuery = true, value = """
            SELECT
                v.id_vehiculo       AS "id",
                v.placa             AS "placa",
                m.descripcion       AS "marca",
                t.nombre_tipo       AS "tipoVehiculo",
                v.kilometraje_actual AS "kilometrajeActual"
            FROM vehiculos v
            JOIN cat_marcas_modelos  m ON v.id_marca          = m.id_marca
            JOIN cat_tipos_vehiculo  t ON v.id_tipo_vehiculo  = t.id_tipo_vehiculo
            WHERE v.activo = TRUE
            ORDER BY v.placa
            """)
    List<VehicleProjection> findAllActiveVehicles();

    /**
     * Actualiza el kilometraje del vehículo al guardar una inspección
     * pre-operativa.
     */
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE vehiculos SET kilometraje_actual = :km WHERE id_vehiculo = :idVehiculo")
    void updateKilometraje(@Param("idVehiculo") Integer idVehiculo, @Param("km") Integer km);

    /** Busca el vehículo por placa — el Android envía la placa, no el id. */
    java.util.Optional<VehicleEntity> findByPlaca(String placa);

    /**
     * Busca un vehículo activo por placa resolviendo marca y tipo via JOIN.
     * Retorna null si no existe.
     */
    @Query(nativeQuery = true, value = """
            SELECT
                v.id_vehiculo       AS "id",
                v.placa             AS "placa",
                m.descripcion       AS "marca",
                t.nombre_tipo       AS "tipoVehiculo",
                v.kilometraje_actual AS "kilometrajeActual"
            FROM vehiculos v
            JOIN cat_marcas_modelos  m ON v.id_marca         = m.id_marca
            JOIN cat_tipos_vehiculo  t ON v.id_tipo_vehiculo = t.id_tipo_vehiculo
            WHERE v.placa = :placa
              AND v.activo = TRUE
            LIMIT 1
            """)
    java.util.Optional<VehicleProjection> findVehicleDetailByPlaca(@Param("placa") String placa);
}
