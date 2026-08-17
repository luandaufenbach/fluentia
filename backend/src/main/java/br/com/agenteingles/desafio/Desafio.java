package br.com.agenteingles.desafio;

import br.com.agenteingles.modulo.Modulo;
import br.com.agenteingles.tema.Tema;
import br.com.agenteingles.usuario.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Desafio gerado sob medida — nunca reaproveitado. Guarda qual conceito mirava,
 * qual tema usou e por que o orquestrador o escolheu.
 */
@Entity
@Table(name = "desafio")
public class Desafio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modulo_id", nullable = false)
    private Modulo modulo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tema_id", nullable = false)
    private Tema tema;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FormatoDoDesafio formato = FormatoDoDesafio.TEXTO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusDoDesafio status = StatusDoDesafio.AGUARDANDO_RESPOSTA;

    @Column(nullable = false, columnDefinition = "text")
    private String enunciado;

    @Column(name = "contexto_da_cena", columnDefinition = "text")
    private String contextoDaCena;

    @Column(name = "resposta_de_referencia", columnDefinition = "text")
    private String respostaDeReferencia;

    @Column(name = "criterio_de_avaliacao", columnDefinition = "text")
    private String criterioDeAvaliacao;

    /** Por que o orquestrador escolheu este modulo e este tema agora. */
    @Column(name = "motivo_da_escolha", nullable = false, columnDefinition = "text")
    private String motivoDaEscolha;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    @Column(name = "respondido_em")
    private LocalDateTime respondidoEm;

    protected Desafio() {
    }

    public Desafio(Usuario usuario, Modulo modulo, Tema tema, FormatoDoDesafio formato,
                   String enunciado, String contextoDaCena, String respostaDeReferencia,
                   String criterioDeAvaliacao, String motivoDaEscolha) {
        this.usuario = usuario;
        this.modulo = modulo;
        this.tema = tema;
        this.formato = formato;
        this.enunciado = enunciado;
        this.contextoDaCena = contextoDaCena;
        this.respostaDeReferencia = respostaDeReferencia;
        this.criterioDeAvaliacao = criterioDeAvaliacao;
        this.motivoDaEscolha = motivoDaEscolha;
    }

    public void marcarComoAvaliado(LocalDateTime momento) {
        this.status = StatusDoDesafio.AVALIADO;
        this.respondidoEm = momento;
    }

    /**
     * Sai da fila sem entrar no historico. Usado quando o aluno escolhe praticar outro
     * conceito: como nunca foi respondido, nao ha avaliacao e a media do modulo nao muda.
     */
    public void descartar() {
        this.status = StatusDoDesafio.DESCARTADO;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Modulo getModulo() {
        return modulo;
    }

    public Tema getTema() {
        return tema;
    }

    public FormatoDoDesafio getFormato() {
        return formato;
    }

    public StatusDoDesafio getStatus() {
        return status;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public String getContextoDaCena() {
        return contextoDaCena;
    }

    public String getRespostaDeReferencia() {
        return respostaDeReferencia;
    }

    public String getCriterioDeAvaliacao() {
        return criterioDeAvaliacao;
    }

    public String getMotivoDaEscolha() {
        return motivoDaEscolha;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getRespondidoEm() {
        return respondidoEm;
    }
}
