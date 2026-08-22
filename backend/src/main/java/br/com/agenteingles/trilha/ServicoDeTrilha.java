package br.com.agenteingles.trilha;

import br.com.agenteingles.modulo.ModuloController;
import br.com.agenteingles.modulo.ServicoDeModulo;
import br.com.agenteingles.modulo.SituacaoDoModulo;
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

    public ServicoDeTrilha(ServicoDeModulo servicoDeModulo) {
        this.servicoDeModulo = servicoDeModulo;
    }

    @Transactional(readOnly = true)
    public TrilhaResposta montar(Usuario usuario) {
        List<SituacaoDoModulo> situacoes = servicoDeModulo.situacaoDeTodosOsModulos(usuario);

        Map<Fase, List<SituacaoDoModulo>> porFase = new LinkedHashMap<>();
        situacoes.stream()
                .sorted(Comparator.comparing(situacao -> situacao.modulo().getFase().getOrdem()))
                .forEach(situacao -> porFase
                        .computeIfAbsent(situacao.modulo().getFase(), fase -> new ArrayList<>())
                        .add(situacao));

        List<FaseNaTrilhaResposta> fases = porFase.entrySet().stream()
                .map(entrada -> montarFase(entrada.getKey(), entrada.getValue()))
                .toList();

        return new TrilhaResposta(
                fases,
                fases.stream().mapToInt(FaseNaTrilhaResposta::modulosConsolidados).sum(),
                situacoes.size());
    }

    private FaseNaTrilhaResposta montarFase(Fase fase, List<SituacaoDoModulo> situacoes) {
        int consolidados = (int) situacoes.stream().filter(this::consolidado).count();
        boolean marcoAlcancado = consolidados == situacoes.size();

        // "Em andamento" e a fase onde o aluno esta de fato: ja encostou nela e ainda
        // nao fechou. E o que a interface usa para posicionar o "voce esta aqui".
        boolean encostou = situacoes.stream().anyMatch(situacao -> !situacao.nuncaPraticado());

        return new FaseNaTrilhaResposta(
                fase.getCodigo(),
                fase.getNome(),
                fase.getPromessa(),
                fase.getMarco(),
                marcoAlcancado,
                encostou && !marcoAlcancado,
                consolidados,
                situacoes.size(),
                situacoes.stream().map(ModuloController::converter).toList());
    }

    private boolean consolidado(SituacaoDoModulo situacao) {
        return !situacao.nuncaPraticado()
                && situacao.nota().compareTo(NOTA_QUE_CONSOLIDA) >= 0;
    }
}
