package br.com.agenteingles.agente;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracao dos agentes.
 *
 * @param usarClaude quando falso, os agentes simulados assumem e nada e enviado para a API
 * @param modeloDeRaciocinio modelo usado por gerador e avaliador — a qualidade da correcao
 *                           e o coracao do produto, entao aqui vai o modelo mais capaz
 * @param modeloSimples modelo para tarefas simples e de alto volume
 * @param emailDoUsuarioPadrao usuario de desenvolvimento usado ate a autenticacao entrar
 */
@ConfigurationProperties(prefix = "agente-ingles")
public record PropriedadesDoAgente(
        boolean usarClaude,
        String modeloDeRaciocinio,
        String modeloSimples,
        String emailDoUsuarioPadrao) {

    public PropriedadesDoAgente {
        if (modeloDeRaciocinio == null || modeloDeRaciocinio.isBlank()) {
            modeloDeRaciocinio = "claude-sonnet-5";
        }
        if (modeloSimples == null || modeloSimples.isBlank()) {
            modeloSimples = "claude-haiku-4-5";
        }
        if (emailDoUsuarioPadrao == null || emailDoUsuarioPadrao.isBlank()) {
            emailDoUsuarioPadrao = "dev@agenteingles.local";
        }
    }
}
