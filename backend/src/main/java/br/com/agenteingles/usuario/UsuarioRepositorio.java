package br.com.agenteingles.usuario;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    /**
     * Busca pelo e-mail sem diferenciar caixa.
     *
     * <p>O e-mail e a credencial de login, e comparar com caixa faria
     * {@code Joao@x.com} e {@code joao@x.com} virarem contas distintas — a restricao
     * de unicidade nao pegaria, e o mesmo endereco teria duas contas. O indice unico
     * criado na migration tambem e sobre {@code LOWER(email)}, para que a garantia
     * exista no banco e nao so aqui.
     *
     * <p>Os nomes de metodo estao em portugues, entao a consulta e declarada
     * explicitamente: o Spring Data so deriva consultas dos prefixos em ingles.
     */
    @Query("select u from Usuario u where lower(u.email) = lower(:email)")
    Optional<Usuario> buscarPorEmail(@Param("email") String email);

    @Query("select count(u) > 0 from Usuario u where lower(u.email) = lower(:email)")
    boolean existeComEmail(@Param("email") String email);

    /**
     * Trava a linha da conta ate o fim da transacao.
     *
     * <p>Serve para serializar operacoes que so podem existir uma vez por conta. Sem a
     * trava, duas requisicoes simultaneas — duas abas, um duplo clique, o modo estrito
     * do React chamando o efeito duas vezes — passam juntas pela verificacao de "ja
     * existe?" e as duas tentam inserir; a segunda morre no indice unico e vira erro
     * na cara de quem so clicou uma vez.
     *
     * <p>Com a trava a segunda requisicao espera a primeira terminar e simplesmente
     * encontra o que a primeira criou.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.id = :id")
    Optional<Usuario> travar(@Param("id") Long id);
}
