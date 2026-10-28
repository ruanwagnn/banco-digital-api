package br.com.portfolio.bancodigital.controller;

import br.com.portfolio.bancodigital.domain.Conta;
import br.com.portfolio.bancodigital.domain.Transacao;
import br.com.portfolio.bancodigital.dto.*;
import br.com.portfolio.bancodigital.service.ContaService;
import br.com.portfolio.bancodigital.service.TransacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transacoes")
@Tag(name = "Transacoes", description = "Deposito, saque, transferencia e extrato")
public class TransacaoController {

    private final TransacaoService transacaoService;
    private final ContaService contaService;

    public TransacaoController(TransacaoService transacaoService, ContaService contaService) {
        this.transacaoService = transacaoService;
        this.contaService = contaService;
    }

    @PostMapping("/deposito")
    @Operation(summary = "Deposita um valor na conta do cliente autenticado")
    public ResponseEntity<TransacaoResponse> depositar(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody OperacaoValorRequest req,
            @Parameter(description = "Chave de idempotencia opcional para evitar duplicidade")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        Conta conta = contaService.buscarPorCpf(user.getUsername());
        Transacao t = transacaoService.depositar(conta.getNumero(), req.valor(), idempotencyKey);
        return ResponseEntity.ok(TransacaoResponse.de(t));
    }

    @PostMapping("/saque")
    @Operation(summary = "Saca um valor da conta do cliente autenticado")
    public ResponseEntity<TransacaoResponse> sacar(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody OperacaoValorRequest req,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        Conta conta = contaService.buscarPorCpf(user.getUsername());
        Transacao t = transacaoService.sacar(conta.getNumero(), req.valor(), idempotencyKey);
        return ResponseEntity.ok(TransacaoResponse.de(t));
    }

    @PostMapping("/transferencia")
    @Operation(summary = "Transfere um valor para outra conta")
    public ResponseEntity<TransacaoResponse> transferir(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody TransferenciaRequest req,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        Conta origem = contaService.buscarPorCpf(user.getUsername());
        Transacao t = transacaoService.transferir(
                origem.getNumero(), req.contaDestino(), req.valor(), idempotencyKey);
        return ResponseEntity.ok(TransacaoResponse.de(t));
    }

    @GetMapping("/extrato")
    @Operation(summary = "Lista as transacoes da conta do cliente autenticado (paginado)")
    public ResponseEntity<Page<TransacaoResponse>> extrato(
            @AuthenticationPrincipal UserDetails user,
            Pageable pageable) {
        Conta conta = contaService.buscarPorCpf(user.getUsername());
        Page<TransacaoResponse> page = transacaoService.extrato(conta.getNumero(), pageable)
                .map(TransacaoResponse::de);
        return ResponseEntity.ok(page);
    }
}
