package br.com.portfolio.bancodigital.controller;

import br.com.portfolio.bancodigital.domain.Conta;
import br.com.portfolio.bancodigital.dto.ContaResponse;
import br.com.portfolio.bancodigital.service.ContaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contas")
@Tag(name = "Contas", description = "Consulta de conta e saldo")
public class ContaController {

    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @GetMapping("/me")
    @Operation(summary = "Dados e saldo da conta do cliente autenticado")
    public ResponseEntity<ContaResponse> minhaConta(@AuthenticationPrincipal UserDetails user) {
        Conta conta = contaService.buscarPorCpf(user.getUsername());
        return ResponseEntity.ok(ContaResponse.de(conta));
    }
}
