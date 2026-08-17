package br.com.agenteingles.desafio;

import br.com.agenteingles.agente.AgenteAvaliador;
import br.com.agenteingles.agente.AgenteGeradorDeDesafio;
import br.com.agenteingles.agente.DesafioGerado;
import br.com.agenteingles.agente.ErroApontado;
import br.com.agenteingles.agente.PedidoDeAvaliacao;
import br.com.agenteingles.agente.PedidoDeGeracao;
import br.com.agenteingles.agente.ResultadoDaAvaliacao;
import br.com.agenteingles.comum.RecursoNaoEncontradoException;
import br.com.agenteingles.comum.RegraDeNegocioException;
import br.com.agenteingles.modulo.Modulo;
import br.com.agenteingles.nota.NotaDoModulo;
import br.com.agenteingles.nota.NotaDoModuloRepositorio;
import br.com.agenteingles.nota.ServicoDeNota;
import br.com.agenteingles.orquestrador.DecisaoDoOrquestrador;
import br.com.agenteingles.orquestrador.Orquestrador;
import br.com.agenteingles.usuario.Usuario;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fecha o loop do produto: o orquestrador escolhe o que praticar, o gerador cria o desafio,
 * o avaliador julga a resposta e a nota do modulo e recalculada — o que ja muda a proxima escolha.
 */
@Service
public class ServicoDeDesafio {

    private static final Logger log = LoggerFactory.getLogger(ServicoDeDesafio.class);

    /** Quantos enunciados anteriores sao enviados ao gerador para ele nao repetir a cena. */
    private static final Limit ENUNCIADOS_PARA_EVITAR_REPETICAO = Limit.of(20);

    /** Quantos tipos de erro recentes alimentam o reforco dirigido. */
    private static final Limit ERROS_PARA_REFORCO = Limit.of(5);

    private final Orquestrador orquestrador;
    private final AgenteGeradorDeDesafio agenteGerador;
    private final AgenteAvaliador agenteAvaliador;
    private final ServicoDeNota servicoDeNota;
    private final DesafioRepositorio desafioRepositorio;
    private final AvaliacaoDoDesafioRepositorio avaliacaoRepositorio;
    private final NotaDoModuloRepositorio notaRepositorio;

    public ServicoDeDesafio(Orquestrador orquestrador,
                            AgenteGeradorDeDesafio agenteGerador,
                            AgenteAvaliador agenteAvaliador,
                            ServicoDeNota servicoDeNota,
                            DesafioRepositorio desafioRepositorio,
                            AvaliacaoDoDesafioRepositorio avaliacaoRepositorio,
                            NotaDoModuloRepositorio notaRepositorio) {
        this.orquestrador = orquestrador;
        this.agenteGerador = agenteGerador;
        this.agenteAvaliador = agenteAvaliador;
        this.servicoDeNota = servicoDeNota;
        this.desafioRepositorio = desafioRepositorio;
        this.avaliacaoRepositorio = avaliacaoRepositorio;
        this.notaRepositorio = notaRepositorio;
    }

    /**
     * Desafio da vez. Se ja existe um em aberto, devolve o mesmo — gerar outro descartaria
     * o anterior e criaria buraco no historico que alimenta a nota.
     */
    @Transactional
    public Desafio proximoDesafio(Usuario usuario) {
        List<Desafio> emAberto = desafioRepositorio.listarEmAberto(usuario.getId(), Limit.of(1));
        if (!emAberto.isEmpty()) {
            return emAberto.get(0);
        }
        return gerarNovoDesafio(usuario);
    }

    @Transactional
    public Desafio gerarNovoDesafio(Usuario usuario) {
        DecisaoDoOrquestrador decisao = orquestrador.decidirProximaPratica(usuario);
        Modulo modulo = decisao.situacaoDoModulo().modulo();

        List<String> enunciadosRecentes = desafioRepositorio.listarEnunciadosRecentes(
                usuario.getId(), modulo.getId(), ENUNCIADOS_PARA_EVITAR_REPETICAO);
        List<String> errosRecentes = avaliacaoRepositorio.listarTiposDeErroRecentes(
                usuario.getId(), modulo.getId(), ERROS_PARA_REFORCO);

        PedidoDeGeracao pedido = new PedidoDeGeracao(
                modulo.getCodigo(),
                modulo.getNome(),
                modulo.getDescricao(),
                modulo.getNivelCefr(),
                decisao.tema().getNome(),
                decisao.tema().getDescricao(),
                FormatoDoDesafio.TEXTO,
                decisao.situacaoDoModulo().nota(),
                errosRecentes,
                enunciadosRecentes);

        DesafioGerado gerado = agenteGerador.gerar(pedido);
        log.debug("Desafio gerado para o modulo {}: {}", modulo.getCodigo(), gerado.enunciado());

        Desafio desafio = new Desafio(
                usuario,
                modulo,
                decisao.tema(),
                FormatoDoDesafio.TEXTO,
                gerado.enunciado(),
                gerado.contextoDaCena(),
                gerado.respostaDeReferencia(),
                gerado.criterioDeAvaliacao(),
                decisao.motivo());

        return desafioRepositorio.save(desafio);
    }

    /**
     * Avalia a resposta, grava o historico e recalcula a nota do modulo.
     *
     * @return o resultado com a nota da resposta e a nota do modulo ja atualizada
     */
    @Transactional
    public ResultadoDaResposta responder(Usuario usuario, Long desafioId, String resposta) {
        Desafio desafio = desafioRepositorio.buscarDoUsuario(desafioId, usuario.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Desafio nao encontrado: " + desafioId));

        if (desafio.getStatus() != StatusDoDesafio.AGUARDANDO_RESPOSTA) {
            throw new RegraDeNegocioException("Este desafio ja foi respondido.");
        }

        Modulo modulo = desafio.getModulo();
        PedidoDeAvaliacao pedido = new PedidoDeAvaliacao(
                modulo.getCodigo(),
                modulo.getNome(),
                modulo.getDescricao(),
                modulo.getNivelCefr(),
                desafio.getEnunciado(),
                desafio.getContextoDaCena(),
                desafio.getRespostaDeReferencia(),
                desafio.getCriterioDeAvaliacao(),
                resposta);

        ResultadoDaAvaliacao resultado = agenteAvaliador.avaliar(pedido);
        LocalDateTime agora = LocalDateTime.now();

        AvaliacaoDoDesafio avaliacao = new AvaliacaoDoDesafio(
                desafio, resposta, resultado.notaObtida(), resultado.feedback());
        if (resultado.erros() != null) {
            for (ErroApontado erro : resultado.erros()) {
                avaliacao.adicionarErro(new ErroDetectado(
                        erro.tipo(), erro.trechoErrado(), erro.correcao(), erro.explicacao()));
            }
        }
        avaliacaoRepositorio.save(avaliacao);

        desafio.marcarComoAvaliado(agora);
        desafioRepositorio.save(desafio);

        BigDecimal notaDoModulo = recalcularNotaDoModulo(usuario, modulo, agora);
        log.debug("Modulo {} recalculado para a nota {}", modulo.getCodigo(), notaDoModulo);

        return new ResultadoDaResposta(avaliacao, notaDoModulo, servicoDeNota.faixaDa(notaDoModulo));
    }

    /**
     * Recalcula a nota do modulo a partir do historico gravado. Sempre parte do historico,
     * e nao da nota anterior, para que a media ponderada e o banco nunca divirjam.
     */
    private BigDecimal recalcularNotaDoModulo(Usuario usuario, Modulo modulo, LocalDateTime agora) {
        List<AvaliacaoDoDesafio> recentes = avaliacaoRepositorio.listarRecentesDoModulo(
                usuario.getId(), modulo.getId(), Limit.of(ServicoDeNota.QUANTIDADE_DE_AVALIACOES_CONSIDERADAS));

        List<BigDecimal> notas = new ArrayList<>();
        for (AvaliacaoDoDesafio avaliacao : recentes) {
            notas.add(avaliacao.getNotaObtida());
        }

        BigDecimal notaCalculada = servicoDeNota.calcularNotaDaPratica(notas);

        NotaDoModulo notaDoModulo = notaRepositorio
                .buscarPorUsuarioEModulo(usuario.getId(), modulo.getId())
                .orElseGet(() -> new NotaDoModulo(usuario, modulo, notaCalculada));

        notaDoModulo.registrarPratica(notaCalculada, agora);
        notaRepositorio.save(notaDoModulo);

        return notaCalculada;
    }

    @Transactional(readOnly = true)
    public List<Desafio> historico(Usuario usuario, int quantidade) {
        return desafioRepositorio.listarHistorico(usuario.getId(), Limit.of(quantidade));
    }
}
