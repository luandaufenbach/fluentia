package br.com.agenteingles.conteudo;

import br.com.agenteingles.modulo.Modulo;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * O que o modulo ensina, antes de cobrar. E por modulo e nao por desafio porque a
 * explicacao do conceito e estavel: o desafio muda toda vez, a regra do "to be" nao.
 */
@Entity
@Table(name = "conteudo_do_modulo")
public class ConteudoDoModulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modulo_id", nullable = false, unique = true)
    private Modulo modulo;

    @Column(nullable = false, columnDefinition = "text")
    private String resumo;

    @Column(nullable = false, columnDefinition = "text")
    private String explicacao;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @OneToMany(mappedBy = "conteudo", fetch = FetchType.LAZY)
    @OrderBy("ordem")
    private List<ExemploDoConteudo> exemplos = new ArrayList<>();

    @OneToMany(mappedBy = "conteudo", fetch = FetchType.LAZY)
    @OrderBy("ordem")
    private List<ErroComumDoConteudo> errosComuns = new ArrayList<>();

    protected ConteudoDoModulo() {
    }

    public Long getId() {
        return id;
    }

    public Modulo getModulo() {
        return modulo;
    }

    public String getResumo() {
        return resumo;
    }

    public String getExplicacao() {
        return explicacao;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public List<ExemploDoConteudo> getExemplos() {
        return exemplos;
    }

    public List<ErroComumDoConteudo> getErrosComuns() {
        return errosComuns;
    }
}
