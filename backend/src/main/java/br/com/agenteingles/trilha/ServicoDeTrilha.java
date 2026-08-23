package br.com.agenteingles.trilha;

import br.com.agenteingles.comum.RegraDeNegocioException;
import br.com.agenteingles.modulo.ModuloController;
import br.com.agenteingles.modulo.ServicoDeModulo;
import br.com.agenteingles.modulo.SituacaoDoModulo;
import br.com.agenteingles.orquestrador.Orquestrador;
import br.com.agenteingles.trilha.TrilhaController.FaseNaTrilhaResposta;
import br.com.agenteingles.trilha.TrilhaController.TrilhaResposta;
import br.com.agenteingles.usuario.Usuario;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Monta a trilha agrupando a situacao de cada modulo na fase a que ele pertence. */
@Service
public class ServicoDeTrilha {

    /**
     * Um modulo conta como consolidado para a fase quando sai do vermelho. E o mesmo
     * limite que ja libera o modulo seguinte: seria incoerente destravar o proximo
     * conceito e ainda assim dizer que este nao foi vencido.
     */
    private static final BigDecimal NOTA_QUE_CONSOLIDA = ServicoDeModulo.NOTA_QUE_LIBERA_O_PROXIMO;

    private final ServicoDeModulo servicoDeModulo;
    private final Orquestrador orquestrador;

    public ServicoDeTrilha(ServicoDeModulo servicoDeModulo, Orquestrador orquestrador) {
        this.servicoDeModulo = servicoDeModulo;
        this.orquestrador = orquestrador;
    }

    @Transactional(readOnly = true)
    public TrilhaResposta montar(Usuario usuario) {
        List<SituacaoDoModulo> situacoes = servicoDeModulo.situacaoDeTodosOsModulos(usuario);
        String faseDoProximoPasso = faseDoProximoPasso(usuario);

        Map<Fase, List<SituacaoDoModulo>> porFase = new LinkedHashMap<>();
        situacoes.stream()
                .sorted(Comparator.comparing(situacao -> situacao.modulo().getFase().getOrdem()))
                .forEach(situacao -> porFase
                        .computeIfAbsent(situacao.modulo().getFase(), fase -> new ArrayList<>())
                        .add(situacao));

        List<FaseNaTrilhaResposta> fases = porFase.entrySet().stream()
                .map(entrada -> montarFase(entrada.getKey(), entrada.getValue(), faseDoProximoPasso))
                .toList();

        return new TrilhaResposta(
                fases,
                fases.stream().mapToInt(FaseNaTrilhaResposta::modulosConsolidados).sum(),
                fases.stream().mapToInt(FaseNaTrilhaResposta::modulosPresumidos).sum(),
                situacoes.size());
    }

    private FaseNaTrilhaResposta montarFase(Fase fase,
                                            List<SituacaoDoModulo> situacoes,
                                            String faseDoProximoPasso) {
        // Consolidado exige pratica: nota presumida pelo nivelamento conta em separado.
        int consolidados = (int) situacoes.stream().filter(this::consolidadoPelaPratica).count();
        int presumidos = (int) situacoes.stream().filter(this::consolidadoPorPresuncao).count();

        return new FaseNaTrilhaResposta(
                fase.getCodigo(),
                fase.getNome(),
                fase.getPromessa(),
                fase.getMarco(),
                situarMarco(situacoes.size(), consolidados, presumidos),
                fase.getCodigo().equals(faseDoProximoPasso),
                consolidados,
                presumidos,
                situacoes.size(),
                situacoes.stream().map(ModuloController::converter).toList());
    }

    /**
     * A fase do conceito que o orquestrador escolheria agora.
     *
     * <p>Vem do proprio orquestrador, e nao de uma regra parecida escrita aqui. Enquanto
     * "voce esta aqui" era "fase ja encostada e ainda nao fechada", a tela conseguia
     * mostrar o marcador numa fase e o "proximo passo" em outra — dois indicadores de
     * posicao discordando na mesma tela. Saindo da mesma fonte, eles nao tem como divergir.
     *
     * @return o codigo da fase, ou {@code null} quando nao ha modulo liberado
     */
    private String faseDoProximoPasso(Usuario usuario) {
        try {
            return orquestrador.decidirProximaPratica(usuario)
                    .situacaoDoModulo().modulo().getFase().getCodigo();
        } catch (RegraDeNegocioException nenhumModuloLiberado) {
            // A trilha precisa aparecer mesmo assim: ela e justamente a tela que explica
            // por que nada esta liberado.
            return null;
        }
    }

    private SituacaoDoMarco situarMarco(int total, int consolidados, int presumidos) {
        if (consolidados == total) {
            return SituacaoDoMarco.ALCANCADO;
        }
        if (consolidados + presumidos == total) {
            return SituacaoDoMarco.PRESUMIDO;
        }
        return SituacaoDoMarco.PENDENTE;
    }

    /** Fora do vermelho e provado numa resposta de verdade. */
    private boolean consolidadoPelaPratica(SituacaoDoModulo situacao) {
        return situacao.demonstrado() && foraDoVermelho(situacao);
    }

    /** Fora do vermelho por estimativa do nivelamento, sem nenhuma pratica ainda. */
    private boolean consolidadoPorPresuncao(SituacaoDoModulo situacao) {
        return situacao.presumido() && foraDoVermelho(situacao);
    }

    private boolean foraDoVermelho(SituacaoDoModulo situacao) {
        return situacao.nota().compareTo(NOTA_QUE_CONSOLIDA) >= 0;
    }
}
