package br.com.portfolio.bancodigital.security;

import br.com.portfolio.bancodigital.domain.Cliente;
import br.com.portfolio.bancodigital.repository.ClienteRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteUserDetailsService implements UserDetailsService {

    private final ClienteRepository clienteRepository;

    public ClienteUserDetailsService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String cpf) throws UsernameNotFoundException {
        Cliente cliente = clienteRepository.findByCpf(cpf)
                .orElseThrow(() -> new UsernameNotFoundException("Cliente nao encontrado: " + cpf));
        return new User(
                cliente.getCpf(),
                cliente.getSenhaHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + cliente.getRole().name())));
    }
}
