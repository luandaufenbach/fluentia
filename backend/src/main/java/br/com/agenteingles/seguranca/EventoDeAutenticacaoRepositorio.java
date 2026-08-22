package br.com.agenteingles.seguranca;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventoDeAutenticacaoRepositorio extends JpaRepository<EventoDeAutenticacao, Long> {

    /** Ultimos eventos de um e-mail — a leitura que uma investigacao comeca fazendo. */
    @Query("""
            select e from EventoDeAutenticacao e
            where lower(e.email) = lower(:email)
            order by e.ocorridoEm desc
            """)
    List<EventoDeAutenticacao> listarPorEmail(@Param("email") String email, Limit limite);

    /**
     * Quantas recusas partiram de uma origem numa janela de tempo.
     *
     * <p>O bloqueio por conta nao cobre o ataque que espalha poucas tentativas por
     * muitas contas a partir do mesmo lugar; esta contagem cobre.
     */
    @Query("""
            select count(e) from EventoDeAutenticacao e
            where e.origem = :origem
              and e.tipo = br.com.agenteingles.seguranca.TipoDeEventoDeAutenticacao.LOGIN_RECUSADO
              and e.ocorridoEm > :desde
            """)
    long contarRecusasDaOrigem(@Param("origem") String origem, @Param("desde") LocalDateTime desde);
}
