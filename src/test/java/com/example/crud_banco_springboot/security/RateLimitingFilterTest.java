package com.example.crud_banco_springboot.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RateLimitingFilterTest {

    private RateLimitingFilter rateLimitingFilter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        rateLimitingFilter = new RateLimitingFilter();
        ReflectionTestUtils.setField(rateLimitingFilter, "enabled", true);
        ReflectionTestUtils.setField(rateLimitingFilter, "maxRequestsPerMinute", 3);
        rateLimitingFilter.resetCounts();
        filterChain = mock(FilterChain.class);
    }

    @Test
    @DisplayName("Deve permitir requisições dentro do limite permitido")
    void devePermitirRequisicoesAbaixoDoLimite() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/clientes");
        request.setRemoteAddr("192.168.1.10");
        MockHttpServletResponse response = new MockHttpServletResponse();

        rateLimitingFilter.doFilter(request, response, filterChain);

        assertEquals(200, response.getStatus());
        assertEquals("3", response.getHeader("X-RateLimit-Limit"));
        assertEquals("2", response.getHeader("X-RateLimit-Remaining"));
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve bloquear e retornar 429 quando exceder o limite de requisições por minuto")
    void deveBloquearQuandoExcederLimite() throws ServletException, IOException {
        String clientIp = "10.0.0.50";

        for (int i = 1; i <= 3; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/contas");
            req.setRemoteAddr(clientIp);
            MockHttpServletResponse res = new MockHttpServletResponse();
            rateLimitingFilter.doFilter(req, res, filterChain);
            assertEquals(200, res.getStatus());
        }

        // 4ª requisição (excede o limite de 3)
        MockHttpServletRequest blockedRequest = new MockHttpServletRequest("GET", "/contas");
        blockedRequest.setRemoteAddr(clientIp);
        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();

        rateLimitingFilter.doFilter(blockedRequest, blockedResponse, filterChain);

        assertEquals(429, blockedResponse.getStatus());
        assertEquals("0", blockedResponse.getHeader("X-RateLimit-Remaining"));
        // filterChain só deve ter sido chamado 3 vezes (não chamado na 4ª)
        verify(filterChain, times(3)).doFilter(any(), any());
    }

    @Test
    @DisplayName("Deve respeitar IP extraído do cabeçalho X-Forwarded-For")
    void deveExtrairIpDoHeaderXForwardedFor() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/transacoes");
        request.addHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18");
        MockHttpServletResponse response = new MockHttpServletResponse();

        rateLimitingFilter.doFilter(request, response, filterChain);

        assertEquals(200, response.getStatus());
        assertEquals("2", response.getHeader("X-RateLimit-Remaining"));
    }

    @Test
    @DisplayName("Deve permitir requisições livremente quando rate limiting estiver desabilitado")
    void devePermitirRequisicoesQuandoDesabilitado() throws ServletException, IOException {
        ReflectionTestUtils.setField(rateLimitingFilter, "enabled", false);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/clientes");
        MockHttpServletResponse response = new MockHttpServletResponse();

        rateLimitingFilter.doFilter(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain, times(1)).doFilter(request, response);
    }
}
