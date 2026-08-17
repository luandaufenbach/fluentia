package br.com.agenteingles.conteudo;

import br.com.agenteingles.modulo.NivelCefr;
import java.util.List;

/**
 * O conteudo pronto para a tela. Record, e nao a entidade: devolver a entidade
 * significaria carregar associacao lazy fora da transacao, que ja custou um bug aqui.
 */
public record ConteudoDoModuloResposta(
        String moduloCodigo,
        String moduloNome,
        NivelCefr nivel,
        String resumo,
        String explicacao,
        List<ExemploResposta> exemplos,
        List<ErroComumResposta> errosComuns) {

    public record ExemploResposta(String emIngles, String emPortugues, String observacao) {
    }

    public record ErroComumResposta(String errado, String certo, String explicacao) {
    }

    static ConteudoDoModuloResposta de(ConteudoDoModulo conteudo) {
        return new ConteudoDoModuloResposta(
                conteudo.getModulo().getCodigo(),
                conteudo.getModulo().getNome(),
                conteudo.getModulo().getNivelCefr(),
                conteudo.getResumo(),
                conteudo.getExplicacao(),
                conteudo.getExemplos().stream()
                        .map(exemplo -> new ExemploResposta(
                                exemplo.getEmIngles(),
                                exemplo.getEmPortugues(),
                                exemplo.getObservacao()))
                        .toList(),
                conteudo.getErrosComuns().stream()
                        .map(erro -> new ErroComumResposta(
                                erro.getErrado(),
                                erro.getCerto(),
                                erro.getExplicacao()))
                        .toList());
    }
}
