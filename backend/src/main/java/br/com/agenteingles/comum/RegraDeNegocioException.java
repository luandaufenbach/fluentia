package br.com.agenteingles.comum;

/** Lancada quando a operacao e valida na forma, mas nao faz sentido no estado atual. */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
