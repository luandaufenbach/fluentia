package br.com.agenteingles.desafio;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.agenteingles.ContaDeTeste;
import br.com.agenteingles.LimpezaDoBancoDeTeste;
import br.com.agenteingles.agente.PropriedadesDoAgente;
import br.com.agenteingles.usuario.ServicoDeUsuario;
import br.com.agenteingles.usuario.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tamanho do lote conforme o aluno ja praticou ou nao o modulo.
 *
 * <p>O lote existe para dividir o custo fixo do pedido, mas so se paga se o aluno voltar
 * aquele conceito. Na primeira visita ele e menor de proposito.
 */
@SpringBootTest
@Transactional
class TamanhoDoLoteIT {

    @Autowired
    private ServicoDeDesafio servicoDeDesafio;

    @Autowired
    private ServicoDeUsuario servicoDeUsuario;

    @Autowired
    private DesafioRepositorio desafioRepositorio;

    @Autowired
    private PropriedadesDoAgente propriedades;

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
    @DisplayName("modulo nunca praticado gera o lote inicial, menor")
    void primeiraVisitaGeraLoteMenor() {
        servicoDeDesafio.proximoDesafio(usuario);

        assertThat(desafiosDoModulo("verbo_to_be"))
                .isEqualTo(propriedades.desafiosPorLoteInicial());
    }

    @Test
    @DisplayName("depois da primeira resposta o lote passa a ser o cheio")
    void depoisDePraticarGeraLoteCheio() {
        // Consome o lote inicial inteiro: cada resposta esvazia um item da fila.
        for (int i = 0; i < propriedades.desafiosPorLoteInicial(); i++) {
            ResumoDoDesafio desafio = servicoDeDesafio.proximoDesafio(usuario, "verbo_to_be");
            servicoDeDesafio.responder(usuario, desafio.id(), "I am Brazilian.");
        }

        long antesDoProximoLote = desafiosDoModulo("verbo_to_be");
        servicoDeDesafio.proximoDesafio(usuario, "verbo_to_be");

        assertThat(desafiosDoModulo("verbo_to_be") - antesDoProximoLote)
                .isEqualTo(propriedades.desafiosPorLote());
    }

    @Test
    @DisplayName("o lote inicial e menor que o cheio, senao a mudanca nao economiza nada")
    void loteInicialEMenorQueOCheio() {
        assertThat(propriedades.desafiosPorLoteInicial())
                .isLessThan(propriedades.desafiosPorLote());
    }

    private long desafiosDoModulo(String codigo) {
        return desafioRepositorio.findAll().stream()
                .filter(desafio -> desafio.getUsuario().getId().equals(usuario.getId()))
                .filter(desafio -> desafio.getModulo().getCodigo().equals(codigo))
                .count();
    }
}
