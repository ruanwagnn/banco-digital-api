package br.com.portfolio.bancodigital.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Conta bancaria. O saldo e sempre BigDecimal (nunca double, para nao perder centavos).
 * A anotacao @Version habilita bloqueio otimista: se duas operacoes tentarem alterar
 * a mesma conta ao mesmo tempo, o JPA lanca OptimisticLockException em vez de gravar
 * um saldo errado.
 */
@Entity
@Table(name = "contas", uniqueConstraints = @UniqueConstraint(columnNames = "numero"))
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 12)
    private String numero;

    @Column(nullable = false, length = 4)
    private String agencia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean ativa = true;

    @Version
    private Long versao;

    @Column(nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

    protected Conta() {
    }

    public Conta(String numero, String agencia, Cliente cliente) {
        this.numero = numero;
        this.agencia = agencia;
        this.cliente = cliente;
        this.saldo = BigDecimal.ZERO;
    }

    public void creditar(BigDecimal valor) {
        this.saldo = this.saldo.add(valor);
    }

    public void debitar(BigDecimal valor) {
        this.saldo = this.saldo.subtract(valor);
    }

    public boolean temSaldoSuficiente(BigDecimal valor) {
        return this.saldo.compareTo(valor) >= 0;
    }

    public Long getId() { return id; }
    public String getNumero() { return numero; }
    public String getAgencia() { return agencia; }
    public Cliente getCliente() { return cliente; }
    public BigDecimal getSaldo() { return saldo; }
    public boolean isAtiva() { return ativa; }
    public Long getVersao() { return versao; }
    public Instant getCriadoEm() { return criadoEm; }

    public void setAtiva(boolean ativa) { this.ativa = ativa; }
}
