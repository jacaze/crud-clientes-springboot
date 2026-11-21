package com.example.crud_banco_springboot.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO para operações de transferência entre contas bancárias.
 */
public record TransferenciaRequestDTO(
        @NotNull(message = "O ID da conta de origem é obrigatório")
        @Positive(message = "O ID da conta de origem deve ser maior que zero")
        Long contaOrigemId,

        @NotNull(message = "O ID da conta de destino é obrigatório")
        @Positive(message = "O ID da conta de destino deve ser maior que zero")
        Long contaDestinoId,

        @NotNull(message = "O valor da transferência é obrigatório")
        @Positive(message = "O valor deve ser maior que zero")
        BigDecimal valor
) {
}
