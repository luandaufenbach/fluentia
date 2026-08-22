package br.com.agenteingles.desafio;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DesafioRepositorio extends JpaRepository<Desafio, Long> {

    /** Desafio ainda em aberto: evita gerar um novo enquanto o anterior nao foi respondido. */
    @Query("""
            select d from Desafio d
            where d.usuario.id = :usuarioId
              and d.status = br.com.agenteingles.desafio.StatusDoDesafio.AGUARDANDO_RESPOSTA
            order by d.criadoEm desc
            """)
    List<Desafio> listarEmAberto(@Param("usuarioId") Long usuarioId, Limit limite);

    /**
     * Desafios ja gerados e ainda nao apresentados neste modulo.
     *
     * <p>Enquanto a fila tiver item, praticar de novo nao custa chamada de API nenhuma —
     * e o aluno recebe o desafio sem espera de rede.
     */
    @Query("""
            select d from Desafio d
            where d.usuario.id = :usuarioId and d.modulo.id = :moduloId
              and d.status = br.com.agenteingles.desafio.StatusDoDesafio.NA_FILA
            order by d.criadoEm asc
            """)
    List<Desafio> listarNaFila(@Param("usuarioId") Long usuarioId,
                               @Param("moduloId") Long moduloId,
                               Limit limite);

    /** Enunciados recentes do modulo, usados para nao repetir cena nem construcao. */
    @Query("""
            select d.enunciado from Desafio d
            where d.usuario.id = :usuarioId and d.modulo.id = :moduloId
            order by d.criadoEm desc
            """)
    List<String> listarEnunciadosRecentes(@Param("usuarioId") Long usuarioId,
                                          @Param("moduloId") Long moduloId,
                                          Limit limite);

    @Query("""
            select d from Desafio d
            where d.usuario.id = :usuarioId
            order by d.criadoEm desc
            """)
    List<Desafio> listarHistorico(@Param("usuarioId") Long usuarioId, Limit limite);

    @Query("select d from Desafio d where d.id = :desafioId and d.usuario.id = :usuarioId")
    Optional<Desafio> buscarDoUsuario(@Param("desafioId") Long desafioId,
                                      @Param("usuarioId") Long usuarioId);
}
