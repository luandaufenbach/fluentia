package br.com.agenteingles.custo;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Uma chamada a API, com o que ela consumiu e custou.
 *
 * <p>O usuario e um id solto, e nao uma associacao: este registro e escrito numa
 * transacao propria, e carregar a entidade so para gravar o vinculo obrigaria a
 * busca-la de novo.
 */
@Entity
@Table(name = "consumo_de_api")
public class ConsumoDeApi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nulo para rotina sem dono, como a geracao de conteudo. */
    @Column(name = "usuario_id")
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_de_chamada", nullable = false, length = 40)
    private TipoDeChamada tipoDeChamada;

    @Column(nullable = false, length = 60)
    private String modelo;

    @Column(name = "tokens_de_entrada", nullable = false)
    private int tokensDeEntrada;

    @Column(name = "tokens_de_saida", nullable = false)
    private int tokensDeSaida;

    /** Quantos desafios o lote rendeu. Zero quando a resposta veio ilegivel: o gasto aconteceu igual. */
    @Column(name = "itens_produzidos", nullable = false)
    private int itensProduzidos;

    /** Nulo quando o modelo nao tem preco configurado — zero mentiria no total. */
    @Column(name = "custo_usd", precision = 12, scale = 6)
    private BigDecimal custoUsd;

    @Column(name = "ocorrido_em", nullable = false)
    private LocalDateTime ocorridoEm = LocalDateTime.now();

    protected ConsumoDeApi() {
    }

    public ConsumoDeApi(Long usuarioId,
                        TipoDeChamada tipoDeChamada,
                        String modelo,
                        int tokensDeEntrada,
                        int tokensDeSaida,
                        int itensProduzidos,
                        BigDecimal custoUsd) {
        this.usuarioId = usuarioId;
        this.tipoDeChamada = tipoDeChamada;
        this.modelo = modelo;
        this.tokensDeEntrada = tokensDeEntrada;
        this.tokensDeSaida = tokensDeSaida;
        this.itensProduzidos = itensProduzidos;
        this.custoUsd = custoUsd;
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public TipoDeChamada getTipoDeChamada() {
        return tipoDeChamada;
    }

    public String getModelo() {
        return modelo;
    }

    public int getTokensDeEntrada() {
        return tokensDeEntrada;
    }

    public int getTokensDeSaida() {
        return tokensDeSaida;
    }

    public int getItensProduzidos() {
        return itensProduzidos;
    }

    public BigDecimal getCustoUsd() {
        return custoUsd;
    }

    public LocalDateTime getOcorridoEm() {
        return ocorridoEm;
    }
}
