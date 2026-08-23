package br.com.agenteingles.nivelamento;

import br.com.agenteingles.agente.AgenteDeNivelamento;
import br.com.agenteingles.agente.PedidoDeNivelamento;
import br.com.agenteingles.agente.ResultadoDoNivelamento;
import br.com.agenteingles.comum.RecursoNaoEncontradoException;
import br.com.agenteingles.comum.RegraDeNegocioException;
import br.com.agenteingles.modulo.Modulo;
import br.com.agenteingles.modulo.ModuloRepositorio;
import br.com.agenteingles.modulo.NivelCefr;
import br.com.agenteingles.nota.NotaDoModulo;
import br.com.agenteingles.nota.NotaDoModuloRepositorio;
import br.com.agenteingles.usuario.Usuario;
import br.com.agenteingles.usuario.UsuarioRepositorio;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * O nivelamento de entrada: uma conversa curta que decide por onde a trilha comeca.
 *
 * <p>Sem ele todo mundo comecava em A1, e quem ja sabe ingles abandonava na primeira tela.
 */
@Service
public class ServicoDeNivelamento {

    private static final Logger log = LoggerFactory.getLogger(ServicoDeNivelamento.class);

    /**
     * Nota dada aos modulos abaixo do nivel estimado.
     *
     * <p>Sete, e nao dez, porque nada disso foi provado aqui: e uma presuncao, nao um
     * resultado. Sete passa do limite de liberacao (seis), entao a trilha abre ate o nivel
     * da pessoa, e ainda assim o modulo aparece em amarelo — dizendo, corretamente, que
     * ele ainda nao foi demonstrado.
     *
     * <p>Como a quantidade de praticas fica em zero e nao ha data de pratica, a nota nao
     * sofre o decaimento por esquecimento: nao faz sentido "esquecer" o que ainda nao foi
     * medido.
     */
    private static final BigDecimal NOTA_PRESUMIDA = new BigDecimal("7.00");

    private final AgenteDeNivelamento agente;
    private final NivelamentoRepositorio nivelamentoRepositorio;
    private final ModuloRepositorio moduloRepositorio;
    private final NotaDoModuloRepositorio notaRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    public ServicoDeNivelamento(AgenteDeNivelamento agente,
                                NivelamentoRepositorio nivelamentoRepositorio,
                                ModuloRepositorio moduloRepositorio,
                                NotaDoModuloRepositorio notaRepositorio,
                                UsuarioRepositorio usuarioRepositorio) {
        this.agente = agente;
        this.nivelamentoRepositorio = nivelamentoRepositorio;
        this.moduloRepositorio = moduloRepositorio;
        this.notaRepositorio = notaRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    /**
     * Retoma o nivelamento aberto ou comeca um novo.
     *
     * <p>Trava a conta antes de olhar. Duas requisicoes simultaneas de inicio — duas
     * abas, um duplo clique, o modo estrito do React chamando o efeito duas vezes —
     * passariam juntas pelo "ja existe?" e as duas tentariam inserir; a segunda morreria
     * no indice unico e viraria erro na tela de quem clicou uma vez so. Com a trava a
     * segunda espera e encontra o que a primeira criou.
     */
    @Transactional
    public EtapaDoNivelamento iniciar(Usuario usuario) {
        usuarioRepositorio.travar(usuario.getId());

        Nivelamento nivelamento = nivelamentoRepositorio.emAndamento(usuario.getId())
                .orElseGet(() -> nivelamentoRepositorio.save(new Nivelamento(usuario)));

        return proximaEtapa(nivelamento, usuario);
    }

    /** Se a conta ja passou por um nivelamento — a tela usa isto para saber se deve oferecer. */
    @Transactional(readOnly = true)
    public boolean jaFezNivelamento(Usuario usuario) {
        return nivelamentoRepositorio.concluidosDoUsuario(usuario.getId()) > 0;
    }

    /**
     * Registra a resposta e devolve a proxima pergunta — ou o resultado, se a escada acabou.
     *
     * @param resposta em branco ou nula significa pular, que e um sinal legitimo de teto
     */
    @Transactional
    public EtapaDoNivelamento responder(Usuario usuario, Long nivelamentoId, int ordem, String resposta) {
        Nivelamento nivelamento = nivelamentoRepositorio
                .buscarDoUsuario(nivelamentoId, usuario.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Nivelamento não encontrado: " + nivelamentoId));

        if (nivelamento.getStatus() != StatusDoNivelamento.EM_ANDAMENTO) {
            throw new RegraDeNegocioException("Este nivelamento já foi encerrado.");
        }

        RespostaDoNivelamento pendente = nivelamento.proximaPergunta();
        if (pendente == null) {
            throw new RegraDeNegocioException("Não há mais perguntas neste nivelamento.");
        }
        if (pendente.getOrdem() != ordem) {
            // A tela e a conversa precisam estar no mesmo ponto: aceitar fora de ordem
            // deixaria uma pergunta sem resposta no meio, e o veredito sairia de dado errado.
            throw new RegraDeNegocioException(
                    "Esperando a resposta da pergunta %d.".formatted(pendente.getOrdem()));
        }

        pendente.registrar(resposta);
        return proximaEtapa(nivelamento, usuario);
    }

    /** Encerra sem concluir: quem prefere comecar do zero nao fica com a conversa presa. */
    @Transactional
    public void abandonar(Usuario usuario, Long nivelamentoId) {
        nivelamentoRepositorio.buscarDoUsuario(nivelamentoId, usuario.getId())
                .filter(nivelamento -> nivelamento.getStatus() == StatusDoNivelamento.EM_ANDAMENTO)
                .ifPresent(Nivelamento::abandonar);
    }

    private EtapaDoNivelamento proximaEtapa(Nivelamento nivelamento, Usuario usuario) {
        RespostaDoNivelamento proxima = nivelamento.proximaPergunta();
        if (proxima != null) {
            return EtapaDoNivelamento.pergunta(nivelamento, proxima);
        }
        return EtapaDoNivelamento.concluido(nivelamento, concluir(nivelamento, usuario));
    }

    private EtapaDoNivelamento.ResultadoParaOAluno concluir(Nivelamento nivelamento, Usuario usuario) {
        ResultadoDoNivelamento veredito = agente.estimar(montarPedido(nivelamento));
        NivelCefr nivel = interpretarNivel(veredito.nivelCefr());

        nivelamento.concluir(nivel, veredito.resumo());

        usuario.setNivelEstimado(nivel);
        usuarioRepositorio.save(usuario);

        List<Modulo> presumidos = presumirModulosAbaixoDe(usuario, nivel);

        log.info("Nivelamento {} concluido no nivel {}: {} modulo(s) presumidos",
                nivelamento.getId(), nivel, presumidos.size());

        return new EtapaDoNivelamento.ResultadoParaOAluno(
                nivel.name(),
                veredito.resumo(),
                veredito.pontoForte(),
                veredito.pontoAFortalecer(),
                presumidos.size(),
                primeiroModuloDoNivel(nivel));
    }

    /**
     * Da nota presumida aos modulos de nivel abaixo do estimado.
     *
     * <p>E o que faz a estimativa virar ponto de partida de verdade. Sem isto o
     * pre-requisito de cada modulo continuaria pendente e a pessoa cairia em A1 de novo,
     * com um nivel bonito escrito no perfil e nenhum efeito na trilha.
     *
     * <p>Nunca sobrescreve nota existente: pratica de verdade vale mais do que presuncao,
     * mesmo quando a presuncao e mais alta.
     */
    private List<Modulo> presumirModulosAbaixoDe(Usuario usuario, NivelCefr nivel) {
        List<Modulo> presumidos = new ArrayList<>();

        for (Modulo modulo : moduloRepositorio.listarTodosComPreRequisitos()) {
            if (modulo.getNivelCefr().ordinal() >= nivel.ordinal()) {
                continue;
            }
            boolean jaTemNota = notaRepositorio
                    .buscarPorUsuarioEModulo(usuario.getId(), modulo.getId())
                    .isPresent();
            if (jaTemNota) {
                continue;
            }
            notaRepositorio.save(new NotaDoModulo(usuario, modulo, NOTA_PRESUMIDA));
            presumidos.add(modulo);
        }
        return presumidos;
    }

    private String primeiroModuloDoNivel(NivelCefr nivel) {
        return moduloRepositorio.listarTodosComPreRequisitos().stream()
                .filter(modulo -> modulo.getNivelCefr() == nivel)
                .map(Modulo::getNome)
                .findFirst()
                .orElse(null);
    }

    private PedidoDeNivelamento montarPedido(Nivelamento nivelamento) {
        List<PedidoDeNivelamento.TurnoDoNivelamento> turnos = nivelamento.getRespostas().stream()
                .map(resposta -> new PedidoDeNivelamento.TurnoDoNivelamento(
                        resposta.getNivelAlvo().name(),
                        resposta.getPergunta(),
                        resposta.getResposta()))
                .toList();

        return new PedidoDeNivelamento(nivelamento.getUsuario().getId(), turnos);
    }

    /**
     * Nivel em branco ou fora do quadro vira A1.
     *
     * <p>Cair para o mais baixo e a falha segura: comecar abaixo do proprio nivel custa
     * alguns minutos, comecar acima faz a pessoa desistir.
     */
    private NivelCefr interpretarNivel(String nivelCefr) {
        if (nivelCefr == null || nivelCefr.isBlank()) {
            return NivelCefr.A1;
        }
        try {
            return NivelCefr.valueOf(nivelCefr.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException foraDoQuadro) {
            log.warn("Nivel '{}' fora do quadro CEFR: assumindo A1", nivelCefr);
            return NivelCefr.A1;
        }
    }
}
