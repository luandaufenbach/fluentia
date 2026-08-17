package br.com.agenteingles.conteudo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConteudoDoModuloRepositorio extends JpaRepository<ConteudoDoModulo, Long> {

    /**
     * Exemplos e erros comuns vem em consultas separadas de proposito.
     *
     * <p>Buscar as duas colecoes numa consulta so nao e permitido pelo Hibernate
     * ({@code MultipleBagFetchException}), e mesmo se fosse traria o produto cartesiano
     * das duas — 6 exemplos e 4 erros virariam 24 linhas. Duas consultas na mesma
     * transacao preenchem a mesma instancia e mantem a ordem de cada colecao.
     */
    @Query("""
            select c from ConteudoDoModulo c
            left join fetch c.exemplos
            where c.modulo.codigo = :codigo
            """)
    Optional<ConteudoDoModulo> buscarComExemplos(@Param("codigo") String codigo);

    @Query("""
            select c from ConteudoDoModulo c
            left join fetch c.errosComuns
            where c.modulo.codigo = :codigo
            """)
    Optional<ConteudoDoModulo> buscarComErrosComuns(@Param("codigo") String codigo);

    /** Codigos que ja tem conteudo — usado para nao regerar o que existe. */
    @Query("select c.modulo.codigo from ConteudoDoModulo c")
    List<String> codigosDosModulosComConteudo();
}
