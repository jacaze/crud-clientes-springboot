package com.example.crud_banco_springboot.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro HTTP que injeta cabeçalhos de segurança básicos recomendados pela OWASP.
 */
@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Previne ataques de MIME sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");

        // Protege contra Clickjacking
        response.setHeader("X-Frame-Options", "DENY");

        // Ativa proteção XSS básica do navegador
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // Política de Referrer estrita
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Content Security Policy básica
        response.setHeader("Content-Security-Policy", "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:;");

        // Política de Permissões
        response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");

        filterChain.doFilter(request, response);
    }
}
