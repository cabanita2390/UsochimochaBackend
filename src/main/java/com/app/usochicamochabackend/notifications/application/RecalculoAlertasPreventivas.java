package com.app.usochicamochabackend.notifications.application;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Recalcula las alertas preventivas en segundo plano, después de confirmar la transacción que lo pide.
 * Cada inspección (vehículo, moto, máquina) recalculaba las alertas de toda la flota dentro de la
 * petición: unos 800 ms por inspección, y una cola del móvil con muchas pendientes se sentía lenta.
 * Varias solicitudes seguidas se juntan en un solo recálculo (con los datos ya guardados).
 */
@Slf4j
@Component
public class RecalculoAlertasPreventivas {

    private final PreventiveAlertCalculationService calculo;
    private final ExecutorService ejecutor = Executors.newSingleThreadExecutor(r -> {
        Thread hilo = new Thread(r, "recalculo-alertas");
        hilo.setDaemon(true);
        return hilo;
    });
    /** Hay un recálculo encolado que todavía no empezó: los pedidos que lleguen mientras tanto ya quedan cubiertos. */
    private final AtomicBoolean encolado = new AtomicBoolean(false);

    public RecalculoAlertasPreventivas(PreventiveAlertCalculationService calculo) {
        this.calculo = calculo;
    }

    /** Pide un recálculo: corre cuando se confirme la transacción en curso (o ya, si no hay una). */
    public void solicitar() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    encolar();
                }
            });
        } else {
            encolar();
        }
    }

    private void encolar() {
        if (!encolado.compareAndSet(false, true)) {
            return;
        }
        ejecutor.execute(() -> {
            // Se libera al empezar: lo que se guarde durante este recálculo pide uno nuevo y no se pierde.
            encolado.set(false);
            try {
                calculo.calculateAndEmitAlerts();
            } catch (Exception e) {
                log.warn("[Alertas] No se pudo recalcular en segundo plano: {}", e.getMessage(), e);
            }
        });
    }

    @PreDestroy
    void cerrar() {
        ejecutor.shutdown();
    }
}
