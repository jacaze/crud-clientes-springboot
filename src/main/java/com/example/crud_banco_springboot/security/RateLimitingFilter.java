package com.example.crud_banco_springboot.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Filtro de limitação de taxa (Rate Limiting) em memória baseado em janela de tempo (Fixed Window Counter).
 * Protege a API contra ataques de negação de serviço (DoS) e força bruta.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    @Value("${app.security.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${app.security.rate-limit.max-requests-per-minute:60}")
    private int maxRequestsPerMinute;

    private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();

    private static class RequestCounter {
        private final long windowStartTime;
        private final AtomicInteger count;

        public RequestCounter(long windowStartTime) {
            this.windowStartTime = windowStartTime;
            this.count = new AtomicInteger(1);
        }

        public long getWindowStartTime() {
            return windowStartTime;
        }

        public AtomicInteger getCount() {
            return count;
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = extractClientIp(request);
        long currentTimeMillis = System.currentTimeMillis();
        long windowDurationMillis = 60_000L; // 1 minuto

        RequestCounter counter = requestCounts.compute(clientIp, (key, existingCounter) -> {
            if (existingCounter == null || (currentTimeMillis - existingCounter.getWindowStartTime()) > windowDurationMillis) {
                return new RequestCounter(currentTimeMillis);
            }
            existingCounter.getCount().incrementAndGet();
            return existingCounter;
        });

        int currentRequests = counter.getCount().get();
        int remaining = Math.max(0, maxRequestsPerMinute - currentRequests);

        response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequestsPerMinute));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));

        if (currentRequests > maxRequestsPerMinute) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("""
                    {
                      "status": 429,
                      "erro": "Too Many Requests",
                      "mensagem": "Limite de requisicoes excedido. Tente novamente em instantes."
                    }
                    """);
            return;
        }

        // Limpeza periódica leve para evitar vazamento de memória com IPs antigos
        if (requestCounts.size() > 5000) {
            requestCounts.entrySet().removeIf(entry ->
                    currentTimeMillis - entry.getValue().getWindowStartTime() > windowDurationMillis * 2);
        }

        filterChain.doFilter(request, response);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    // Método exposto para facilitar testes unitários
    public void resetCounts() {
        requestCounts.clear();
    }
}
