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
     * Quantos eventos de um tipo partiram de uma origem numa janela de tempo.
     *
     * <p>Sustenta os dois limites por origem, que cobrem o que o limite por conta nao
     * alcanca:
     *
     * <ul>
     *   <li>{@code LOGIN_RECUSADO} — quem espalha poucas tentativas por muitas contas
     *       nunca estoura o contador de nenhuma delas.</li>
     *   <li>{@code CADASTRO} — criar conta nao falha nenhuma vez, entao nao existe
     *       contador de falha para estourar. O que precisa ser limitado aqui e o
     *       proprio sucesso repetido.</li>
     * </ul>
     *
     * <p>Origem nula nao conta: {@code = null} nao casa com nada em SQL, e a contagem
     * voltaria zero de qualquer forma. Quem chama trata esse caso antes.
     */
    @Query("""
            select count(e) from EventoDeAutenticacao e
            where e.origem = :origem
              and e.tipo = :tipo
              and e.ocorridoEm > :desde
            """)
    long contarEventosDaOrigem(@Param("origem") String origem,
                               @Param("tipo") TipoDeEventoDeAutenticacao tipo,
                               @Param("desde") LocalDateTime desde);
}
