package br.com.agenteingles.custo;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.agenteingles.ContaDeTeste;
import br.com.agenteingles.LimpezaDoBancoDeTeste;
import br.com.agenteingles.usuario.ServicoDeUsuario;
import br.com.agenteingles.usuario.Usuario;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * O medidor de custo contra o banco de verdade.
 *
 * <p>Sem {@code @Transactional} de proposito: o registro de consumo roda em transacao
 * propria, e uma transacao de teste em volta esconderia justamente o que precisa ser
 * verificado.
 */
@SpringBootTest
class MedidorDeCustoIT {

    @Autowired
    private RegistroDeConsumo registro;

    @Autowired
    private ServicoDeConsumo servicoDeConsumo;

    @Autowired
    private ConsumoDeApiRepositorio consumoRepositorio;

    @Autowired
    private ServicoDeUsuario servicoDeUsuario;

    @Autowired
    private LimpezaDoBancoDeTeste limpeza;

    @Autowired
    private ContaDeTeste conta;

    @Autowired
    private TransactionTemplate transacoes;

    private Usuario usuario;

    @BeforeEach
    void prepararLinhaDeBase() {
        limpeza.limparHistoricoENotas();
        conta.autenticar();
        usuario = servicoDeUsuario.usuarioAtual();
    }

    @AfterEach
    void encerrarSessao() {
        limpeza.limparHistoricoENotas();
        conta.limparContexto();
    }

    @Test
    @DisplayName("calcula o custo pela tabela do modelo configurado")
    void calculaOCustoDaChamada() {
        registro.registrar(usuario.getId(), TipoDeChamada.GERACAO_DE_DESAFIO,
                "claude-sonnet-5", 1_000, 500, 5);

        ResumoDeConsumo resumo = servicoDeConsumo.doUsuario(usuario);

        assertThat(resumo.total().chamadas()).isEqualTo(1);
        assertThat(resumo.total().tokensDeEntrada()).isEqualTo(1_000);
        assertThat(resumo.total().tokensDeSaida()).isEqualTo(500);
        assertThat(resumo.total().custoUsd()).isEqualByComparingTo("0.010500");
    }

    @Test
    @DisplayName("o nome com data no fim encontra o preco da familia")
    void nomeComDataEncontraOPreco() {
        // A API responde com o nome datado mesmo quando pedimos o nome curto:
        // pedimos claude-sonnet-5 e volta claude-sonnet-5-20250929. A busca exata
        // falhava nesses casos e o custo ficava em aberto — sem quebrar nada, so
        // deixando o relatorio de gasto furado, que e a pior forma de falhar.
        registro.registrar(usuario.getId(), TipoDeChamada.AVALIACAO_DE_RESPOSTA,
                "claude-sonnet-5-20250929", 1_000, 500, 1);

        ResumoDeConsumo resumo = servicoDeConsumo.doUsuario(usuario);

        assertThat(resumo.total().custoUsd()).isEqualByComparingTo("0.010500");
        assertThat(resumo.modelosSemPreco()).isEmpty();
    }

    @Test
    @DisplayName("modelo sem preco configurado grava os tokens e deixa o custo em aberto")
    void modeloSemPrecoNaoViraZero() {
        registro.registrar(usuario.getId(), TipoDeChamada.AVALIACAO_DE_RESPOSTA,
                "modelo-que-ninguem-configurou", 800, 400, 1);

        ResumoDeConsumo resumo = servicoDeConsumo.doUsuario(usuario);

        // Os tokens ficam registrados: sem eles nao haveria como refazer a conta depois.
        assertThat(resumo.total().tokensDeEntrada()).isEqualTo(800);
        // E o modelo aparece na lista, para o total nao ser lido como se estivesse completo.
        assertThat(resumo.modelosSemPreco()).containsExactly("modelo-que-ninguem-configurou");
    }

    @Test
    @DisplayName("o consumo de uma conta nao aparece no extrato da outra")
    void consumoNaoVazaEntreContas() {
        Usuario outro = conta.garantirQueOOutroExiste();

        registro.registrar(usuario.getId(), TipoDeChamada.GERACAO_DE_DESAFIO,
                "claude-sonnet-5", 1_000, 500, 5);

        ResumoDeConsumo doOutro = servicoDeConsumo.doUsuario(outro);

        assertThat(doOutro.total().chamadas()).isZero();
        assertThat(doOutro.total().custoUsd()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("o gasto sobrevive ao rollback de quem chamou")
    void gastoSobreviveAoRollback() {
        long antes = consumoRepositorio.count();

        // A chamada a API ja aconteceu e ja foi cobrada. Se o trabalho que veio depois
        // falhar e desfizer a transacao, o dinheiro continua tendo saido: por isso o
        // registro roda em transacao propria, e nao pode voltar junto.
        //
        // A transacao de fora vem do TransactionTemplate, e nao de um metodo anotado
        // deste mesmo teste: anotacao em chamada interna nao cria transacao nenhuma, e o
        // teste passaria sem nunca ter havido rollback para sobreviver.
        try {
            transacoes.executeWithoutResult(status -> {
                registro.registrar(usuario.getId(), TipoDeChamada.AVALIACAO_DE_RESPOSTA,
                        "claude-sonnet-5", 500, 300, 1);
                throw new IllegalStateException("falha depois da chamada ja paga");
            });
        } catch (IllegalStateException esperado) {
            // O rollback e o ponto do teste.
        }

        assertThat(consumoRepositorio.count()).isEqualTo(antes + 1);
    }

    @Test
    @DisplayName("separa o gasto por tipo de chamada")
    void separaPorTipo() {
        registro.registrar(usuario.getId(), TipoDeChamada.GERACAO_DE_DESAFIO,
                "claude-sonnet-5", 200, 800, 5);
        registro.registrar(usuario.getId(), TipoDeChamada.AVALIACAO_DE_RESPOSTA,
                "claude-sonnet-5", 600, 400, 1);

        List<ConsumoPorTipo> porTipo = servicoDeConsumo.doUsuario(usuario).porTipo();

        assertThat(porTipo).hasSize(2);
        assertThat(porTipo).extracting(ConsumoPorTipo::tipo)
                .containsExactlyInAnyOrder(
                        TipoDeChamada.GERACAO_DE_DESAFIO, TipoDeChamada.AVALIACAO_DE_RESPOSTA);
    }
}
