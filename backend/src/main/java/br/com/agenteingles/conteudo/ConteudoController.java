package br.com.agenteingles.conteudo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Conteudo de ensino do modulo — o que o aluno le antes de praticar. */
@RestController
@RequestMapping("/api/modulos/{codigo}/conteudo")
public class ConteudoController {

    private final ServicoDeConteudo servicoDeConteudo;

    public ConteudoController(ServicoDeConteudo servicoDeConteudo) {
        this.servicoDeConteudo = servicoDeConteudo;
    }

    @GetMapping
    public ConteudoDoModuloResposta buscar(@PathVariable String codigo) {
        return servicoDeConteudo.buscarPorCodigoDoModulo(codigo);
    }
}
