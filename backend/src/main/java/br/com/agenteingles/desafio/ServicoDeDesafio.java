package br.com.agenteingles.desafio;

import br.com.agenteingles.agente.AgenteAvaliador;
import br.com.agenteingles.agente.AgenteGeradorDeDesafio;
import br.com.agenteingles.agente.DesafioGerado;
import br.com.agenteingles.agente.ErroApontado;
import br.com.agenteingles.agente.PedidoDeAvaliacao;
import br.com.agenteingles.agente.PedidoDeGeracao;
import br.com.agenteingles.agente.PropriedadesDoAgente;
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

    /**
     * Quantos enunciados anteriores sao enviados ao gerador para ele nao repetir a cena.
     *
     * <p>Eram 20, e medindo o pedido real eles sozinhos custavam 943 dos 1.609 tokens de
     * entrada — 58% da chamada. Seis bastam para o modelo perceber o padrao do que ja foi
     * cobrado; o que garante de fato a nao repeticao e o historico gravado no banco, nao
     * o tamanho desta lista.
     */
    private static final Limit ENUNCIADOS_PARA_EVITAR_REPETICAO = Limit.of(6);

    /**
     * Enunciado longo nao ajuda o modelo a evitar repeticao: o inicio ja identifica o
     * desafio. Cortar aqui derruba o custo sem perder a funcao da lista.
     */
    private static final int TAMANHO_DO_ENUNCIADO_NA_LISTA = 90;

    /** Quantos tipos de erro recentes alimentam o reforco dirigido. */
    private static final Limit ERROS_PARA_REFORCO = Limit.of(5);

    private final Orquestrador orquestrador;
    private final AgenteGeradorDeDesafio agenteGerador;
    private final AgenteAvaliador agenteAvaliador;
    private final ServicoDeNota servicoDeNota;
    private final DesafioRepositorio desafioRepositorio;
    private final AvaliacaoDoDesafioRepositorio avaliacaoRepositorio;
    private final NotaDoModuloRepositorio notaRepositorio;
    private final PropriedadesDoAgente propriedades;

    public ServicoDeDesafio(Orquestrador orquestrador,
                            AgenteGeradorDeDesafio agenteGerador,
                            AgenteAvaliador agenteAvaliador,
                            ServicoDeNota servicoDeNota,
                            DesafioRepositorio desafioRepositorio,
                            AvaliacaoDoDesafioRepositorio avaliacaoRepositorio,
                            NotaDoModuloRepositorio notaRepositorio,
                            PropriedadesDoAgente propriedades) {
        this.orquestrador = orquestrador;
        this.agenteGerador = agenteGerador;
        this.agenteAvaliador = agenteAvaliador;
        this.servicoDeNota = servicoDeNota;
        this.desafioRepositorio = desafioRepositorio;
        this.avaliacaoRepositorio = avaliacaoRepositorio;
        this.notaRepositorio = notaRepositorio;
        this.propriedades = propriedades;
    }

    /**
     * Desafio da vez. Se ja existe um em aberto, devolve o mesmo — gerar outro descartaria
     * o anterior e criaria buraco no historico que alimenta a nota.
     */
    @Transactional
    public ResumoDoDesafio proximoDesafio(Usuario usuario) {
        return proximoDesafio(usuario, null);
    }

    /**
     * @param codigoDoModulo modulo que o aluno pediu para praticar, ou {@code null} para
     *                       deixar o orquestrador escolher
     */
    @Transactional
    public ResumoDoDesafio proximoDesafio(Usuario usuario, String codigoDoModulo) {
        List<Desafio> emAberto = desafioRepositorio.listarEmAberto(usuario.getId(), Limit.of(1));

        if (!emAberto.isEmpty()) {
            Desafio aberto = emAberto.get(0);
            boolean serveParaOPedido = codigoDoModulo == null
                    || aberto.getModulo().getCodigo().equals(codigoDoModulo);

            if (serveParaOPedido) {
                return ResumoDoDesafio.de(aberto);
            }

            // O aluno estudou outro conceito e pediu para praticar esse. Manter o desafio
            // antigo em aberto deixaria dois pendentes; descartar nao perde nota, porque
            // desafio sem resposta nunca entrou no historico que alimenta a media.
            aberto.descartar();
            log.debug("Desafio {} descartado: aluno pediu pratica de {}", aberto.getId(), codigoDoModulo);
        }

        return gerarNovoDesafio(usuario, codigoDoModulo);
    }

    @Transactional
    public ResumoDoDesafio gerarNovoDesafio(Usuario usuario) {
        return gerarNovoDesafio(usuario, null);
    }

    @Transactional
    public ResumoDoDesafio gerarNovoDesafio(Usuario usuario, String codigoDoModulo) {
        DecisaoDoOrquestrador decisao = codigoDoModulo == null
                ? orquestrador.decidirProximaPratica(usuario)
                : orquestrador.decidirPraticaDoModulo(usuario, codigoDoModulo);
        Modulo modulo = decisao.situacaoDoModulo().modulo();

        // Fila antes de API: o lote anterior ja pagou por estes desafios.
        List<Desafio> naFila = desafioRepositorio.listarNaFila(
                usuario.getId(), modulo.getId(), Limit.of(1));
        if (!naFila.isEmpty()) {
            Desafio daFila = naFila.get(0);
            daFila.apresentar(decisao.motivo());
            log.debug("Desafio {} veio da fila do modulo {}, sem chamada de API",
                    daFila.getId(), modulo.getCodigo());
            return ResumoDoDesafio.de(daFila);
        }

        return gerarLote(usuario, decisao, modulo);
    }

    /**
     * Pede o lote inteiro numa chamada e guarda o excedente na fila. O primeiro desafio
     * volta para o aluno; os demais ficam prontos para as proximas praticas do modulo.
     */
    private ResumoDoDesafio gerarLote(Usuario usuario, DecisaoDoOrquestrador decisao, Modulo modulo) {
        List<String> enunciadosRecentes = desafioRepositorio
                .listarEnunciadosRecentes(usuario.getId(), modulo.getId(), ENUNCIADOS_PARA_EVITAR_REPETICAO)
                .stream()
                .map(this::encurtar)
                .toList();
        List<String> errosRecentes = avaliacaoRepositorio.listarTiposDeErroRecentes(
                usuario.getId(), modulo.getId(), ERROS_PARA_REFORCO);

        PedidoDeGeracao pedido = new PedidoDeGeracao(
                usuario.getId(),
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

        int tamanhoDoLote = tamanhoDoLotePara(usuario, modulo);
        List<DesafioGerado> gerados = agenteGerador.gerar(pedido, tamanhoDoLote);
        log.debug("Lote de {} desafio(s) gerado para o modulo {}", gerados.size(), modulo.getCodigo());

        Desafio primeiro = null;
        for (DesafioGerado gerado : gerados) {
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

            if (primeiro == null) {
                primeiro = desafioRepositorio.save(desafio);
            } else {
                desafio.guardarNaFila();
                desafioRepositorio.save(desafio);
            }
        }
        return ResumoDoDesafio.de(primeiro);
    }

    /**
     * Lote pequeno na primeira visita ao modulo, cheio depois.
     *
     * <p>O lote existe para dividir o custo fixo do pedido, mas ele so se paga se o
     * aluno voltar aquele modulo. Quem nunca praticou ali pode nao voltar: no pior
     * caso, alguem que passa uma vez por cada um dos 16 conceitos pagaria 80 desafios
     * para usar 16. Comecar por dois derruba esse desperdicio e mantem a divisao do
     * custo fixo para quem fica.
     */
    private int tamanhoDoLotePara(Usuario usuario, Modulo modulo) {
        boolean jaPraticou = avaliacaoRepositorio.contarDoModulo(usuario.getId(), modulo.getId()) > 0;
        return jaPraticou ? propriedades.desafiosPorLote() : propriedades.desafiosPorLoteInicial();
    }

    private String encurtar(String enunciado) {
        return enunciado.length() <= TAMANHO_DO_ENUNCIADO_NA_LISTA
                ? enunciado
                : enunciado.substring(0, TAMANHO_DO_ENUNCIADO_NA_LISTA) + "...";
    }

    /**
     * Avalia a resposta, grava o historico e recalcula a nota do modulo.
     *
     * @return o resultado com a nota da resposta e a nota do modulo ja atualizada
     */
    @Transactional
    public ResultadoDaResposta responder(Usuario usuario, Long desafioId, String resposta) {
        Desafio desafio = desafioRepositorio.buscarDoUsuario(desafioId, usuario.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Desafio não encontrado: " + desafioId));

        if (desafio.getStatus() != StatusDoDesafio.AGUARDANDO_RESPOSTA) {
            throw new RegraDeNegocioException("Este desafio já foi respondido.");
        }

        Modulo modulo = desafio.getModulo();
        PedidoDeAvaliacao pedido = new PedidoDeAvaliacao(
                usuario.getId(),
                modulo.getCodigo(),
                modulo.getNome(),
                modulo.getDescricao(),
                modulo.getNivelCefr(),
                desafio.getEnunciado(),
                desafio.getContextoDaCena(),
                desafio.getRespostaDeReferencia(),
                desafio.getCriterioDeAvaliacao(),
                resposta,
                usuario.getTipoDeCorrecao());

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

        return new ResultadoDaResposta(
                desafio.getId(),
                avaliacao.getNotaObtida(),
                avaliacao.getFeedback(),
                resultado.erros() == null ? List.of() : resultado.erros(),
                notaDoModulo,
                servicoDeNota.faixaDa(notaDoModulo),
                modulo.getNome());
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
    public List<ResumoDoDesafio> historico(Usuario usuario, int quantidade) {
        return desafioRepositorio.listarHistorico(usuario.getId(), Limit.of(quantidade)).stream()
                .map(ResumoDoDesafio::de)
                .toList();
    }
}
