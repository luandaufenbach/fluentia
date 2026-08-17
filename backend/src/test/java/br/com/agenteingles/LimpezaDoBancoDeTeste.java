package br.com.agenteingles;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Devolve o banco de testes a linha de base semeada pelas migrations.
 *
 * <p>Testes que passam pela camada HTTP gravam de verdade (o commit acontece dentro do
 * servico), entao sem esta limpeza um teste deixaria nota e historico para o proximo e a
 * ordem de execucao passaria a importar. Modulo, tema e usuario ficam intactos: sao o
 * conteudo do curriculo, nao dado de sessao.
 */
@Component
public class LimpezaDoBancoDeTeste {

    private final JdbcTemplate jdbcTemplate;

    public LimpezaDoBancoDeTeste(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void limparHistoricoENotas() {
        jdbcTemplate.execute("TRUNCATE TABLE erro_detectado, avaliacao_do_desafio, desafio, nota_do_modulo "
                + "RESTART IDENTITY CASCADE");
    }
}
