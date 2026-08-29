package br.com.agenteingles.seguranca;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecuperacaoDeSenhaRepositorio extends JpaRepository<RecuperacaoDeSenha, Long> {

    /** O unico caminho de leitura: pelo hash, nunca pelo token em claro. */
    Optional<RecuperacaoDeSenha> findByTokenHash(String tokenHash);

    /**
     * Queima os pedidos anteriores da conta.
     *
     * <p>Pedir um link novo precisa invalidar os antigos. Sem isto, cada pedido deixaria
     * mais um link valido circulando por e-mail, e quem pedisse cinco vezes teria cinco
     * chaves da propria conta espalhadas na caixa de entrada — a janela de risco cresceria
     * a cada tentativa de quem so nao estava conseguindo entrar.
     */
    @Modifying
    @Query("""
            update RecuperacaoDeSenha r set r.usadoEm = :agora
            where r.usuarioId = :usuarioId and r.usadoEm is null
            """)
    int invalidarPendentesDoUsuario(@Param("usuarioId") Long usuarioId,
                                    @Param("agora") LocalDateTime agora);

    /** Pedidos vindos de uma origem numa janela — alimenta o limite por endereco. */
    @Query("""
            select count(r) from RecuperacaoDeSenha r
            where r.origem = :origem and r.criadoEm > :desde
            """)
    long contarPedidosDaOrigem(@Param("origem") String origem, @Param("desde") LocalDateTime desde);
}
