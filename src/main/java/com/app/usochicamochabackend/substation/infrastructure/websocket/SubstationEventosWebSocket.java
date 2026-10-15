package com.app.usochicamochabackend.substation.infrastructure.websocket;

import com.app.usochicamochabackend.substation.application.port.SubstationEventosPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubstationEventosWebSocket implements SubstationEventosPort {

    public static final String TOPIC_EJECUCIONES = "/topic/subestaciones/ejecuciones";

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void ejecucionCambio(Long estacionId, Long ejecucionId, String tipo) {
        Map<String, Object> evento = Map.of("estacionId", estacionId, "ejecucionId", ejecucionId, "tipo", tipo);
        // Solo después del commit: si la transacción se revierte la web no debe recargar por nada,
        // y si recarga antes del commit no vería el registro nuevo.
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    enviar(evento);
                }
            });
        } else {
            enviar(evento);
        }
    }

    private void enviar(Map<String, Object> evento) {
        // Best-effort: un fallo del broker nunca debe tumbar el registro que ya se guardó.
        try {
            messagingTemplate.convertAndSend(TOPIC_EJECUCIONES, evento);
        } catch (RuntimeException e) {
            log.warn("No se pudo avisar por WebSocket el cambio de ejecución {}", evento, e);
        }
    }
}
