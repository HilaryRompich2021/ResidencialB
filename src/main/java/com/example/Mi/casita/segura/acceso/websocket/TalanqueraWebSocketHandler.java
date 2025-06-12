package com.example.Mi.casita.segura.acceso.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * Handler que:
 *  - Registra la sesión de entrada y salida al recibir "register:entrada" / "register:salida"
 *  - Permite enviar mensajes sólo a esos dos clientes
 */
@Component
public class TalanqueraWebSocketHandler extends TextWebSocketHandler {
    private WebSocketSession entradaSession;
    private WebSocketSession salidaSession;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage msg) throws Exception {
        String p = msg.getPayload();
        if ("register:entrada".equals(p)) {
            entradaSession = session;
            session.sendMessage(new TextMessage("registered:entrada"));
        } else if ("register:salida".equals(p)) {
            salidaSession = session;
            session.sendMessage(new TextMessage("registered:salida"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        if (session == entradaSession)  entradaSession = null;
        if (session == salidaSession)   salidaSession  = null;
    }

    public void broadcast(String message) {
        TextMessage tm = new TextMessage(message);
        try {
            if (entradaSession != null && entradaSession.isOpen())
                entradaSession.sendMessage(tm);
            if (salidaSession  != null && salidaSession.isOpen())
                salidaSession.sendMessage(tm);
        } catch (Exception e) { e.printStackTrace(); }
    }
}

