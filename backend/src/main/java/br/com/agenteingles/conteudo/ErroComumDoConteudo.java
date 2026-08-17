package br.com.agenteingles.conteudo;

import jakarta.persistence.*;

/**
 * Erro que o aluno provavelmente vai cometer neste conceito. Usa o mesmo formato
 * errado -> certo da correcao do avaliador, para que ler antes e errar depois
 * falem a mesma lingua.
 */
@Entity
@Table(name = "erro_comum_do_conteudo")
public class ErroComumDoConteudo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conteudo_id", nullable = false)
    private ConteudoDoModulo conteudo;

    @Column(nullable = false)
    private Integer ordem;

    @Column(nullable = false, columnDefinition = "text")
    private String errado;

    @Column(nullable = false, columnDefinition = "text")
    private String certo;

    @Column(nullable = false, columnDefinition = "text")
    private String explicacao;

    protected ErroComumDoConteudo() {
    }

    public Long getId() {
        return id;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public String getErrado() {
        return errado;
    }

    public String getCerto() {
        return certo;
    }

    public String getExplicacao() {
        return explicacao;
    }
}
