package com.app.usochicamochabackend.order.application.port;

import com.app.usochicamochabackend.order.application.dto.OrderWithVehicleDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetAllVehicleOrdersUseCase {
    Page<OrderWithVehicleDTO> getAllVehicleOrders(Pageable pageable);

    /**
     * @param soloMotos null = sin filtrar (vehículos + motos), true = solo motos,
     *                  false = solo no-motos. El filtro se aplica a nivel de base
     *                  de datos para no romper la paginación.
     */
    default Page<OrderWithVehicleDTO> getAllVehicleOrders(Pageable pageable, Boolean soloMotos) {
        return getAllVehicleOrders(pageable);
    }
}
