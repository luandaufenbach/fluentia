package br.com.agenteingles.tema;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Temas disponiveis. O tema e filtro/contexto do desafio, nao o curriculo. */
@RestController
@RequestMapping("/api/temas")
public class TemaController {

    private final TemaRepositorio temaRepositorio;

    public TemaController(TemaRepositorio temaRepositorio) {
        this.temaRepositorio = temaRepositorio;
    }

    public record TemaResposta(Long id, String codigo, String nome, String descricao) {
    }

    @GetMapping
    public List<TemaResposta> listar() {
        return temaRepositorio.listarOrdenadosPorNome().stream()
                .map(tema -> new TemaResposta(tema.getId(), tema.getCodigo(), tema.getNome(), tema.getDescricao()))
                .toList();
    }
}
