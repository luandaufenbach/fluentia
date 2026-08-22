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
 */
@ConfigurationProperties(prefix = "agente-ingles.seguranca")
public record PropriedadesDeSeguranca(
        int tentativasAteBloquear,
        int minutosDeBloqueio,
        int tamanhoMinimoDaSenha,
        int minutosDeSessao,
        List<String> origensPermitidas) {

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
    }
}
