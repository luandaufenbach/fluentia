package br.com.agenteingles.tema;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TemaRepositorio extends JpaRepository<Tema, Long> {

    @Query("select t from Tema t where t.codigo = :codigo")
    Optional<Tema> buscarPorCodigo(@Param("codigo") String codigo);

    @Query("select t from Tema t order by t.nome")
    List<Tema> listarOrdenadosPorNome();
}
