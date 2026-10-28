package br.com.portfolio.bancodigital.service;

import br.com.portfolio.bancodigital.domain.Cliente;
import br.com.portfolio.bancodigital.domain.Conta;
import br.com.portfolio.bancodigital.domain.Role;
import br.com.portfolio.bancodigital.dto.RegistroRequest;
import br.com.portfolio.bancodigital.exception.RecursoNaoEncontradoException;
import br.com.portfolio.bancodigital.exception.RegraNegocioException;
import br.com.portfolio.bancodigital.repository.ClienteRepository;
import br.com.portfolio.bancodigital.repository.ContaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
public class ContaService {

    private static final String AGENCIA_PADRAO = "0001";
    private final SecureRandom random = new SecureRandom();

    private final ClienteRepository clienteRepository;
    private final ContaRepository contaRepository;
    private final PasswordEncoder passwordEncoder;

    public ContaService(ClienteRepository clienteRepository,
                        ContaRepository contaRepository,
                        PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.contaRepository = contaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Conta registrarClienteComConta(RegistroRequest req) {
        if (clienteRepository.existsByCpf(req.cpf())) {
            throw new RegraNegocioException("Ja existe um cliente com este CPF.");
        }
        if (clienteRepository.existsByEmail(req.email())) {
            throw new RegraNegocioException("Ja existe um cliente com este e-mail.");
        }
        Cliente cliente = new Cliente(
                req.nome(), req.cpf(), req.email(),
                passwordEncoder.encode(req.senha()), Role.CLIENTE);
        cliente = clienteRepository.save(cliente);

        Conta conta = new Conta(gerarNumeroUnico(), AGENCIA_PADRAO, cliente);
        return contaRepository.save(conta);
    }

    @Transactional(readOnly = true)
    public Conta buscarPorCpf(String cpf) {
        return contaRepository.findByClienteCpf(cpf)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Conta nao encontrada para o CPF informado."));
    }

    @Transactional(readOnly = true)
    public Conta buscarPorNumero(String numero) {
        return contaRepository.findByNumero(numero)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Conta " + numero + " nao encontrada."));
    }

    private String gerarNumeroUnico() {
        String numero;
        do {
            numero = String.format("%08d", random.nextInt(100_000_000));
        } while (contaRepository.existsByNumero(numero));
        return numero;
    }
}
