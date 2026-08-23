package br.com.agenteingles.sessao;

import br.com.agenteingles.desafio.AvaliacaoDoDesafioRepositorio;
import br.com.agenteingles.nota.NotaDoModulo;
import br.com.agenteingles.nota.NotaDoModuloRepositorio;
import br.com.agenteingles.nota.ServicoDeNota;
import br.com.agenteingles.usuario.Usuario;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * O dia do aluno: a meta de hoje, a sequencia de dias e o que o tempo esta derrubando.
 *
 * <p>Nao guarda estado proprio. Tudo aqui e derivado do historico de avaliacoes e das
 * notas ja gravadas — uma tabela de "sessao do dia" seria mais um estado para
 * dessincronizar do historico, e o historico ja e a verdade.
 */
@Service
public class ServicoDaSessao {

    /**
     * Minutos que um desafio costuma levar, do enunciado a leitura da correcao.
     * Converte o ritmo escolhido em Configuracoes num numero de desafios.
     */
    private static final int MINUTOS_POR_DESAFIO = 3;

    /** Piso e teto da meta. Meta de um desafio nao e sessao; de trinta, ninguem cumpre. */
    private static final int META_MINIMA = 3;
    private static final int META_MAXIMA = 20;

    /**
     * Queda minima para o conceito entrar na lista de revisao.
     *
     * <p>Abaixo disso a diferenca e invisivel na tela — a nota e mostrada com uma casa
     * decimal — e avisar sobre uma queda que ninguem enxerga so gera ruido.
     */
    private static final BigDecimal QUEDA_QUE_MERECE_AVISO = new BigDecimal("0.5");

    private final AvaliacaoDoDesafioRepositorio avaliacaoRepositorio;
    private final NotaDoModuloRepositorio notaRepositorio;
    private final ServicoDeNota servicoDeNota;

    public ServicoDaSessao(AvaliacaoDoDesafioRepositorio avaliacaoRepositorio,
                           NotaDoModuloRepositorio notaRepositorio,
                           ServicoDeNota servicoDeNota) {
        this.avaliacaoRepositorio = avaliacaoRepositorio;
        this.notaRepositorio = notaRepositorio;
        this.servicoDeNota = servicoDeNota;
    }

    @Transactional(readOnly = true)
    public ResumoDoDia doUsuario(Usuario usuario) {
        LocalDateTime agora = LocalDateTime.now();
        LocalDate hoje = agora.toLocalDate();

        int meta = metaDe(usuario);
        long concluidos = avaliacaoRepositorio.contarDesde(usuario.getId(), hoje.atStartOfDay());

        return new ResumoDoDia(
                meta,
                concluidos,
                concluidos >= meta,
                SequenciaDeDias.calcular(avaliacaoRepositorio.listarDiasPraticados(usuario.getId()), hoje),
                revisoesPendentes(usuario, agora));
    }

    /**
     * Meta do dia a partir do ritmo declarado pelo aluno.
     *
     * <p>Sai da preferencia que ele mesmo escolheu, e nao de um numero fixo: quinze
     * minutos por dia viram cinco desafios, que e o que ele se propos a fazer.
     */
    private int metaDe(Usuario usuario) {
        int porRitmo = usuario.getMinutosPorDia() / MINUTOS_POR_DESAFIO;
        return Math.clamp(porRitmo, META_MINIMA, META_MAXIMA);
    }

    /**
     * Conceitos que estao caindo por falta de pratica.
     *
     * <p>Nota presumida pelo nivelamento nao entra: ela nao tem data de pratica, logo nao
     * decai — nao faz sentido mandar revisar o que ainda nao foi medido.
     */
    private List<RevisaoPendente> revisoesPendentes(Usuario usuario, LocalDateTime agora) {
        List<RevisaoPendente> pendentes = new ArrayList<>();

        for (NotaDoModulo nota : notaRepositorio.listarPorUsuario(usuario.getId())) {
            LocalDateTime ultimaPratica = nota.getDataDaUltimaPratica();
            if (ultimaPratica == null) {
                continue;
            }

            BigDecimal notaHoje = servicoDeNota.aplicarDecaimento(nota.getNota(), ultimaPratica, agora);
            BigDecimal queda = nota.getNota().subtract(notaHoje);
            if (queda.compareTo(QUEDA_QUE_MERECE_AVISO) < 0) {
                continue;
            }

            pendentes.add(new RevisaoPendente(
                    nota.getModulo().getCodigo(),
                    nota.getModulo().getNome(),
                    nota.getNota(),
                    notaHoje,
                    queda,
                    servicoDeNota.faixaDa(notaHoje),
                    servicoDeNota.faixaDa(notaHoje) != servicoDeNota.faixaDa(nota.getNota()),
                    Duration.between(ultimaPratica, agora).toDays()));
        }

        // Quem mudou de faixa vem antes: ali a queda pode ter fechado o modulo seguinte.
        pendentes.sort(Comparator
                .comparing(RevisaoPendente::mudouDeFaixa).reversed()
                .thenComparing(Comparator.comparing(RevisaoPendente::queda).reversed()));
        return pendentes;
    }
}
