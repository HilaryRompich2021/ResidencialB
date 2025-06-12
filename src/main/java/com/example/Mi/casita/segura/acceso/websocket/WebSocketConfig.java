package com.example.Mi.casita.segura.acceso.websocket;

import com.example.Mi.casita.segura.auth.service.CustomUserDetailsService;
import com.example.Mi.casita.segura.auth.service.JwtService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.util.Collections;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final TalanqueraWebSocketHandler handler;
    private final JwtHandshakeInterceptor jwtInterceptor;

    public WebSocketConfig(TalanqueraWebSocketHandler handler,
                           JwtService jwtService,
                           CustomUserDetailsService uds) {
        this.handler        = handler;
        this.jwtInterceptor = new JwtHandshakeInterceptor(jwtService, uds);
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        DefaultHandshakeHandler handshakeHandler = new DefaultHandshakeHandler();
        // <-- aquí, pasamos un String[] (varargs), no una List
        handshakeHandler.setSupportedProtocols(new String[]{ "arduino" });

        registry
                .addHandler(handler, "/ws/talanquera")
                .addInterceptors(jwtInterceptor)
                .setAllowedOrigins("*")
                .setHandshakeHandler(handshakeHandler);
    }
}