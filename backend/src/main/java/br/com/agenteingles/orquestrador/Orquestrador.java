package br.com.agenteingles.orquestrador;

import br.com.agenteingles.comum.RecursoNaoEncontradoException;
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
                    "Nenhum módulo liberado para prática. Verifique os pré-requisitos da trilha.");
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
                .orElseThrow(() -> new RegraDeNegocioException("Não foi possível escolher um módulo para praticar."));
    }

    /**
     * Tema preferido do objetivo do usuario, trocando de cena quando o ultimo desafio
     * daquele modulo ja usou esse tema — o conceito se repete, a roupagem nao.
     */
    private Tema escolherTema(Usuario usuario, SituacaoDoModulo escolhido) {
        String codigoPreferido = TEMA_PREFERIDO_POR_OBJETIVO.getOrDefault(usuario.getObjetivo(), TEMA_PADRAO);
        Tema preferido = temaRepositorio.buscarPorCodigo(codigoPreferido)
                .or(() -> temaRepositorio.buscarPorCodigo(TEMA_PADRAO))
                .orElseThrow(() -> new RegraDeNegocioException("Nenhum tema cadastrado na trilha."));

        List<Desafio> ultimos = desafioRepositorio.listarHistorico(usuario.getId(), Limit.of(1));
        boolean repetiuTemaNoMesmoModulo = !ultimos.isEmpty()
                && ultimos.get(0).getModulo().getId().equals(escolhido.modulo().getId())
                && ultimos.get(0).getTema().getId().equals(preferido.getId());

        if (!repetiuTemaNoMesmoModulo) {
            return preferido;
        }

        return proximaCenaDiferente(usuario, escolhido, preferido);
    }

    /**
     * Uma cena diferente da anterior, girando por toda a lista de temas.
     *
     * <p>Antes isto era {@code findFirst()} na lista ordenada por nome: com dois temas
     * funcionava, mas o app tem nove, e a alternativa caia sempre no mesmo — sete cenas
     * nunca apareceriam. O aluno via o mesmo par de cenários se revezando para sempre.
     *
     * <p>O giro usa quantos desafios ele ja fez naquele conceito. E deterministico de
     * proposito: sorteio pode repetir a mesma cena tres vezes seguidas, que e exatamente
     * o que este metodo existe para evitar.
     */
    private Tema proximaCenaDiferente(Usuario usuario, SituacaoDoModulo escolhido, Tema preferido) {
        List<Tema> outros = temaRepositorio.listarOrdenadosPorNome().stream()
                .filter(tema -> !tema.getId().equals(preferido.getId()))
                .toList();

        if (outros.isEmpty()) {
            return preferido;
        }

        long jaFeitos = desafioRepositorio.contarDoModulo(usuario.getId(), escolhido.modulo().getId());
        return outros.get((int) (jaFeitos % outros.size()));
    }

    /**
     * Pratica dirigida: o aluno acabou de estudar um modulo e pediu para exercitar
     * justamente ele. A ordem de prioridade do orquestrador nao se aplica aqui — quem
     * escolheu foi o aluno, e o orquestrador so decide a cena. O pre-requisito continua
     * valendo: praticar um conceito cuja base ainda esta aberta so produz erro sem causa.
     */
    @Transactional(readOnly = true)
    public DecisaoDoOrquestrador decidirPraticaDoModulo(Usuario usuario, String codigoDoModulo) {
        SituacaoDoModulo situacao = servicoDeModulo.situacaoDeTodosOsModulos(usuario).stream()
                .filter(candidato -> candidato.modulo().getCodigo().equals(codigoDoModulo))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Módulo %s não existe.".formatted(codigoDoModulo)));

        if (!situacao.liberado()) {
            throw new RegraDeNegocioException(
                    "Módulo %s ainda depende de: %s.".formatted(
                            situacao.modulo().getNome(),
                            String.join(", ", situacao.preRequisitosPendentes())));
        }

        Tema tema = escolherTema(usuario, situacao);
        return new DecisaoDoOrquestrador(situacao, tema, montarMotivoEscolhidoPeloAluno(situacao, tema));
    }

    private String montarMotivoEscolhidoPeloAluno(SituacaoDoModulo escolhido, Tema tema) {
        // Sem aspas em volta do nome: alguns modulos ja tem aspas no proprio nome
        // (Verbo "to be"), e o texto sairia com aspas aninhadas.
        return "Você escolheu praticar " + escolhido.modulo().getNome()
                + " logo depois de estudar o conteúdo. A cena vem do tema "
                + tema.getNome() + ".";
    }

    /*
     * Sem aspas em volta do nome do modulo: alguns ja tem aspas no proprio nome
     * (Verbo "to be") e o texto sairia com aspas aninhadas na tela.
     */
    private String montarMotivo(SituacaoDoModulo escolhido, Tema tema, Usuario usuario) {
        String nomeDoModulo = escolhido.modulo().getNome();
        String cena = " A cena vem do tema " + tema.getNome() + ", alinhado ao objetivo "
                + usuario.getObjetivo().getRotulo() + ".";

        if (escolhido.nuncaPraticado()) {
            return nomeDoModulo + " ainda não foi praticado e está com os pré-requisitos em dia: "
                    + "é o próximo passo da trilha." + cena;
        }
        String notaFormatada = escolhido.nota().toPlainString();
        if (escolhido.nota().compareTo(ServicoDeModulo.NOTA_QUE_LIBERA_O_PROXIMO) < 0) {
            return nomeDoModulo + " está em vermelho (nota " + notaFormatada
                    + "), então é o reforço mais urgente." + cena;
        }
        if (escolhido.nota().compareTo(NOTA_DE_CONSOLIDACAO) < 0) {
            return nomeDoModulo + " está em amarelo (nota " + notaFormatada
                    + ") e ainda não consolidou." + cena;
        }
        return "Todos os módulos liberados estão consolidados; " + nomeDoModulo
                + " é o que está há mais tempo sem prática, então entra como revisão." + cena;
    }
}
