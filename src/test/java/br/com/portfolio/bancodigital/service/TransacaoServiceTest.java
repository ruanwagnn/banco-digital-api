package br.com.portfolio.bancodigital.service;

import br.com.portfolio.bancodigital.domain.Cliente;
import br.com.portfolio.bancodigital.domain.Conta;
import br.com.portfolio.bancodigital.domain.Role;
import br.com.portfolio.bancodigital.domain.TipoTransacao;
import br.com.portfolio.bancodigital.domain.Transacao;
import br.com.portfolio.bancodigital.exception.RegraNegocioException;
import br.com.portfolio.bancodigital.exception.SaldoInsuficienteException;
import br.com.portfolio.bancodigital.repository.ContaRepository;
import br.com.portfolio.bancodigital.repository.TransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransacaoService")
class TransacaoServiceTest {

    @Mock
    ContaRepository contaRepository;
    @Mock
    TransacaoRepository transacaoRepository;
    @InjectMocks
    TransacaoService service;

    Cliente cliente;

    @BeforeEach
    void setup() {
        cliente = new Cliente("Joao", "11122233344", "joao@email.com", "hash", Role.CLIENTE);
    }

    private Conta contaComSaldo(String numero, String saldo) {
        Conta conta = new Conta(numero, "0001", cliente);
        conta.creditar(new BigDecimal(saldo));
        return conta;
    }

    @Test
    @DisplayName("deposito aumenta o saldo e registra a transacao")
    void depositoAumentaSaldo() {
        Conta conta = contaComSaldo("00000001", "100.00");
        when(contaRepository.findByNumeroParaAtualizar("00000001")).thenReturn(Optional.of(conta));
        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(inv -> inv.getArgument(0));

        service.depositar("00000001", new BigDecimal("50.00"), null);

        assertThat(conta.getSaldo()).isEqualByComparingTo("150.00");
        verify(transacaoRepository).save(argThat(t -> t.getTipo() == TipoTransacao.DEPOSITO));
    }

    @Test
    @DisplayName("saque com saldo suficiente diminui o saldo")
    void saqueComSaldo() {
        Conta conta = contaComSaldo("00000001", "100.00");
        when(contaRepository.findByNumeroParaAtualizar("00000001")).thenReturn(Optional.of(conta));
        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(inv -> inv.getArgument(0));

        service.sacar("00000001", new BigDecimal("30.00"), null);

        assertThat(conta.getSaldo()).isEqualByComparingTo("70.00");
    }

    @Test
    @DisplayName("saque sem saldo lanca SaldoInsuficienteException e nao altera o saldo")
    void saqueSemSaldo() {
        Conta conta = contaComSaldo("00000001", "20.00");
        when(contaRepository.findByNumeroParaAtualizar("00000001")).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> service.sacar("00000001", new BigDecimal("100.00"), null))
                .isInstanceOf(SaldoInsuficienteException.class);

        assertThat(conta.getSaldo()).isEqualByComparingTo("20.00");
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("deposito com valor zero ou negativo e rejeitado")
    void depositoValorInvalido() {
        assertThatThrownBy(() -> service.depositar("00000001", new BigDecimal("0.00"), null))
                .isInstanceOf(RegraNegocioException.class);
        verifyNoInteractions(transacaoRepository);
    }

    @Test
    @DisplayName("transferencia move o valor da origem para o destino")
    void transferenciaMoveSaldo() {
        Conta origem = contaComSaldo("00000001", "200.00");
        Conta destino = contaComSaldo("00000002", "50.00");
        when(contaRepository.findByNumeroParaAtualizar("00000001")).thenReturn(Optional.of(origem));
        when(contaRepository.findByNumeroParaAtualizar("00000002")).thenReturn(Optional.of(destino));
        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(inv -> inv.getArgument(0));

        service.transferir("00000001", "00000002", new BigDecimal("75.00"), null);

        assertThat(origem.getSaldo()).isEqualByComparingTo("125.00");
        assertThat(destino.getSaldo()).isEqualByComparingTo("125.00");
        verify(transacaoRepository, times(2)).save(any(Transacao.class));
    }

    @Test
    @DisplayName("transferencia para a propria conta e rejeitada")
    void transferenciaMesmaConta() {
        assertThatThrownBy(() ->
                service.transferir("00000001", "00000001", new BigDecimal("10.00"), null))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("operacao com mesma Idempotency-Key nao e processada duas vezes")
    void idempotenciaNaoDuplica() {
        Conta conta = contaComSaldo("00000001", "100.00");
        Transacao jaFeita = new Transacao(conta, null, TipoTransacao.DEPOSITO,
                new BigDecimal("50.00"), new BigDecimal("150.00"), "chave-123");
        when(transacaoRepository.findByIdempotencyKey("chave-123")).thenReturn(Optional.of(jaFeita));

        Transacao resultado = service.depositar("00000001", new BigDecimal("50.00"), "chave-123");

        assertThat(resultado).isSameAs(jaFeita);
        verify(contaRepository, never()).findByNumeroParaAtualizar(anyString());
        verify(transacaoRepository, never()).save(any());
    }
}
