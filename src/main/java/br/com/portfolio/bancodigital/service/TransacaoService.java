package br.com.portfolio.bancodigital.service;

import br.com.portfolio.bancodigital.domain.Conta;
import br.com.portfolio.bancodigital.domain.TipoTransacao;
import br.com.portfolio.bancodigital.domain.Transacao;
import br.com.portfolio.bancodigital.exception.RecursoNaoEncontradoException;
import br.com.portfolio.bancodigital.exception.RegraNegocioException;
import br.com.portfolio.bancodigital.exception.SaldoInsuficienteException;
import br.com.portfolio.bancodigital.repository.ContaRepository;
import br.com.portfolio.bancodigital.repository.TransacaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransacaoService {

    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;

    public TransacaoService(ContaRepository contaRepository,
                            TransacaoRepository transacaoRepository) {
        this.contaRepository = contaRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional
    public Transacao depositar(String numeroConta, BigDecimal valor, String idempotencyKey) {
        Transacao existente = buscarPorIdempotency(idempotencyKey);
        if (existente != null) {
            return existente;
        }
        validarValorPositivo(valor);
        Conta conta = travarConta(numeroConta);
        conta.creditar(valor);
        return registrar(conta, null, TipoTransacao.DEPOSITO, valor, idempotencyKey);
    }

    @Transactional
    public Transacao sacar(String numeroConta, BigDecimal valor, String idempotencyKey) {
        Transacao existente = buscarPorIdempotency(idempotencyKey);
        if (existente != null) {
            return existente;
        }
        validarValorPositivo(valor);
        Conta conta = travarConta(numeroConta);
        if (!conta.temSaldoSuficiente(valor)) {
            throw new SaldoInsuficienteException();
        }
        conta.debitar(valor);
        return registrar(conta, null, TipoTransacao.SAQUE, valor, idempotencyKey);
    }

    @Transactional
    public Transacao transferir(String numeroOrigem, String numeroDestino,
                                BigDecimal valor, String idempotencyKey) {
        Transacao existente = buscarPorIdempotency(idempotencyKey);
        if (existente != null) {
            return existente;
        }
        validarValorPositivo(valor);
        if (numeroOrigem.equals(numeroDestino)) {
            throw new RegraNegocioException("Nao e possivel transferir para a propria conta.");
        }

        // Trava as duas contas sempre na mesma ordem (menor numero primeiro) para
        // evitar deadlock quando A->B e B->A acontecem ao mesmo tempo.
        Conta origem;
        Conta destino;
        if (numeroOrigem.compareTo(numeroDestino) < 0) {
            origem = travarConta(numeroOrigem);
            destino = travarConta(numeroDestino);
        } else {
            destino = travarConta(numeroDestino);
            origem = travarConta(numeroOrigem);
        }

        if (!origem.temSaldoSuficiente(valor)) {
            throw new SaldoInsuficienteException();
        }
        origem.debitar(valor);
        destino.creditar(valor);

        registrar(destino, origem.getNumero(), TipoTransacao.TRANSFERENCIA_RECEBIDA, valor, null);
        return registrar(origem, destino.getNumero(), TipoTransacao.TRANSFERENCIA_ENVIADA, valor, idempotencyKey);
    }

    @Transactional(readOnly = true)
    public Page<Transacao> extrato(String numeroConta, Pageable pageable) {
        return transacaoRepository.findByContaNumeroOrderByCriadoEmDesc(numeroConta, pageable);
    }

    // ---- auxiliares ----

    private Conta travarConta(String numero) {
        Conta conta = contaRepository.findByNumeroParaAtualizar(numero)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Conta " + numero + " nao encontrada."));
        if (!conta.isAtiva()) {
            throw new RegraNegocioException("A conta " + numero + " esta inativa.");
        }
        return conta;
    }

    private Transacao registrar(Conta conta, String contraparte, TipoTransacao tipo,
                                BigDecimal valor, String idempotencyKey) {
        Transacao t = new Transacao(conta, contraparte, tipo, valor, conta.getSaldo(), idempotencyKey);
        return transacaoRepository.save(t);
    }

    private Transacao buscarPorIdempotency(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return null;
        }
        return transacaoRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
    }

    private void validarValorPositivo(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("O valor deve ser maior que zero.");
        }
    }
}
