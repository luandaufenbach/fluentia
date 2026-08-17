package br.com.agenteingles.comum;

/** Lancada quando um identificador informado pelo cliente nao existe. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
