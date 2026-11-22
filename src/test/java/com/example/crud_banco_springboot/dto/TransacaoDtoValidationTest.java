package com.example.crud_banco_springboot.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransacaoDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Deve validar TransacaoValorDTO com valor positivo")
    void deveAceitarTransacaoValorPositivo() {
        TransacaoValorDTO dto = new TransacaoValorDTO(new BigDecimal("10.50"));
        Set<ConstraintViolation<TransacaoValorDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Deve rejeitar TransacaoValorDTO com valor negativo, zero ou nulo")
    void deveRejeitarTransacaoValorInvalido() {
        Set<ConstraintViolation<TransacaoValorDTO>> violacoesNegativo =
                validator.validate(new TransacaoValorDTO(new BigDecimal("-0.01")));
        Set<ConstraintViolation<TransacaoValorDTO>> violacoesZero =
                validator.validate(new TransacaoValorDTO(BigDecimal.ZERO));
        Set<ConstraintViolation<TransacaoValorDTO>> violacoesNulo =
                validator.validate(new TransacaoValorDTO(null));

        assertFalse(violacoesNegativo.isEmpty());
        assertFalse(violacoesZero.isEmpty());
        assertFalse(violacoesNulo.isEmpty());
    }

    @Test
    @DisplayName("Deve validar TransferenciaRequestDTO com todos os campos válidos")
    void deveAceitarTransferenciaRequestValido() {
        TransferenciaRequestDTO dto = new TransferenciaRequestDTO(1L, 2L, new BigDecimal("250.00"));
        Set<ConstraintViolation<TransferenciaRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Deve rejeitar TransferenciaRequestDTO com IDs nulos/inválidos ou valor negativo")
    void deveRejeitarTransferenciaRequestInvalido() {
        Set<ConstraintViolation<TransferenciaRequestDTO>> violacoes =
                validator.validate(new TransferenciaRequestDTO(null, -1L, new BigDecimal("-50.00")));
        assertFalse(violacoes.isEmpty());
        assertTrue(violacoes.size() >= 3);
    }
}
