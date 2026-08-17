package br.com.agenteingles.desafio;

import jakarta.persistence.*;

/**
 * Erro especifico apontado pelo avaliador. E o que permite reforco dirigido:
 * o orquestrador usa o historico de erros, e nao so a nota, para decidir o proximo desafio.
 */
@Entity
@Table(name = "erro_detectado")
public class ErroDetectado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "avaliacao_id", nullable = false)
    private AvaliacaoDoDesafio avaliacao;

    @Column(nullable = false, length = 60)
    private String tipo;

    @Column(name = "trecho_errado", columnDefinition = "text")
    private String trechoErrado;

    @Column(columnDefinition = "text")
    private String correcao;

    @Column(nullable = false, columnDefinition = "text")
    private String explicacao;

    protected ErroDetectado() {
    }

    public ErroDetectado(String tipo, String trechoErrado, String correcao, String explicacao) {
        this.tipo = tipo;
        this.trechoErrado = trechoErrado;
        this.correcao = correcao;
        this.explicacao = explicacao;
    }

    void associarA(AvaliacaoDoDesafio avaliacao) {
        this.avaliacao = avaliacao;
    }

    public Long getId() {
        return id;
    }

    public AvaliacaoDoDesafio getAvaliacao() {
        return avaliacao;
    }

    public String getTipo() {
        return tipo;
    }

    public String getTrechoErrado() {
        return trechoErrado;
    }

    public String getCorrecao() {
        return correcao;
    }

    public String getExplicacao() {
        return explicacao;
    }
}
