package com.example.Mi.casita.segura.acceso.websocket;

import com.example.Mi.casita.segura.auth.service.CustomUserDetailsService;
import com.example.Mi.casita.segura.auth.service.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

public class JwtHandshakeInterceptor implements HandshakeInterceptor {
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtHandshakeInterceptor(JwtService jwtService,
                                   CustomUserDetailsService uds) {
        this.jwtService = jwtService;
        this.userDetailsService = uds;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        // 1) Intenta extraer de header Authorization
        String token = null;
        List<String> auth = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (auth != null && !auth.isEmpty() && auth.get(0).startsWith("Bearer ")) {
            token = auth.get(0).substring(7);
        }

        // 2) Si no había header, parsea query string ?token=…
        if (token == null) {
            String query = request.getURI().getQuery();  // e.g. "token=eyJ…"
            if (query != null) {
                for (String param : query.split("&")) {
                    if (param.startsWith("token=")) {
                        token = param.substring("token=".length());
                        break;
                    }
                }
            }
        }

        // 3) Validación final
        if (token == null) {
            return false;
        }
        String username = jwtService.extractUsername(token);
        if (username == null) {
            return false;
        }
        var userDetails = userDetailsService.loadUserByUsername(username);
        if (!jwtService.isTokenValid(token, userDetails)) {
            return false;
        }

        // 4) Guarda datos en sesión
        attributes.put("username", username);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) { }
}