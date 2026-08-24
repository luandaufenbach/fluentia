package br.com.agenteingles.seguranca;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Parametros de seguranca. Ficam em configuracao, e nao espalhados no codigo, porque
 * limite de tentativa e tempo de bloqueio sao decisao operacional: mudam conforme o
 * ataque observado, sem precisar de novo build.
 *
 * @param tentativasAteBloquear falhas seguidas antes de bloquear a conta
 * @param minutosDeBloqueio duracao do bloqueio temporario
 * @param tamanhoMinimoDaSenha o comprimento e o que mais pesa contra forca bruta;
 *                             a NIST 800-63B recomenda exigir tamanho e nao
 *                             composicao obrigatoria de caracteres
 * @param minutosDeSessao tempo de inatividade ate a sessao expirar
 * @param origensPermitidas origens que podem chamar a API pelo navegador
 * @param cadastrosPorOrigemPorHora contas que um mesmo endereco pode criar por hora.
 *                                  O limite por conta nao serve aqui: criar conta nao
 *                                  falha, entao nao ha contador de falha para estourar
 * @param recusasPorOrigemPorHora recusas de login que um mesmo endereco pode acumular
 *                                por hora, cobrindo quem espalha poucas tentativas por
 *                                muitas contas e nunca estoura o contador de nenhuma
 */
@ConfigurationProperties(prefix = "agente-ingles.seguranca")
public record PropriedadesDeSeguranca(
        int tentativasAteBloquear,
        int minutosDeBloqueio,
        int tamanhoMinimoDaSenha,
        int minutosDeSessao,
        List<String> origensPermitidas,
        int cadastrosPorOrigemPorHora,
        int recusasPorOrigemPorHora) {

    public PropriedadesDeSeguranca {
        if (tentativasAteBloquear < 1) {
            tentativasAteBloquear = 5;
        }
        if (minutosDeBloqueio < 1) {
            minutosDeBloqueio = 15;
        }
        if (tamanhoMinimoDaSenha < 8) {
            tamanhoMinimoDaSenha = 10;
        }
        if (minutosDeSessao < 1) {
            minutosDeSessao = 60;
        }
        if (origensPermitidas == null || origensPermitidas.isEmpty()) {
            origensPermitidas = List.of("http://localhost:5173");
        }
        // Os dois limites caem para um padrao seguro quando vierem zerados, e nunca para
        // "sem limite": configuracao ausente precisa falhar fechada. Um erro de digitacao
        // no ambiente nao pode desligar a protecao em silencio.
        if (cadastrosPorOrigemPorHora < 1) {
            cadastrosPorOrigemPorHora = 3;
        }
        if (recusasPorOrigemPorHora < 1) {
            recusasPorOrigemPorHora = 30;
        }
    }
}
