package br.com.portfolio.bancodigital.exception;

/** Erro de regra de negocio -> vira HTTP 422 (Unprocessable Entity). */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
