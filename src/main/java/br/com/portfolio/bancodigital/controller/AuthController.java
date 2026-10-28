package br.com.portfolio.bancodigital.controller;

import br.com.portfolio.bancodigital.domain.Conta;
import br.com.portfolio.bancodigital.dto.*;
import br.com.portfolio.bancodigital.service.AuthService;
import br.com.portfolio.bancodigital.service.ContaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticacao", description = "Registro de cliente e login")
public class AuthController {

    private final AuthService authService;
    private final ContaService contaService;

    public AuthController(AuthService authService, ContaService contaService) {
        this.authService = authService;
        this.contaService = contaService;
    }

    @PostMapping("/registrar")
    @Operation(summary = "Cria um cliente e abre uma conta corrente para ele")
    public ResponseEntity<ContaResponse> registrar(@Valid @RequestBody RegistroRequest req) {
        Conta conta = contaService.registrarClienteComConta(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ContaResponse.de(conta));
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica por CPF e senha e devolve um token JWT")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.autenticar(req));
    }
}
