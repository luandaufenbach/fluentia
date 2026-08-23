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
        PrecoDoModelo preco = propriedades.precosPorMilhaoDeTokens().get(modelo);
        if (preco == null) {
            log.warn("Modelo {} sem preco configurado: os tokens sao gravados, o custo fica em aberto", modelo);
            return null;
        }
        return preco.calcular(tokensDeEntrada, tokensDeSaida);
    }
}
