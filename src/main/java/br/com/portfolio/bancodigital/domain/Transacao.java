package br.com.portfolio.bancodigital.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transacoes", indexes = {
        @Index(name = "idx_transacao_conta", columnList = "conta_id"),
        @Index(name = "idx_transacao_idempotency", columnList = "idempotencyKey", unique = true)
})
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_id", nullable = false)
    private Conta conta;

    @Column(length = 12)
    private String contaContraparte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTransacao tipo;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldoApos;

    @Column(updatable = false)
    private String idempotencyKey;

    @Column(nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

    protected Transacao() {
    }

    public Transacao(Conta conta, String contaContraparte, TipoTransacao tipo,
                     BigDecimal valor, BigDecimal saldoApos, String idempotencyKey) {
        this.conta = conta;
        this.contaContraparte = contaContraparte;
        this.tipo = tipo;
        this.valor = valor;
        this.saldoApos = saldoApos;
        this.idempotencyKey = idempotencyKey;
    }

    public Long getId() { return id; }
    public Conta getConta() { return conta; }
    public String getContaContraparte() { return contaContraparte; }
    public TipoTransacao getTipo() { return tipo; }
    public BigDecimal getValor() { return valor; }
    public BigDecimal getSaldoApos() { return saldoApos; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Instant getCriadoEm() { return criadoEm; }
}
