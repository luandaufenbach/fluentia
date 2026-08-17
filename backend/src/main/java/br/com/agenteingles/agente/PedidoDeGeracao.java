package br.com.agenteingles.agente;

import br.com.agenteingles.desafio.FormatoDoDesafio;
import br.com.agenteingles.modulo.NivelCefr;
import java.math.BigDecimal;
import java.util.List;

/**
 * Tudo que o gerador precisa para criar um desafio sob medida.
 *
 * @param enunciadosRecentes enunciados ja usados neste modulo — o novo desafio nao pode repeti-los
 * @param errosRecentes tipos de erro que o usuario vem cometendo neste conceito
 */
public record PedidoDeGeracao(
        String codigoDoModulo,
        String nomeDoModulo,
        String descricaoDoModulo,
        NivelCefr nivel,
        String nomeDoTema,
        String descricaoDoTema,
        FormatoDoDesafio formato,
        BigDecimal notaAtual,
        List<String> errosRecentes,
        List<String> enunciadosRecentes) {
}
