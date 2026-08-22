package br.com.agenteingles.usuario;

/**
 * Papel da conta. Existe desde ja, com um valor so, porque acrescentar papel depois
 * significa reescrever toda regra de acesso que assumiu que todo mundo e igual.
 */
public enum PapelDoUsuario {

    /** Quem estuda. Enxerga e altera apenas os proprios dados. */
    ALUNO,

    /** Operacao do produto. Ainda sem endpoint proprio; reservado. */
    ADMINISTRADOR;

    /** O Spring Security espera o prefixo ROLE_ na autoridade. */
    public String autoridade() {
        return "ROLE_" + name();
    }
}
