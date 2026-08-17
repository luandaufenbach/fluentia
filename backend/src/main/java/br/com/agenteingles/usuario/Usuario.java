package br.com.agenteingles.usuario;

import br.com.agenteingles.modulo.NivelCefr;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 180, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ObjetivoDoUsuario objetivo = ObjetivoDoUsuario.CONVERSACAO_GERAL;

    @Column(name = "minutos_por_dia", nullable = false)
    private Integer minutosPorDia = 15;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_de_correcao", nullable = false, length = 20)
    private TipoDeCorrecao tipoDeCorrecao = TipoDeCorrecao.DETALHADA;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_estimado", length = 2)
    private NivelCefr nivelEstimado;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    protected Usuario() {
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ObjetivoDoUsuario getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(ObjetivoDoUsuario objetivo) {
        this.objetivo = objetivo;
    }

    public Integer getMinutosPorDia() {
        return minutosPorDia;
    }

    public void setMinutosPorDia(Integer minutosPorDia) {
        this.minutosPorDia = minutosPorDia;
    }

    public TipoDeCorrecao getTipoDeCorrecao() {
        return tipoDeCorrecao;
    }

    public void setTipoDeCorrecao(TipoDeCorrecao tipoDeCorrecao) {
        this.tipoDeCorrecao = tipoDeCorrecao;
    }

    public NivelCefr getNivelEstimado() {
        return nivelEstimado;
    }

    public void setNivelEstimado(NivelCefr nivelEstimado) {
        this.nivelEstimado = nivelEstimado;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
