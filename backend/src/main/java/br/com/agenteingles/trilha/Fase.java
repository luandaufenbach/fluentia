package br.com.agenteingles.trilha;

import jakarta.persistence.*;

/**
 * Etapa da trilha, do ponto de vista do aluno.
 *
 * <p>O nivel CEFR continua sendo a verdade tecnica do conteudo; a fase e a traducao
 * disso em promessa concreta. "A2" nao motiva ninguem — "contar como foi o seu fim
 * de semana" motiva.
 */
@Entity
@Table(name = "fase")
public class Fase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60, unique = true)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nome;

    /** O que o aluno vai saber fazer ao terminar a fase. */
    @Column(nullable = false, columnDefinition = "text")
    private String promessa;

    /** A habilidade concreta que marca o fim da fase. */
    @Column(nullable = false, columnDefinition = "text")
    private String marco;

    @Column(nullable = false)
    private Integer ordem;

    protected Fase() {
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public String getPromessa() {
        return promessa;
    }

    public String getMarco() {
        return marco;
    }

    public Integer getOrdem() {
        return ordem;
    }
}
