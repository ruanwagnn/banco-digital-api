package br.com.portfolio.bancodigital.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record TransferenciaRequest(
        @NotBlank(message = "A conta de destino e obrigatoria.")
        String contaDestino,

        @NotNull(message = "O valor e obrigatorio.")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero.")
        @Digits(integer = 17, fraction = 2, message = "Valor monetario invalido.")
        BigDecimal valor
) {
}
