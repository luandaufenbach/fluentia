package br.com.agenteingles.seguranca;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Um pedido de recuperacao de senha.
 *
 * <p>Guarda o <b>hash</b> do token, nunca o token. Ele so existe no e-mail que a pessoa
 * recebeu e na memoria da requisicao que o gerou — pelo mesmo motivo que a senha e
 * guardada como hash: um vazamento deste banco nao pode virar acesso as contas.
 */
@Entity
@Table(name = "recuperacao_de_senha")
public class RecuperacaoDeSenha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    @Column(name = "usado_em")
    private LocalDateTime usadoEm;

    @Column(length = 45)
    private String origem;

    protected RecuperacaoDeSenha() {
    }

    public RecuperacaoDeSenha(Long usuarioId, String tokenHash, LocalDateTime expiraEm, String origem) {
        this.usuarioId = usuarioId;
        this.tokenHash = tokenHash;
        this.expiraEm = expiraEm;
        this.origem = origem;
    }

    /**
     * Vale so uma vez e so dentro do prazo.
     *
     * <p>As duas condicoes existem por motivos diferentes: o prazo limita a janela em
     * que um e-mail vazado serve para alguma coisa; o uso unico impede que o mesmo link,
     * ainda no prazo, seja usado de novo por quem tenha acesso a caixa de entrada depois.
     */
    public boolean utilizavel(LocalDateTime agora) {
        return usadoEm == null && agora.isBefore(expiraEm);
    }

    public void marcarComoUsado(LocalDateTime agora) {
        this.usadoEm = agora;
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }

    public LocalDateTime getUsadoEm() {
        return usadoEm;
    }
}
