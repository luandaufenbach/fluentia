package br.com.agenteingles.tema;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.agenteingles.ContaDeTeste;
import br.com.agenteingles.LimpezaDoBancoDeTeste;
import br.com.agenteingles.agente.simulado.GeradorDeDesafioSimulado;
import br.com.agenteingles.desafio.ResumoDoDesafio;
import br.com.agenteingles.desafio.ServicoDeDesafio;
import br.com.agenteingles.usuario.ObjetivoDoUsuario;
import br.com.agenteingles.usuario.ServicoDeUsuario;
import br.com.agenteingles.usuario.Usuario;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/** Os temas das cenas e o giro entre eles. */
@SpringBootTest
@Transactional
class TemasDasCenasIT {

    @Autowired
    private TemaRepositorio temaRepositorio;

    @Autowired
    private ServicoDeDesafio servicoDeDesafio;

    @Autowired
    private ServicoDeUsuario servicoDeUsuario;

    @Autowired
    private LimpezaDoBancoDeTeste limpeza;

    @Autowired
    private ContaDeTeste conta;

    private Usuario usuario;

    @BeforeEach
    void prepararLinhaDeBase() {
        limpeza.limparHistoricoENotas();
        conta.autenticar();
        usuario = servicoDeUsuario.usuarioAtual();
    }

    @AfterEach
    void encerrarSessao() {
        conta.limparContexto();
    }

    @Test
    @DisplayName("ingles para dev nao existe mais como tema")
    void inglesParaDevSaiu() {
        assertThat(temaRepositorio.buscarPorCodigo("ingles_para_dev")).isEmpty();
    }

    @Test
    @DisplayName("as cenas cobrem o dia a dia, nao um nicho profissional")
    void cenasCobremODiaADia() {
        List<String> codigos = temaRepositorio.listarOrdenadosPorNome().stream()
                .map(Tema::getCodigo)
                .toList();

        assertThat(codigos).contains(
                "conversacao_livre", "viagem", "trabalho", "cultura_e_expressoes",
                "comida_e_restaurante", "compras_e_servicos", "saude_e_bem_estar",
                "vida_social", "casa_e_rotina");
    }

    @Test
    @DisplayName("todo tema cadastrado tem cenas proprias no gerador simulado")
    void todoTemaTemCenas() {
        // A chave do mapa de cenas e o nome do tema. Um acento fora do lugar faz o
        // tema cair na cena generica sem erro nenhum — some a cor da cena, em silencio.
        assertThat(temaRepositorio.listarOrdenadosPorNome())
                .allSatisfy(tema -> assertThat(GeradorDeDesafioSimulado.temCenasProprias(tema.getNome()))
                        .as("cenas do tema %s", tema.getNome())
                        .isTrue());
    }

    @Test
    @DisplayName("insistir no mesmo conceito nao prende o aluno em duas cenas")
    void insistirNoConceitoNaoPrendeEmDuasCenas() {
        // Antes a alternativa ao tema preferido era sempre a primeira da lista por nome:
        // com nove temas, sete nunca apareceriam e o aluno via o mesmo par se revezando
        // até o fim. A cena é escolhida por lote, não por desafio, então são precisas
        // várias rodadas para o giro aparecer — que é justamente como o aluno o vive.
        usuario.setObjetivo(ObjetivoDoUsuario.CONVERSACAO_GERAL);
        Set<String> cenasVistas = new LinkedHashSet<>();

        for (int i = 0; i < 20; i++) {
            ResumoDoDesafio desafio = servicoDeDesafio.proximoDesafio(usuario, "verbo_to_be");
            cenasVistas.add(desafio.temaNome());
            servicoDeDesafio.responder(usuario, desafio.id(), "I am Brazilian.");
        }

        assertThat(cenasVistas)
                .as("cenas diferentes em vinte praticas do mesmo conceito")
                .hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("o tema do objetivo continua sendo o mais frequente")
    void temaDoObjetivoDomina() {
        // O giro dá variedade, mas quem escolheu viagem não pode receber cena de
        // farmácia na maior parte das vezes: o objetivo é uma escolha do aluno.
        usuario.setObjetivo(ObjetivoDoUsuario.VIAGEM);

        long emViagem = 0;
        for (int i = 0; i < 12; i++) {
            ResumoDoDesafio desafio = servicoDeDesafio.proximoDesafio(usuario, "verbo_to_be");
            if ("Viagem".equals(desafio.temaNome())) {
                emViagem++;
            }
            servicoDeDesafio.responder(usuario, desafio.id(), "I am Brazilian.");
        }

        assertThat(emViagem).as("desafios na cena de viagem").isGreaterThanOrEqualTo(6);
    }
}
