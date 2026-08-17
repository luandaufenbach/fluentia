package br.com.agenteingles.conteudo.geracao;

import java.util.List;

/** O conteudo de ensino que a Claude produz para um modulo. */
public record ConteudoGerado(
        String resumo,
        String explicacao,
        List<ExemploGerado> exemplos,
        List<ErroComumGerado> errosComuns) {

    public record ExemploGerado(String emIngles, String emPortugues, String observacao) {
    }

    public record ErroComumGerado(String errado, String certo, String explicacao) {
    }
}
