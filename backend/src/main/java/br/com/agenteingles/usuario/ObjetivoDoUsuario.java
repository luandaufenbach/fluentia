package br.com.agenteingles.usuario;

/**
 * Objetivo declarado no onboarding. Influencia a escolha do tema pelo orquestrador.
 *
 * <p>DEV saiu: era um nicho dentro de um app que ensina a lingua, e quem esta
 * aprendendo precisa pedir comida e marcar consulta muito antes de escrever code
 * review. A variedade de cena agora vem dos temas, que sao nove.
 */
public enum ObjetivoDoUsuario {

    VIAGEM("viagem"),
    TRABALHO("trabalho"),
    CONVERSACAO_GERAL("conversação geral");

    /**
     * Como o objetivo aparece na explicacao do orquestrador. Fica aqui e nao no
     * frontend porque quem monta a frase do motivo e o backend — derivar do nome da
     * constante devolvia "conversacao geral", sem acento, direto na tela.
     */
    private final String rotulo;

    ObjetivoDoUsuario(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
