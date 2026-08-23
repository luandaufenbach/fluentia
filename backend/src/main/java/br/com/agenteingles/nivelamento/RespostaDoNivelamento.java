package br.com.agenteingles.nivelamento;

import br.com.agenteingles.modulo.NivelCefr;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Uma pergunta da escada e o que o aluno respondeu — ou nao respondeu. */
@Entity
@Table(name = "resposta_do_nivelamento")
public class RespostaDoNivelamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nivelamento_id", nullable = false)
    private Nivelamento nivelamento;

    @Column(nullable = false)
    private Integer ordem;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_alvo", nullable = false, length = 2)
    private NivelCefr nivelAlvo;

    @Column(nullable = false, columnDefinition = "text")
    private String pergunta;

    /** Nulo quando o aluno pulou. Pular e sinal de teto, nao falta de dado. */
    @Column(columnDefinition = "text")
    private String resposta;

    @Column(name = "respondido_em")
    private LocalDateTime respondidoEm;

    protected RespostaDoNivelamento() {
    }

    public RespostaDoNivelamento(Nivelamento nivelamento, int ordem, PerguntaDoNivelamento pergunta) {
        this.nivelamento = nivelamento;
        this.ordem = ordem;
        this.nivelAlvo = pergunta.nivelAlvo();
        this.pergunta = pergunta.pergunta();
    }

    public void registrar(String resposta) {
        this.resposta = resposta == null || resposta.isBlank() ? null : resposta.trim();
        this.respondidoEm = LocalDateTime.now();
    }

    public boolean foiPulada() {
        return resposta == null;
    }

    public boolean aguardandoResposta() {
        return respondidoEm == null;
    }

    public Long getId() {
        return id;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public NivelCefr getNivelAlvo() {
        return nivelAlvo;
    }

    public String getPergunta() {
        return pergunta;
    }

    public String getResposta() {
        return resposta;
    }
}
