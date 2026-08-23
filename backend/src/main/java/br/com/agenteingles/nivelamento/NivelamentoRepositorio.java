package br.com.agenteingles.nivelamento;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NivelamentoRepositorio extends JpaRepository<Nivelamento, Long> {

    @Query("""
            select n from Nivelamento n
            where n.usuario.id = :usuarioId
              and n.status = br.com.agenteingles.nivelamento.StatusDoNivelamento.EM_ANDAMENTO
            """)
    Optional<Nivelamento> emAndamento(@Param("usuarioId") Long usuarioId);

    /** Busca sempre com o dono junto: nivelamento de outra conta nao pode ser alcancado por id. */
    @Query("select n from Nivelamento n where n.id = :id and n.usuario.id = :usuarioId")
    Optional<Nivelamento> buscarDoUsuario(@Param("id") Long id, @Param("usuarioId") Long usuarioId);

    @Query("""
            select count(n) from Nivelamento n
            where n.usuario.id = :usuarioId
              and n.status = br.com.agenteingles.nivelamento.StatusDoNivelamento.CONCLUIDO
            """)
    long concluidosDoUsuario(@Param("usuarioId") Long usuarioId);
}
