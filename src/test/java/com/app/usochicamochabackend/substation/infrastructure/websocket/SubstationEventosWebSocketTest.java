package com.app.usochicamochabackend.substation.infrastructure.websocket;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class SubstationEventosWebSocketTest {

    private final SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
    private final SubstationEventosWebSocket eventos = new SubstationEventosWebSocket(template);

    @AfterEach
    void limpiar() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void dentroDeUnaTransaccion_soloEnviaDespuesDelCommit() {
        TransactionSynchronizationManager.initSynchronization();

        eventos.ejecucionCambio(1L, 9L, "REGISTRADA");
        verify(template, never()).convertAndSend(anyString(), any(Object.class));

        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        verify(template).convertAndSend(SubstationEventosWebSocket.TOPIC_EJECUCIONES,
                (Object) Map.of("estacionId", 1L, "ejecucionId", 9L, "tipo", "REGISTRADA"));
    }

    @Test
    void sinTransaccion_enviaDeUna_yUnFalloDelBrokerNoSePropaga() {
        doThrow(new IllegalStateException("broker caído")).when(template).convertAndSend(eq(SubstationEventosWebSocket.TOPIC_EJECUCIONES), any(Object.class));

        eventos.ejecucionCambio(1L, 9L, "EDITADA");

        verify(template).convertAndSend(eq(SubstationEventosWebSocket.TOPIC_EJECUCIONES), any(Object.class));
    }
}
