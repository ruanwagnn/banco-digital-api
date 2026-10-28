package br.com.portfolio.bancodigital.service;

import br.com.portfolio.bancodigital.dto.LoginRequest;
import br.com.portfolio.bancodigital.dto.TokenResponse;
import br.com.portfolio.bancodigital.domain.Cliente;
import br.com.portfolio.bancodigital.repository.ClienteRepository;
import br.com.portfolio.bancodigital.security.TokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final ClienteRepository clienteRepository;
    private final TokenService tokenService;

    public AuthService(AuthenticationManager authenticationManager,
                       ClienteRepository clienteRepository,
                       TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.clienteRepository = clienteRepository;
        this.tokenService = tokenService;
    }

    public TokenResponse autenticar(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.cpf(), req.senha()));
        Cliente cliente = clienteRepository.findByCpf(req.cpf())
                .orElseThrow(() -> new BadCredentialsException("CPF ou senha invalidos."));
        String token = tokenService.gerarToken(cliente.getCpf(), cliente.getRole().name());
        return TokenResponse.bearer(token, tokenService.getExpiracaoSegundos());
    }
}
