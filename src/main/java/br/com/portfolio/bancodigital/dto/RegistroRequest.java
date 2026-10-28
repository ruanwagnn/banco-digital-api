package br.com.portfolio.bancodigital.dto;

import jakarta.validation.constraints.*;

public record RegistroRequest(
        @NotBlank(message = "O nome e obrigatorio.")
        String nome,

        @NotBlank(message = "O CPF e obrigatorio.")
        @Pattern(regexp = "\\d{11}", message = "O CPF deve ter 11 digitos, somente numeros.")
        String cpf,

        @NotBlank(message = "O e-mail e obrigatorio.")
        @Email(message = "E-mail invalido.")
        String email,

        @NotBlank(message = "A senha e obrigatoria.")
        @Size(min = 8, message = "A senha deve ter ao menos 8 caracteres.")
        String senha
) {
}
