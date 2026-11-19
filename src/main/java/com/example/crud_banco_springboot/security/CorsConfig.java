package com.example.crud_banco_springboot.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Configuração de CORS (Cross-Origin Resource Sharing).
 * Controla quais origens externas podem consumir a API do banco.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.security.cors.allowed-origins:http://localhost:3000,http://localhost:4200,http://localhost:5173}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);

        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("X-RateLimit-Limit", "X-RateLimit-Remaining")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
