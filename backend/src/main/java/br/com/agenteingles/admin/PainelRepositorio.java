package br.com.agenteingles.admin;

import br.com.agenteingles.desafio.Desafio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PainelRepositorio extends JpaRepository<Desafio, Long> {

    /**
     * Desafios respondidos por conta.
     *
     * <p>Conta avaliacoes, e nao desafios: desafio gerado e custo, desafio respondido e
     * uso. Um aluno com trinta desafios na fila e duas respostas praticou duas vezes, e
     * a linha do painel precisa dizer isso, nao o contrario.
     */
    @Query("""
            select new br.com.agenteingles.admin.RespondidosDoUsuario(d.usuario.id, count(a))
            from AvaliacaoDoDesafio a join a.desafio d
            group by d.usuario.id
            """)
    List<RespondidosDoUsuario> contarRespondidosPorUsuario();
}
