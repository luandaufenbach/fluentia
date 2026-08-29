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

    /**
     * Tema que o aluno prefere nas cenas. Nulo significa "sem preferencia": aí quem
     * decide e o objetivo, como sempre foi.
     *
     * <p>Guardado como id solto, e nao como relacionamento JPA, porque quem precisa do
     * tema inteiro e o orquestrador — e ele ja tem o repositorio de temas em maos. Um
     * {@code ManyToOne} aqui traria carregamento preguicoso para dentro de uma entidade
     * lida em todo request, com {@code open-in-view} desligado.
     */
    @Column(name = "tema_preferido_id")
    private Long temaPreferidoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_de_correcao", nullable = false, length = 20)
    private TipoDeCorrecao tipoDeCorrecao = TipoDeCorrecao.DETALHADA;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_estimado", length = 2)
    private NivelCefr nivelEstimado;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    /**
     * Hash da senha, com o prefixo do algoritmo ({@code {bcrypt}$2a$12$...}).
     *
     * <p>Nulo em conta que nao pode autenticar. O campo e privado e nao tem getter
     * publico de proposito: quem precisa comparar e o verificador de senha, que
     * recebe o valor por {@link #getSenhaHashParaVerificacao()}. Sem getter comum,
     * nenhum serializador de resposta o alcanca por acidente.
     */
    @Column(name = "senha_hash", length = 100)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PapelDoUsuario papel = PapelDoUsuario.ALUNO;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "tentativas_falhas", nullable = false)
    private int tentativasFalhas = 0;

    @Column(name = "bloqueado_ate")
    private LocalDateTime bloqueadoAte;

    @Column(name = "ultimo_acesso_em")
    private LocalDateTime ultimoAcessoEm;

    protected Usuario() {
    }

    /**
     * Cria uma conta. O hash chega pronto: esta classe nunca ve a senha em texto puro,
     * nem para guardar, nem para comparar.
     */
    public Usuario(String nome, String email, String senhaHash) {
        this.nome = nome;
        this.email = email;
        this.senhaHash = senhaHash;
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

    public Long getTemaPreferidoId() {
        return temaPreferidoId;
    }

    public void setTemaPreferidoId(Long temaPreferidoId) {
        this.temaPreferidoId = temaPreferidoId;
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

    /** Nome deliberadamente explicito: sinaliza que o valor nao pode sair daqui. */
    public String getSenhaHashParaVerificacao() {
        return senhaHash;
    }

    public PapelDoUsuario getPapel() {
        return papel;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public int getTentativasFalhas() {
        return tentativasFalhas;
    }

    public LocalDateTime getBloqueadoAte() {
        return bloqueadoAte;
    }

    public LocalDateTime getUltimoAcessoEm() {
        return ultimoAcessoEm;
    }

    /** Conta sem hash nunca autentica: e o estado do usuario semeado nas migrations. */
    public boolean podeAutenticar() {
        return ativo && senhaHash != null;
    }

    public boolean estaBloqueado(LocalDateTime agora) {
        return bloqueadoAte != null && bloqueadoAte.isAfter(agora);
    }

    /**
     * Registra uma falha e bloqueia ao atingir o limite.
     *
     * <p>O bloqueio e temporario e nao permanente de proposito: bloqueio definitivo
     * transforma tentativa de invasao em negacao de servico contra o dono da conta,
     * que passa a depender de suporte para voltar.
     */
    public void registrarFalhaDeLogin(int limiteDeTentativas, LocalDateTime bloqueioAte) {
        this.tentativasFalhas++;
        if (this.tentativasFalhas >= limiteDeTentativas) {
            this.bloqueadoAte = bloqueioAte;
        }
    }

    public void registrarAcessoBemSucedido(LocalDateTime agora) {
        this.tentativasFalhas = 0;
        this.bloqueadoAte = null;
        this.ultimoAcessoEm = agora;
    }

    public void trocarSenha(String novoHash) {
        this.senhaHash = novoHash;
        this.tentativasFalhas = 0;
        this.bloqueadoAte = null;
    }
}
