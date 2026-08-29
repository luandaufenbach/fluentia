package br.com.agenteingles.custo;

import br.com.agenteingles.agente.PropriedadesDoAgente;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Grava o consumo de uma chamada.
 *
 * <p>Em transacao propria de proposito. O dinheiro ja foi gasto quando a resposta
 * chegou: se a transacao de quem chamou for desfeita depois — uma falha ao gravar a
 * avaliacao, por exemplo — o gasto continua tendo acontecido, e um registro que
 * some junto com o rollback devolve um total menor do que a fatura real.
 *
 * <p>Fica em bean separado porque {@code REQUIRES_NEW} nao tem efeito nenhum quando o
 * metodo e chamado de dentro da propria classe.
 */
@Service
public class RegistroDeConsumo {

    private static final Logger log = LoggerFactory.getLogger(RegistroDeConsumo.class);

    private final ConsumoDeApiRepositorio repositorio;
    private final PropriedadesDoAgente propriedades;

    public RegistroDeConsumo(ConsumoDeApiRepositorio repositorio, PropriedadesDoAgente propriedades) {
        this.repositorio = repositorio;
        this.propriedades = propriedades;
    }

    /**
     * @param usuarioId conta que provocou a chamada, ou nulo para rotina sem dono
     * @param itensProduzidos quantos desafios o lote rendeu, para chegar ao custo unitario
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(Long usuarioId,
                          TipoDeChamada tipo,
                          String modelo,
                          int tokensDeEntrada,
                          int tokensDeSaida,
                          int itensProduzidos) {
        BigDecimal custo = calcularCusto(modelo, tokensDeEntrada, tokensDeSaida);

        repositorio.save(new ConsumoDeApi(
                usuarioId, tipo, modelo, tokensDeEntrada, tokensDeSaida,
                itensProduzidos, custo));

        log.debug("{}: {} tokens de entrada, {} de saida, custo {}",
                tipo.descricao(), tokensDeEntrada, tokensDeSaida,
                custo == null ? "desconhecido" : "US$ " + custo.toPlainString());
    }

    /** Nulo quando o modelo nao tem preco: melhor custo desconhecido do que custo zero. */
    private BigDecimal calcularCusto(String modelo, int tokensDeEntrada, int tokensDeSaida) {
        PrecoDoModelo preco = buscarPreco(modelo);
        if (preco == null) {
            log.warn("Modelo {} sem preco configurado: os tokens sao gravados, o custo fica em aberto", modelo);
            return null;
        }
        return preco.calcular(tokensDeEntrada, tokensDeSaida);
    }

    /**
     * Busca o preco aceitando o nome com data no fim.
     *
     * <p>O que a API devolve nem sempre e o que foi pedido: pedimos
     * {@code claude-haiku-4-5} e a resposta identifica
     * {@code claude-haiku-4-5-20251001}. A busca exata falhava nesses casos e o custo
     * ficava em aberto — silenciosamente, porque o app continua funcionando e so o
     * relatorio de gasto e que fica furado.
     *
     * <p>Casa pelo prefixo mais longo, e nao por qualquer prefixo: assim
     * {@code claude-haiku-4-5-20251001} encontra {@code claude-haiku-4-5} sem risco de
     * um nome curto capturar familia errada.
     */
    private PrecoDoModelo buscarPreco(String modelo) {
        var precos = propriedades.precosPorMilhaoDeTokens();
        PrecoDoModelo exato = precos.get(modelo);
        if (exato != null) {
            return exato;
        }
        return precos.entrySet().stream()
                .filter(entrada -> modelo.startsWith(entrada.getKey()))
                .max(java.util.Comparator.comparingInt(entrada -> entrada.getKey().length()))
                .map(java.util.Map.Entry::getValue)
                .orElse(null);
    }
}
