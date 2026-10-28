package br.com.portfolio.bancodigital.dto;

import br.com.portfolio.bancodigital.domain.Conta;
import java.math.BigDecimal;

public record ContaResponse(
        String numero,
        String agencia,
        String titular,
        BigDecimal saldo
) {
    public static ContaResponse de(Conta conta) {
        return new ContaResponse(
                conta.getNumero(),
                conta.getAgencia(),
                conta.getCliente().getNome(),
                conta.getSaldo());
    }
}
