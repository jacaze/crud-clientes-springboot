package com.example.crud_banco_springboot.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO para operações de depósito e saque que exigem um valor monetário estritamente positivo.
 */
public record TransacaoValorDTO(
        @NotNull(message = "O valor da transação é obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        BigDecimal valor
) {
}
