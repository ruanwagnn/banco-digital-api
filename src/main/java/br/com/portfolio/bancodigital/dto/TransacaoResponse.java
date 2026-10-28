package br.com.portfolio.bancodigital.dto;

import br.com.portfolio.bancodigital.domain.TipoTransacao;
import br.com.portfolio.bancodigital.domain.Transacao;
import java.math.BigDecimal;
import java.time.Instant;

public record TransacaoResponse(
        Long id,
        TipoTransacao tipo,
        BigDecimal valor,
        BigDecimal saldoApos,
        String contaContraparte,
        Instant criadoEm
) {
    public static TransacaoResponse de(Transacao t) {
        return new TransacaoResponse(
                t.getId(), t.getTipo(), t.getValor(),
                t.getSaldoApos(), t.getContaContraparte(), t.getCriadoEm());
    }
}
