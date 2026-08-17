package br.com.agenteingles.orquestrador;

import br.com.agenteingles.comum.RegraDeNegocioException;
import br.com.agenteingles.desafio.Desafio;
import br.com.agenteingles.desafio.DesafioRepositorio;
import br.com.agenteingles.modulo.ServicoDeModulo;
import br.com.agenteingles.modulo.SituacaoDoModulo;
import br.com.agenteingles.tema.Tema;
import br.com.agenteingles.tema.TemaRepositorio;
import br.com.agenteingles.usuario.ObjetivoDoUsuario;
import br.com.agenteingles.usuario.Usuario;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Decide, a cada sessao, qual conceito precisa de reforco e em qual cena cobra-lo.
 *
 * <p>A escolha do modulo segue uma ordem de prioridade explicita — nao e sorteio:
 * <ol>
 *   <li>modulo liberado em vermelho (nota abaixo de 6): reforco urgente, o mais fraco primeiro;</li>
 *   <li>modulo liberado nunca praticado: avanca o curriculo na ordem do nivel;</li>
 *   <li>modulo liberado ainda nao consolidado (abaixo de 9): fecha a lacuna;</li>
 *   <li>tudo consolidado: revisao do que esta ha mais tempo sem pratica, contra o esquecimento.</li>
 * </ol>
 */
@Service
public class Orquestrador {

    /** Acima desta nota o conceito conta como consolidado e sai da fila de reforco. */
    private static final BigDecimal NOTA_DE_CONSOLIDACAO = new BigDecimal("9");

    /** Tema preferido para cada objetivo declarado no onboarding. */
    private static final Map<ObjetivoDoUsuario, String> TEMA_PREFERIDO_POR_OBJETIVO = Map.of(
            ObjetivoDoUsuario.VIAGEM, "viagem",
            ObjetivoDoUsuario.TRABALHO, "trabalho",
            ObjetivoDoUsuario.DEV, "ingles_para_dev",
            ObjetivoDoUsuario.CONVERSACAO_GERAL, "conversacao_livre");

    private static final String TEMA_PADRAO = "conversacao_livre";

    private final ServicoDeModulo servicoDeModulo;
    private final TemaRepositorio temaRepositorio;
    private final DesafioRepositorio desafioRepositorio;

    public Orquestrador(ServicoDeModulo servicoDeModulo,
                        TemaRepositorio temaRepositorio,
                        DesafioRepositorio desafioRepositorio) {
        this.servicoDeModulo = servicoDeModulo;
        this.temaRepositorio = temaRepositorio;
        this.desafioRepositorio = desafioRepositorio;
    }

    @Transactional(readOnly = true)
    public DecisaoDoOrquestrador decidirProximaPratica(Usuario usuario) {
        List<SituacaoDoModulo> liberados = servicoDeModulo.situacaoDeTodosOsModulos(usuario).stream()
                .filter(SituacaoDoModulo::liberado)
                .toList();

        if (liberados.isEmpty()) {
            throw new RegraDeNegocioException(
                    "Nenhum modulo liberado para pratica. Verifique os pre-requisitos do curriculo.");
        }

        SituacaoDoModulo escolhido = escolherModulo(liberados);
        Tema tema = escolherTema(usuario, escolhido);
        return new DecisaoDoOrquestrador(escolhido, tema, montarMotivo(escolhido, tema, usuario));
    }

    private SituacaoDoModulo escolherModulo(List<SituacaoDoModulo> liberados) {
        Comparator<SituacaoDoModulo> porNotaDepoisPorOrdem = Comparator
                .comparing(SituacaoDoModulo::nota)
                .thenComparing(situacao -> situacao.modulo().getOrdem());

        // 1. Reforco urgente: algum conceito liberado esta em vermelho.
        Optional<SituacaoDoModulo> emVermelho = liberados.stream()
                .filter(situacao -> !situacao.nuncaPraticado())
                .filter(situacao -> situacao.nota().compareTo(ServicoDeModulo.NOTA_QUE_LIBERA_O_PROXIMO) < 0)
                .min(porNotaDepoisPorOrdem);
        if (emVermelho.isPresent()) {
            return emVermelho.get();
        }

        // 2. Avanco: o proximo conceito ainda nao praticado, na ordem do curriculo.
        Optional<SituacaoDoModulo> novo = liberados.stream()
                .filter(SituacaoDoModulo::nuncaPraticado)
                .min(Comparator.comparing(situacao -> situacao.modulo().getOrdem()));
        if (novo.isPresent()) {
            return novo.get();
        }

        // 3. Consolidacao: fecha a lacuna do que ainda nao esta verde.
        Optional<SituacaoDoModulo> naoConsolidado = liberados.stream()
                .filter(situacao -> situacao.nota().compareTo(NOTA_DE_CONSOLIDACAO) < 0)
                .min(porNotaDepoisPorOrdem);
        if (naoConsolidado.isPresent()) {
            return naoConsolidado.get();
        }

        // 4. Revisao: tudo consolidado, entao pratica o que esta ha mais tempo parado.
        return liberados.stream()
                .min(Comparator.comparing(SituacaoDoModulo::dataDaUltimaPratica,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .orElseThrow(() -> new RegraDeNegocioException("Nao foi possivel escolher um modulo para praticar."));
    }

    /**
     * Tema preferido do objetivo do usuario, trocando de cena quando o ultimo desafio
     * daquele modulo ja usou esse tema — o conceito se repete, a roupagem nao.
     */
    private Tema escolherTema(Usuario usuario, SituacaoDoModulo escolhido) {
        String codigoPreferido = TEMA_PREFERIDO_POR_OBJETIVO.getOrDefault(usuario.getObjetivo(), TEMA_PADRAO);
        Tema preferido = temaRepositorio.buscarPorCodigo(codigoPreferido)
                .or(() -> temaRepositorio.buscarPorCodigo(TEMA_PADRAO))
                .orElseThrow(() -> new RegraDeNegocioException("Nenhum tema cadastrado no curriculo."));

        List<Desafio> ultimos = desafioRepositorio.listarHistorico(usuario.getId(), Limit.of(1));
        boolean repetiuTemaNoMesmoModulo = !ultimos.isEmpty()
                && ultimos.get(0).getModulo().getId().equals(escolhido.modulo().getId())
                && ultimos.get(0).getTema().getId().equals(preferido.getId());

        if (!repetiuTemaNoMesmoModulo) {
            return preferido;
        }

        return temaRepositorio.listarOrdenadosPorNome().stream()
                .filter(tema -> !tema.getId().equals(preferido.getId()))
                .findFirst()
                .orElse(preferido);
    }

    private String montarMotivo(SituacaoDoModulo escolhido, Tema tema, Usuario usuario) {
        String nomeDoModulo = escolhido.modulo().getNome();
        String cena = " A cena vem do tema \"" + tema.getNome() + "\", alinhado ao objetivo "
                + usuario.getObjetivo().name().toLowerCase(Locale.ROOT).replace('_', ' ') + ".";

        if (escolhido.nuncaPraticado()) {
            return "Modulo \"" + nomeDoModulo + "\" ainda nao praticado e com pre-requisitos em dia: "
                    + "e o proximo passo do curriculo." + cena;
        }
        String notaFormatada = escolhido.nota().toPlainString();
        if (escolhido.nota().compareTo(ServicoDeModulo.NOTA_QUE_LIBERA_O_PROXIMO) < 0) {
            return "Modulo \"" + nomeDoModulo + "\" esta em vermelho (nota " + notaFormatada
                    + "), entao e o reforco mais urgente." + cena;
        }
        if (escolhido.nota().compareTo(NOTA_DE_CONSOLIDACAO) < 0) {
            return "Modulo \"" + nomeDoModulo + "\" esta em amarelo (nota " + notaFormatada
                    + ") e ainda nao consolidou." + cena;
        }
        return "Todos os modulos liberados estao consolidados; \"" + nomeDoModulo
                + "\" e o que esta ha mais tempo sem pratica, entao entra como revisao." + cena;
    }
}
