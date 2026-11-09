package com.app.usochicamochabackend.notifications.application;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class RecalculoAlertasPreventivasTest {

    private final PreventiveAlertCalculationService calculo = mock(PreventiveAlertCalculationService.class);
    private final RecalculoAlertasPreventivas recalculo = new RecalculoAlertasPreventivas(calculo);

    @AfterEach
    void limpiar() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        recalculo.cerrar();
    }

    @Test
    void sinTransaccion_recalculaEnSegundoPlano() {
        recalculo.solicitar();

        verify(calculo, timeout(2000)).calculateAndEmitAlerts();
    }

    @Test
    void conTransaccion_esperaAQueSeConfirme() {
        TransactionSynchronizationManager.initSynchronization();
        recalculo.solicitar();
        verify(calculo, after(300).never()).calculateAndEmitAlerts();

        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);

        verify(calculo, timeout(2000)).calculateAndEmitAlerts();
    }

    @Test
    void variosPedidosMientrasCorreUno_seJuntan() throws Exception {
        CountDownLatch corriendo = new CountDownLatch(1);
        CountDownLatch soltar = new CountDownLatch(1);
        doAnswer(inv -> {
            corriendo.countDown();
            soltar.await(2, TimeUnit.SECONDS);
            return null;
        }).doNothing().when(calculo).calculateAndEmitAlerts();

        recalculo.solicitar();
        assertTrue(corriendo.await(2, TimeUnit.SECONDS));
        for (int i = 0; i < 20; i++) {
            recalculo.solicitar(); // llegan mientras el primero sigue corriendo
        }
        soltar.countDown();

        // El primero más uno solo para los 20 que llegaron durante él.
        verify(calculo, timeout(2000).times(2)).calculateAndEmitAlerts();
        verify(calculo, after(300).times(2)).calculateAndEmitAlerts();
    }
}
