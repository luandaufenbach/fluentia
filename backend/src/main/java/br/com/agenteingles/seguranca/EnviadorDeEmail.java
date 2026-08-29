package br.com.agenteingles.seguranca;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Envio de e-mail por SMTP.
 *
 * <p>SMTP puro, e nao a API de um provedor: comeca no Gmail, que nao exige dominio
 * proprio, e migra para Resend ou Brevo mudando servidor, porta, usuario e senha no
 * ambiente. Nenhuma linha daqui muda junto.
 *
 * <p>Sem SMTP configurado o app <b>sobe assim mesmo</b> e o link vai para o log. Isso e
 * proposital: o projeto precisa rodar num clone recem-baixado, sem credencial de e-mail,
 * e quem esta desenvolvendo consegue seguir o fluxo inteiro lendo o console. O aviso sai
 * como WARN justamente para nao passar despercebido em producao.
 */
@Component
public class EnviadorDeEmail {

    private static final Logger log = LoggerFactory.getLogger(EnviadorDeEmail.class);

    private final ObjectProvider<JavaMailSender> remetente;
    private final String de;
    private final boolean configurado;

    /**
     * O {@link JavaMailSender} vem por {@link ObjectProvider} porque ele <b>pode nao
     * existir</b>: o Spring so cria esse bean quando {@code spring.mail.host} tem valor,
     * e aqui o padrao e vazio.
     *
     * <p>Pedir o bean direto no construtor quebrava a subida inteira do app com
     * "required a bean of type JavaMailSender that could not be found" — o app nao
     * subia sem credencial de e-mail, que e exatamente o contrario do que esta classe
     * se propoe a fazer.
     */
    public EnviadorDeEmail(ObjectProvider<JavaMailSender> remetente,
                           @Value("${spring.mail.host:}") String servidor,
                           @Value("${agente-ingles.email.remetente:}") String de) {
        this.remetente = remetente;
        this.de = de;
        this.configurado = !servidor.isBlank() && !de.isBlank();
    }

    /**
     * @return {@code true} se o e-mail saiu de fato. Quem chama <b>nao</b> repassa esse
     *         resultado na resposta HTTP: dizer "enviado" ou "nao enviado" revelaria
     *         quais e-mails tem conta.
     */
    public boolean enviar(String para, String assunto, String corpo) {
        if (!configurado) {
            log.warn("SMTP nao configurado. O e-mail para {} NAO foi enviado. Conteudo:\n{}",
                    para, corpo);
            return false;
        }

        try {
            SimpleMailMessage mensagem = new SimpleMailMessage();
            mensagem.setFrom(de);
            mensagem.setTo(para);
            mensagem.setSubject(assunto);
            mensagem.setText(corpo);
            JavaMailSender servico = remetente.getIfAvailable();
            if (servico == null) {
                log.error("SMTP configurado mas o bean de envio nao existe. E-mail para {} NAO saiu.", para);
                return false;
            }
            servico.send(mensagem);
            log.info("E-mail enviado para {}", para);
            return true;
        } catch (RuntimeException falha) {
            // Alto e claro: quem pediu o link fica esperando um e-mail que nao vem, e a
            // resposta HTTP nao pode contar isso. Se nao gritar aqui, ninguem descobre.
            log.error("Falha ao enviar e-mail para {}", para, falha);
            return false;
        }
    }
}
