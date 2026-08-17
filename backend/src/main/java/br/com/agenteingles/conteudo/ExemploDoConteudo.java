package br.com.agenteingles.conteudo;

import jakarta.persistence.*;

/** Frase de exemplo do conceito, com a traducao ao lado. */
@Entity
@Table(name = "exemplo_do_conteudo")
public class ExemploDoConteudo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conteudo_id", nullable = false)
    private ConteudoDoModulo conteudo;

    @Column(nullable = false)
    private Integer ordem;

    @Column(name = "em_ingles", nullable = false, columnDefinition = "text")
    private String emIngles;

    @Column(name = "em_portugues", nullable = false, columnDefinition = "text")
    private String emPortugues;

    /** Detalhe opcional sobre a frase — por que ela usa aquela forma. */
    @Column(columnDefinition = "text")
    private String observacao;

    protected ExemploDoConteudo() {
    }

    public Long getId() {
        return id;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public String getEmIngles() {
        return emIngles;
    }

    public String getEmPortugues() {
        return emPortugues;
    }

    public String getObservacao() {
        return observacao;
    }
}
