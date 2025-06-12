package com.example.Mi.casita.segura.auth.service;


import com.example.Mi.casita.segura.visitantes.dto.VisitanteRegistroDTO;
import com.example.Mi.casita.segura.visitantes.model.Visitante;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        //Permiso sin autorizacion a /ws/**
                        .requestMatchers(HttpMethod.GET, "/ws/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()

                        .requestMatchers(
                                "/api/auth/login",
                                "/api/usuarios/registrar",
                                "/api/usuarios",
                                "/api/usuarios/**",
                                "/api/usuarios/{cui}",
                                "/api/usuarios",
                                "/api/visitantes/registro",
                                "/api/visitantes",
                                "/api/pagos/registrarPago",
                                "/api/pagos",
                                "/api/pagos/pendientes/*",
                                "/api/pagos/todos/**",
                                "/api/pagos/listar/**",
                                "/api/reservas/**",
                                "/api/pagos/cargo-agua"
                                )
                        .permitAll()
                        //.hasAnyRole("RESIDENTE", "ADMINISTRADOR")

                        //TICKETS
                        //Solo ADMINISTRADOR puede poner en proceso o completar:
                        .requestMatchers(HttpMethod.PUT, "/api/tickets/en-proceso", "/api/tickets/completar")
                        .hasRole("ADMINISTRADOR")

                        //POST /api/tickets lo pueden hacer RESIDENTE, GUARDIA o ADMINISTRADOR:
                        .requestMatchers(HttpMethod.POST, "/api/tickets")
                        .hasAnyRole("RESIDENTE", "GUARDIA", "ADMINISTRADOR")

                        //GET /api/tickets y GET /api/tickets/{id} para cualquier usuario autenticado:
                        .requestMatchers(HttpMethod.GET, "/api/tickets", "/api/tickets/*")
                        .authenticated()

                        //PAQUETES
                        // 1) Registro de paquetes: solo RESIDENTE puede
                        .requestMatchers(HttpMethod.POST, "/api/paquetes/registrar", "/api/paquetes/mis-paquetes")
                        .hasRole("RESIDENTE")

                        // 2) Validar código de llegada: solo GUARDIA puede
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/paquetes/validar-ingreso"
                        )
                        .hasRole("GUARDIA")

                        // 3) Validar código de entrega: solo GUARDIA puede
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/paquetes/validar-entrega"
                        )
                        .hasRole("GUARDIA")

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200", "https://micasitaseguraresidencialf-cnema5azfjbxdje0.canadacentral-01.azurewebsites.net"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
