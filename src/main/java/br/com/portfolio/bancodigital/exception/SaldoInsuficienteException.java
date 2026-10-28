package br.com.portfolio.bancodigital.exception;

public class SaldoInsuficienteException extends RegraNegocioException {
    public SaldoInsuficienteException() {
        super("Saldo insuficiente para realizar a operacao.");
    }
}
