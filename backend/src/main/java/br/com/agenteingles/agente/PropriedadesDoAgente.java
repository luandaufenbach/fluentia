package br.com.agenteingles.agente;

import br.com.agenteingles.custo.PrecoDoModelo;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracao dos agentes.
 *
 * @param usarClaude quando falso, os agentes simulados assumem e nada e enviado para a API
 * @param modeloDeRaciocinio modelo do avaliador — a qualidade da correcao e o coracao do
 *                           produto, entao aqui vai o modelo mais capaz
 * @param modeloDeGeracao modelo do gerador de desafio. Separado do avaliador para poder
 *                        baixar so este de nivel: gerar enunciado e tarefa mais simples que
 *                        julgar resposta, e e onde esta o maior volume de tokens
 * @param modeloSimples modelo para tarefas simples e de alto volume
 * @param desafiosPorLote quantos desafios sao pedidos de uma vez ao gerador. O custo fixo
 *                        do pedido (instrucao, dados do modulo e esquema) e dividido por
 *                        este numero
 * @param desafiosPorLoteInicial lote da primeira visita a um modulo. Quem nunca praticou
 *                               ali pode nao voltar, e o lote cheio deixaria quatro
 *                               desafios pagos parados na fila
 * @param precosPorMilhaoDeTokens preco em dolar por modelo. Fica em configuracao porque
 *                                tabela de preco muda sem aviso
 * @param emailDoUsuarioPadrao usuario de desenvolvimento usado ate a autenticacao entrar
 */
@ConfigurationProperties(prefix = "agente-ingles")
public record PropriedadesDoAgente(
        boolean usarClaude,
        String modeloDeRaciocinio,
        String modeloDeGeracao,
        String modeloSimples,
        int desafiosPorLote,
        int desafiosPorLoteInicial,
        Map<String, PrecoDoModelo> precosPorMilhaoDeTokens,
        String emailDoUsuarioPadrao,
        Boolean raciocinioNaAvaliacao,
        String modeloDeAvaliacao) {

    public PropriedadesDoAgente {
        if (modeloDeRaciocinio == null || modeloDeRaciocinio.isBlank()) {
            modeloDeRaciocinio = "claude-sonnet-5";
        }
        if (modeloDeGeracao == null || modeloDeGeracao.isBlank()) {
            modeloDeGeracao = modeloDeRaciocinio;
        }
        if (modeloSimples == null || modeloSimples.isBlank()) {
            modeloSimples = "claude-haiku-4-5";
        }
        if (desafiosPorLote < 1) {
            desafiosPorLote = 5;
        }
        if (desafiosPorLoteInicial < 1 || desafiosPorLoteInicial > desafiosPorLote) {
            desafiosPorLoteInicial = Math.min(2, desafiosPorLote);
        }
        if (precosPorMilhaoDeTokens == null) {
            precosPorMilhaoDeTokens = Map.of();
        }
        if (emailDoUsuarioPadrao == null || emailDoUsuarioPadrao.isBlank()) {
            emailDoUsuarioPadrao = "dev@agenteingles.local";
        }
        // Ligado quando nao dito: a correcao e o coracao do produto, e o padrao seguro
        // e o que preserva qualidade, nao o que economiza.
        if (raciocinioNaAvaliacao == null) {
            raciocinioNaAvaliacao = Boolean.TRUE;
        }
        // Separado do modelo de raciocinio de proposito. Aquele e compartilhado com o
        // nivelamento e a geracao de conteudo: trocar la para economizar na correcao
        // levaria os tres juntos, e os tres tem exigencias diferentes. Sem valor
        // proprio, herda o de raciocinio e nada muda.
        if (modeloDeAvaliacao == null || modeloDeAvaliacao.isBlank()) {
            modeloDeAvaliacao = modeloDeRaciocinio;
        }
    }
}
