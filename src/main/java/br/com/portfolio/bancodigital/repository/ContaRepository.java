package br.com.portfolio.bancodigital.repository;

import br.com.portfolio.bancodigital.domain.Conta;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    @Query("select c from Conta c join fetch c.cliente where c.numero = :numero")
    Optional<Conta> findByNumero(@Param("numero") String numero);

    @Query("select c from Conta c join fetch c.cliente cl where cl.cpf = :cpf")
    Optional<Conta> findByClienteCpf(@Param("cpf") String cpf);

    boolean existsByNumero(String numero);

    /**
     * Busca a conta com bloqueio pessimista de escrita. Usado nas operacoes de
     * dinheiro: enquanto a transacao esta aberta, nenhuma outra transacao consegue
     * ler/alterar a mesma linha, o que impede saldo errado em acesso concorrente.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Conta c where c.numero = :numero")
    Optional<Conta> findByNumeroParaAtualizar(@Param("numero") String numero);
}
