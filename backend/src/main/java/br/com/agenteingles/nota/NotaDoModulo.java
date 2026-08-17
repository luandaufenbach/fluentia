package br.com.agenteingles.nota;

import br.com.agenteingles.modulo.Modulo;
import br.com.agenteingles.usuario.Usuario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Nota de dominio de um usuario num modulo. E o dado central do produto:
 * alimenta o orquestrador na escolha do proximo desafio e o desbloqueio de modulos.
 */
@Entity
@Table(name = "nota_do_modulo")
public class NotaDoModulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modulo_id", nullable = false)
    private Modulo modulo;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal nota;

    @Column(name = "quantidade_de_praticas", nullable = false)
    private Integer quantidadeDePraticas = 0;

    @Column(name = "data_da_ultima_pratica")
    private LocalDateTime dataDaUltimaPratica;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm = LocalDateTime.now();

    protected NotaDoModulo() {
    }

    public NotaDoModulo(Usuario usuario, Modulo modulo, BigDecimal nota) {
        this.usuario = usuario;
        this.modulo = modulo;
        this.nota = nota;
    }

    /** Registra o resultado de mais uma pratica neste modulo. */
    public void registrarPratica(BigDecimal notaRecalculada, LocalDateTime momentoDaPratica) {
        this.nota = notaRecalculada;
        this.quantidadeDePraticas = this.quantidadeDePraticas + 1;
        this.dataDaUltimaPratica = momentoDaPratica;
        this.atualizadoEm = momentoDaPratica;
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

    public BigDecimal getNota() {
        return nota;
    }

    public Integer getQuantidadeDePraticas() {
        return quantidadeDePraticas;
    }

    public LocalDateTime getDataDaUltimaPratica() {
        return dataDaUltimaPratica;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}
