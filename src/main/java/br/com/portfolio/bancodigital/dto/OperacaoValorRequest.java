package br.com.portfolio.bancodigital.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Usado em deposito e saque. */
public record OperacaoValorRequest(
        @NotNull(message = "O valor e obrigatorio.")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero.")
        @Digits(integer = 17, fraction = 2, message = "Valor monetario invalido.")
        BigDecimal valor
) {
}
