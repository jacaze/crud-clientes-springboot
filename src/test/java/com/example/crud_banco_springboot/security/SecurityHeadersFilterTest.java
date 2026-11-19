package com.example.crud_banco_springboot.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SecurityHeadersFilterTest {

    @Test
    @DisplayName("Deve injetar todos os cabeçalhos de segurança OWASP recomendados na resposta")
    void deveInjetarCabecalhosDeSeguranca() throws ServletException, IOException {
        SecurityHeadersFilter filter = new SecurityHeadersFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/contas");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
        assertEquals("DENY", response.getHeader("X-Frame-Options"));
        assertEquals("1; mode=block", response.getHeader("X-XSS-Protection"));
        assertEquals("strict-origin-when-cross-origin", response.getHeader("Referrer-Policy"));
        assertEquals("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data:;", response.getHeader("Content-Security-Policy"));
        assertEquals("camera=(), microphone=(), geolocation=()", response.getHeader("Permissions-Policy"));

        verify(chain).doFilter(request, response);
    }
}
