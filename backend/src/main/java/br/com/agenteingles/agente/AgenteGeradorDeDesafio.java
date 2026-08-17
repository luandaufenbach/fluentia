package br.com.agenteingles.agente;

/**
 * Cria um desafio novo mirando um conceito, dentro de um tema.
 * Existem duas implementacoes por tras desta interface: a simulada e a que chama a Claude.
 */
public interface AgenteGeradorDeDesafio {

    DesafioGerado gerar(PedidoDeGeracao pedido);
}
