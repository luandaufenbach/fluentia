package br.com.agenteingles.agente;

import java.util.List;

/**
 * Envelope do lote devolvido pelo gerador.
 *
 * <p>A saida estruturada precisa de um objeto na raiz, entao a lista vem embrulhada
 * aqui em vez de solta.
 */
public record LoteDeDesafios(List<DesafioGerado> desafios) {
}
