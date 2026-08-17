package br.com.agenteingles.modulo;

import jakarta.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Conceito avaliado — a unidade que recebe nota. O tema e um eixo separado:
 * ele da a cena do desafio, enquanto o modulo e o que esta sendo medido.
 */
@Entity
@Table(name = "modulo")
public class Modulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60, unique = true)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_cefr", nullable = false, length = 2)
    private NivelCefr nivelCefr;

    @Column(nullable = false, columnDefinition = "text")
    private String descricao;

    @Column(nullable = false)
    private Integer ordem;

    /** Modulos que precisam de nota razoavel antes deste ser liberado. */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "pre_requisito_modulo",
            joinColumns = @JoinColumn(name = "modulo_id"),
            inverseJoinColumns = @JoinColumn(name = "pre_requisito_id"))
    private Set<Modulo> preRequisitos = new LinkedHashSet<>();

    protected Modulo() {
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

    public NivelCefr getNivelCefr() {
        return nivelCefr;
    }

    public String getDescricao() {
        return descricao;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public Set<Modulo> getPreRequisitos() {
        return preRequisitos;
    }
}
