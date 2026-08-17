package br.com.agenteingles.usuario;

/** Objetivo declarado no onboarding. Influencia a escolha do tema pelo orquestrador. */
public enum ObjetivoDoUsuario {

    VIAGEM("viagem"),
    TRABALHO("trabalho"),
    DEV("inglês para dev"),
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
