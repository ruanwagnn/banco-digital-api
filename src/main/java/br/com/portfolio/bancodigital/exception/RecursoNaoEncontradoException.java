package br.com.portfolio.bancodigital.exception;

/** Recurso inexistente -> vira HTTP 404 (Not Found). */
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
