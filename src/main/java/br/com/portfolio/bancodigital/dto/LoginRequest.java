package br.com.portfolio.bancodigital.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "O CPF e obrigatorio.") String cpf,
        @NotBlank(message = "A senha e obrigatoria.") String senha
) {
}
