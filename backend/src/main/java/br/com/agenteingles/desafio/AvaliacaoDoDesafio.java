package br.com.agenteingles.desafio;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Resultado da avaliacao de uma resposta, com os erros especificos detectados. */
@Entity
@Table(name = "avaliacao_do_desafio")
public class AvaliacaoDoDesafio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "desafio_id", nullable = false, unique = true)
    private Desafio desafio;

    @Column(name = "resposta_do_usuario", nullable = false, columnDefinition = "text")
    private String respostaDoUsuario;

    @Column(name = "nota_obtida", nullable = false, precision = 4, scale = 2)
    private BigDecimal notaObtida;

    @Column(nullable = false, columnDefinition = "text")
    private String feedback;

    @Column(name = "avaliado_em", nullable = false)
    private LocalDateTime avaliadoEm = LocalDateTime.now();

    @OneToMany(mappedBy = "avaliacao", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ErroDetectado> errosDetectados = new ArrayList<>();

    protected AvaliacaoDoDesafio() {
    }

    public AvaliacaoDoDesafio(Desafio desafio, String respostaDoUsuario, BigDecimal notaObtida, String feedback) {
        this.desafio = desafio;
        this.respostaDoUsuario = respostaDoUsuario;
        this.notaObtida = notaObtida;
        this.feedback = feedback;
    }

    public void adicionarErro(ErroDetectado erro) {
        erro.associarA(this);
        this.errosDetectados.add(erro);
    }

    public Long getId() {
        return id;
    }

    public Desafio getDesafio() {
        return desafio;
    }

    public String getRespostaDoUsuario() {
        return respostaDoUsuario;
    }

    public BigDecimal getNotaObtida() {
        return notaObtida;
    }

    public String getFeedback() {
        return feedback;
    }

    public LocalDateTime getAvaliadoEm() {
        return avaliadoEm;
    }

    public List<ErroDetectado> getErrosDetectados() {
        return errosDetectados;
    }
}
