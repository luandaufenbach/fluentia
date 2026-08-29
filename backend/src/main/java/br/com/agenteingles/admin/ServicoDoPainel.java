package br.com.agenteingles.admin;

import br.com.agenteingles.custo.ConsumoDeApiRepositorio;
import br.com.agenteingles.usuario.Usuario;
import br.com.agenteingles.usuario.UsuarioRepositorio;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Monta a visao do administrador: gasto e atividade de todas as contas.
 *
 * <p><b>Somente leitura.</b> Este servico nao desativa conta, nao troca senha, nao apaga
 * nada. E deliberado: um painel que so observa nao tem como quebrar o que observa, e
 * poder sobre a conta alheia merece um caminho proprio, pensado a parte — nao um botao
 * ao lado de um numero.
 */
@Service
public class ServicoDoPainel {

    private final UsuarioRepositorio usuarioRepositorio;
    private final ConsumoDeApiRepositorio consumoRepositorio;
    private final PainelRepositorio painelRepositorio;

    public ServicoDoPainel(UsuarioRepositorio usuarioRepositorio,
                           ConsumoDeApiRepositorio consumoRepositorio,
                           PainelRepositorio painelRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.consumoRepositorio = consumoRepositorio;
        this.painelRepositorio = painelRepositorio;
    }

    @Transactional(readOnly = true)
    public PainelDoAdministrador montar() {
        LocalDateTime agora = LocalDateTime.now();

        /*
         * Tres consultas agregadas e a juncao em Java, em vez de uma consulta com
         * subconsultas correlacionadas.
         *
         * A consulta unica seria uma linha por conta com dois subselects dentro do
         * SELECT — dificil de ler, dificil de mudar, e do tipo que degrada em silencio
         * quando a tabela cresce. Aqui sao tres varreduras agregadas e um mapa: com o
         * numero de contas que este app tem, a diferenca de tempo e irrelevante, e a
         * diferenca de clareza nao e.
         */
        Map<Long, TotalDoUsuario> consumoPorConta = consumoRepositorio.somarPorUsuario().stream()
                .collect(java.util.stream.Collectors.toMap(TotalDoUsuario::usuarioId, Function.identity()));

        Map<Long, Long> respondidosPorConta = painelRepositorio.contarRespondidosPorUsuario().stream()
                .collect(java.util.stream.Collectors.toMap(
                        RespondidosDoUsuario::usuarioId, RespondidosDoUsuario::total));

        List<LinhaDoPainel> contas = usuarioRepositorio.findAll().stream()
                .map(usuario -> montarLinha(usuario, consumoPorConta, respondidosPorConta, agora))
                /*
                 * Do maior gasto para o menor: e a ordem que responde a pergunta que faz
                 * alguem abrir este painel. Por nome ou por data de cadastro, seria
                 * preciso varrer a lista inteira para achar isso.
                 *
                 * Custo nulo (modelo sem preco) vai para o fim, e nao para o topo como
                 * um nulo iria naturalmente: gasto desconhecido nao e gasto alto.
                 */
                .sorted(Comparator.comparing(LinhaDoPainel::custoUsd,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        return new PainelDoAdministrador(
                consumoRepositorio.somarDesde(LocalDate.now().atStartOfDay()),
                consumoRepositorio.somarDesde(agora.minusDays(7)),
                consumoRepositorio.somarTudo(),
                consumoRepositorio.agruparPorTipoGeral(),
                contas.stream().filter(LinhaDoPainel::ativo).count(),
                contas.size(),
                contas,
                consumoRepositorio.modelosSemPrecoGeral());
    }

    private LinhaDoPainel montarLinha(Usuario usuario,
                                      Map<Long, TotalDoUsuario> consumo,
                                      Map<Long, Long> respondidos,
                                      LocalDateTime agora) {
        TotalDoUsuario gasto = consumo.get(usuario.getId());

        return new LinhaDoPainel(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPapel().name(),
                usuario.isAtivo(),
                usuario.estaBloqueado(agora),
                usuario.getCriadoEm(),
                usuario.getUltimoAcessoEm(),
                gasto == null ? 0 : gasto.chamadas(),
                gasto == null ? 0 : gasto.tokensDeEntrada(),
                gasto == null ? 0 : gasto.tokensDeSaida(),
                // Conta sem consumo tem custo ZERO, e nao desconhecido: ela realmente
                // nao gastou. Nulo aqui fica reservado para "gastou, mas nao da para
                // saber quanto" — que e outra coisa, e some no meio se as duas
                // usarem o mesmo valor.
                gasto == null ? BigDecimal.ZERO : gasto.custoUsd(),
                respondidos.getOrDefault(usuario.getId(), 0L));
    }
}
