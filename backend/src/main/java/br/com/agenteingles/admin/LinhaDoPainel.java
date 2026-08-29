package br.com.agenteingles.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Uma conta na visao do administrador: quem e, o que gastou e o que praticou.
 *
 * <p>Junta consumo e atividade numa linha so de proposito. Separado em duas telas, a
 * pergunta que importa — "esta conta esta gastando muito para o tanto que estuda?" —
 * exigiria comparar dois lugares de cabeca.
 *
 * @param custoUsd nulo quando algum modelo usado nao tinha preco configurado. Nulo aqui
 *                 significa "nao da para saber", e nunca zero: um total que soma
 *                 desconhecido como zero mente para baixo, que e a direcao perigosa
 * @param ultimoAcessoEm nulo para quem se cadastrou e nunca voltou
 */
public record LinhaDoPainel(
        Long usuarioId,
        String nome,
        String email,
        String papel,
        boolean ativo,
        boolean bloqueada,
        LocalDateTime criadoEm,
        LocalDateTime ultimoAcessoEm,
        long chamadas,
        long tokensDeEntrada,
        long tokensDeSaida,
        BigDecimal custoUsd,
        long desafiosRespondidos) {
}
