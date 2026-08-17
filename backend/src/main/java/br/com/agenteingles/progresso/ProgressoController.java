package br.com.agenteingles.progresso;

import br.com.agenteingles.modulo.ModuloController;
import br.com.agenteingles.modulo.ServicoDeModulo;
import br.com.agenteingles.modulo.SituacaoDoModulo;
import br.com.agenteingles.nota.FaixaDeNota;
import br.com.agenteingles.orquestrador.DecisaoDoOrquestrador;
import br.com.agenteingles.orquestrador.Orquestrador;
import br.com.agenteingles.usuario.ServicoDeUsuario;
import br.com.agenteingles.usuario.Usuario;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Visao geral do progresso e sugestao do proximo passo — e o dashboard do onboarding:
 * quantos modulos estao em cada faixa e quais conceitos precisam de atencao agora.
 */
@RestController
@RequestMapping("/api")
public class ProgressoController {

    /** Quantos conceitos fracos aparecem na lista de atencao. */
    private static final int MODULOS_QUE_PRECISAM_DE_ATENCAO = 5;

    private final ServicoDeModulo servicoDeModulo;
    private final Orquestrador orquestrador;
    private final ServicoDeUsuario servicoDeUsuario;

    public ProgressoController(ServicoDeModulo servicoDeModulo,
                               Orquestrador orquestrador,
                               ServicoDeUsuario servicoDeUsuario) {
        this.servicoDeModulo = servicoDeModulo;
        this.orquestrador = orquestrador;
        this.servicoDeUsuario = servicoDeUsuario;
    }

    public record ProgressoResposta(
            Map<FaixaDeNota, Long> quantidadePorFaixa,
            int totalDeModulos,
            int modulosLiberados,
            List<ModuloController.ModuloNaListaResposta> precisamDeAtencao) {
    }

    public record SugestaoResposta(
            String moduloCodigo,
            String moduloNome,
            String temaNome,
            String motivo) {
    }

    @GetMapping("/progresso")
    public ProgressoResposta progresso() {
        Usuario usuario = servicoDeUsuario.usuarioAtual();
        List<SituacaoDoModulo> situacoes = servicoDeModulo.situacaoDeTodosOsModulos(usuario);

        Map<FaixaDeNota, Long> quantidadePorFaixa = new LinkedHashMap<>();
        for (FaixaDeNota faixa : FaixaDeNota.values()) {
            quantidadePorFaixa.put(faixa, 0L);
        }
        for (SituacaoDoModulo situacao : situacoes) {
            quantidadePorFaixa.merge(situacao.faixa(), 1L, Long::sum);
        }

        // Atencao vai para os liberados ja praticados com a menor nota: sao os que
        // travam a progressao e os que o esquecimento derrubou.
        List<ModuloController.ModuloNaListaResposta> precisamDeAtencao = situacoes.stream()
                .filter(SituacaoDoModulo::liberado)
                .filter(situacao -> !situacao.nuncaPraticado())
                .filter(situacao -> situacao.faixa() != FaixaDeNota.VERDE)
                .sorted(Comparator.comparing(SituacaoDoModulo::nota))
                .limit(MODULOS_QUE_PRECISAM_DE_ATENCAO)
                .map(ModuloController::converter)
                .toList();

        long liberados = situacoes.stream().filter(SituacaoDoModulo::liberado).count();

        return new ProgressoResposta(
                quantidadePorFaixa, situacoes.size(), (int) liberados, precisamDeAtencao);
    }

    /** O que o orquestrador faria agora, sem gerar o desafio ainda. */
    @GetMapping("/dashboard/sugestao")
    public SugestaoResposta sugestao() {
        Usuario usuario = servicoDeUsuario.usuarioAtual();
        DecisaoDoOrquestrador decisao = orquestrador.decidirProximaPratica(usuario);
        return new SugestaoResposta(
                decisao.situacaoDoModulo().modulo().getCodigo(),
                decisao.situacaoDoModulo().modulo().getNome(),
                decisao.tema().getNome(),
                decisao.motivo());
    }
}
