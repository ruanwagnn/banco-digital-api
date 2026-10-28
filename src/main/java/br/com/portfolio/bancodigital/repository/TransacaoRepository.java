package br.com.portfolio.bancodigital.repository;

import br.com.portfolio.bancodigital.domain.Transacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    Page<Transacao> findByContaNumeroOrderByCriadoEmDesc(String numero, Pageable pageable);
    Optional<Transacao> findByIdempotencyKey(String idempotencyKey);
}
