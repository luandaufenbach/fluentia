package br.com.agenteingles.modulo;

import br.com.agenteingles.nota.FaixaDeNota;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Retrato do modulo para um usuario num instante: a nota ja com o esquecimento descontado,
 * a faixa de cor e se os pre-requisitos liberam a pratica.
 *
 * @param nota {@code null} quando o modulo ainda nao foi praticado (aparece como "novo")
 * @param preRequisitosPendentes nomes dos pre-requisitos que ainda nao atingiram a nota de liberacao
 */
public record SituacaoDoModulo(
        Modulo modulo,
        BigDecimal nota,
        FaixaDeNota faixa,
        boolean liberado,
        List<String> preRequisitosPendentes,
        LocalDateTime dataDaUltimaPratica,
        int quantidadeDePraticas) {

    public boolean nuncaPraticado() {
        return nota == null;
    }
}
