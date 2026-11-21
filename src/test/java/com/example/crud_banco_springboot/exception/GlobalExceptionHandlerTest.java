package com.example.crud_banco_springboot.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Deve formatar erros de validação em resposta HTTP 400 sem vazar stack trace")
    void deveFormatarErrosDeValidacao() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "transacaoValorDTO");
        bindingResult.addError(new FieldError("transacaoValorDTO", "valor", "O valor deve ser maior que zero"));

        MethodParameter parameter = mock(MethodParameter.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidationErrors(ex);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Erro de validação nos dados enviados", response.getBody().get("erro"));

        @SuppressWarnings("unchecked")
        Map<String, String> campos = (Map<String, String>) response.getBody().get("campos");
        assertNotNull(campos);
        assertEquals("O valor deve ser maior que zero", campos.get("valor"));
    }

    @Test
    @DisplayName("Deve tratar JSON malformado com HTTP 400 amigável")
    void deveTratarJsonMalformado() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);

        ResponseEntity<Map<String, Object>> response = handler.handleMalformedJson(ex);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Requisição malformada", response.getBody().get("erro"));
    }
}
