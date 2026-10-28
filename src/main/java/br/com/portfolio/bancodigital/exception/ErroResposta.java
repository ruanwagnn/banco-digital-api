package br.com.portfolio.bancodigital.exception;

import java.time.Instant;
import java.util.Map;

/** Corpo padrao de erro devolvido pela API. */
public record ErroResposta(
        Instant timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        Map<String, String> camposInvalidos
) {
    public ErroResposta(int status, String erro, String mensagem, String caminho) {
        this(Instant.now(), status, erro, mensagem, caminho, null);
    }
}
