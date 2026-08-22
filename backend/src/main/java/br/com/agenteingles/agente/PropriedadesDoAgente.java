package br.com.agenteingles.agente;

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
 * @param emailDoUsuarioPadrao usuario de desenvolvimento usado ate a autenticacao entrar
 */
@ConfigurationProperties(prefix = "agente-ingles")
public record PropriedadesDoAgente(
        boolean usarClaude,
        String modeloDeRaciocinio,
        String modeloDeGeracao,
        String modeloSimples,
        int desafiosPorLote,
        String emailDoUsuarioPadrao) {

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
        if (emailDoUsuarioPadrao == null || emailDoUsuarioPadrao.isBlank()) {
            emailDoUsuarioPadrao = "dev@agenteingles.local";
        }
    }
}
