package com.example.crud_banco_springboot.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CorsConfigTest {

    @Test
    @DisplayName("Deve registrar regras de CORS com origens configuradas")
    void deveRegistrarRegrasDeCors() {
        CorsConfig corsConfig = new CorsConfig();
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "http://localhost:3000, https://meubanco.com");

        CorsRegistry registry = mock(CorsRegistry.class);
        CorsRegistration registration = mock(CorsRegistration.class);

        when(registry.addMapping("/**")).thenReturn(registration);
        when(registration.allowedOrigins("http://localhost:3000", "https://meubanco.com")).thenReturn(registration);
        when(registration.allowedMethods(any(String[].class))).thenReturn(registration);
        when(registration.allowedHeaders("*")).thenReturn(registration);
        when(registration.exposedHeaders("X-RateLimit-Limit", "X-RateLimit-Remaining")).thenReturn(registration);
        when(registration.allowCredentials(true)).thenReturn(registration);
        when(registration.maxAge(3600)).thenReturn(registration);

        assertDoesNotThrow(() -> corsConfig.addCorsMappings(registry));

        verify(registry).addMapping("/**");
        verify(registration).allowedOrigins("http://localhost:3000", "https://meubanco.com");
    }
}
