package br.com.agenteingles.seguranca;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Registro de auditoria de autenticacao.
 *
 * <p>Guarda o que aconteceu, nunca o segredo. Sem esta trilha nao ha como investigar
 * um acesso indevido depois nem perceber um ataque em andamento — e a pergunta
 * "quem entrou nesta conta e quando?" fica sem resposta.
 *
 * <p>O e-mail fica em coluna propria, e nao so a referencia ao usuario, porque a
 * tentativa pode ser contra conta que nao existe: e justamente esse padrao que
 * denuncia varredura de credenciais.
 */
@Entity
@Table(name = "evento_de_autenticacao")
public class EventoDeAutenticacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(nullable = false, length = 180)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoDeEventoDeAutenticacao tipo;

    /** Endereco de origem. 45 caracteres cobrem IPv6. */
    @Column(length = 45)
    private String origem;

    /** Motivo tecnico do evento. Nunca credencial. */
    @Column(length = 200)
    private String detalhe;

    @Column(name = "ocorrido_em", nullable = false)
    private LocalDateTime ocorridoEm = LocalDateTime.now();

    protected EventoDeAutenticacao() {
    }

    public EventoDeAutenticacao(Long usuarioId,
                                String email,
                                TipoDeEventoDeAutenticacao tipo,
                                String origem,
                                String detalhe) {
        this.usuarioId = usuarioId;
        this.email = email;
        this.tipo = tipo;
        this.origem = origem;
        this.detalhe = detalhe;
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getEmail() {
        return email;
    }

    public TipoDeEventoDeAutenticacao getTipo() {
        return tipo;
    }

    public String getOrigem() {
        return origem;
    }

    public LocalDateTime getOcorridoEm() {
        return ocorridoEm;
    }
}
