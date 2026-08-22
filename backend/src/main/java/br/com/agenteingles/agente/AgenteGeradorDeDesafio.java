package br.com.agenteingles.agente;

import java.util.List;

/**
 * Cria desafios novos mirando um conceito, dentro de um tema.
 * Existem duas implementacoes por tras desta interface: a simulada e a que chama a Claude.
 */
public interface AgenteGeradorDeDesafio {

    /**
     * Gera varios desafios de uma vez.
     *
     * <p>Existe em lote, e nao um por chamada, por causa do custo: medindo o pedido real,
     * 666 dos tokens de entrada sao fixos (instrucao de sistema, dados do modulo e esquema
     * do JSON) e se repetiriam a cada desafio. Pedindo cinco de uma vez, esse custo fixo e
     * dividido por cinco. De quebra, quatro dos cinco desafios chegam ao aluno sem espera
     * de rede nenhuma.
     *
     * @param quantidade quantos desafios distintos entre si devem ser gerados
     */
    List<DesafioGerado> gerar(PedidoDeGeracao pedido, int quantidade);
}
