package com.example.crud_banco_springboot.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tratamento global de exceções da API.
 * Padroniza respostas de erro de validação e evita vazamento de stack traces (SEC-08).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> camposComErro = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            camposComErro.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("timestamp", LocalDateTime.now().toString());
        resposta.put("status", HttpStatus.BAD_REQUEST.value());
        resposta.put("erro", "Erro de validação nos dados enviados");
        resposta.put("campos", camposComErro);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedJson(HttpMessageNotReadableException ex) {
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("timestamp", LocalDateTime.now().toString());
        resposta.put("status", HttpStatus.BAD_REQUEST.value());
        resposta.put("erro", "Requisição malformada");
        resposta.put("mensagem", "O corpo da requisição contém formato JSON inválido ou tipos de dados incompatíveis.");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
    }
}
