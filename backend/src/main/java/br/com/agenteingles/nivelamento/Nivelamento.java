package br.com.agenteingles.nivelamento;

import br.com.agenteingles.modulo.NivelCefr;
import br.com.agenteingles.usuario.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A conversa curta que estima o nivel de quem chega.
 *
 * <p>Guarda as perguntas junto com as respostas: a escada vai mudar com o tempo, e um
 * nivelamento antigo sem as perguntas dele nao poderia mais ser reavaliado.
 */
@Entity
@Table(name = "nivelamento")
public class Nivelamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusDoNivelamento status = StatusDoNivelamento.EM_ANDAMENTO;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_estimado", length = 2)
    private NivelCefr nivelEstimado;

    @Column(columnDefinition = "text")
    private String resumo;

    @Column(name = "iniciado_em", nullable = false)
    private LocalDateTime iniciadoEm = LocalDateTime.now();

    @Column(name = "concluido_em")
    private LocalDateTime concluidoEm;

    @OneToMany(mappedBy = "nivelamento", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem asc")
    private List<RespostaDoNivelamento> respostas = new ArrayList<>();

    protected Nivelamento() {
    }

    public Nivelamento(Usuario usuario) {
        this.usuario = usuario;
        for (int ordem = 1; ordem <= PerguntaDoNivelamento.quantidade(); ordem++) {
            respostas.add(new RespostaDoNivelamento(this, ordem, PerguntaDoNivelamento.daOrdem(ordem)));
        }
    }

    /** A proxima pergunta sem resposta, ou vazio quando a escada terminou. */
    public RespostaDoNivelamento proximaPergunta() {
        return respostas.stream()
                .filter(RespostaDoNivelamento::aguardandoResposta)
                .findFirst()
                .orElse(null);
    }

    public void concluir(NivelCefr nivelEstimado, String resumo) {
        this.nivelEstimado = nivelEstimado;
        this.resumo = resumo;
        this.status = StatusDoNivelamento.CONCLUIDO;
        this.concluidoEm = LocalDateTime.now();
    }

    public void abandonar() {
        this.status = StatusDoNivelamento.ABANDONADO;
        this.concluidoEm = LocalDateTime.now();
    }

    /** Quantas perguntas o aluno pulou: o teto costuma aparecer aqui antes da nota. */
    public long puladas() {
        return respostas.stream()
                .filter(resposta -> !resposta.aguardandoResposta() && resposta.foiPulada())
                .count();
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public StatusDoNivelamento getStatus() {
        return status;
    }

    public NivelCefr getNivelEstimado() {
        return nivelEstimado;
    }

    public String getResumo() {
        return resumo;
    }

    public List<RespostaDoNivelamento> getRespostas() {
        return respostas;
    }

    public LocalDateTime getConcluidoEm() {
        return concluidoEm;
    }
}
