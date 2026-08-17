package br.com.agenteingles.conteudo;

import br.com.agenteingles.comum.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Entrega o conteudo de ensino de um modulo. */
@Service
public class ServicoDeConteudo {

    private final ConteudoDoModuloRepositorio conteudoRepositorio;

    public ServicoDeConteudo(ConteudoDoModuloRepositorio conteudoRepositorio) {
        this.conteudoRepositorio = conteudoRepositorio;
    }

    @Transactional(readOnly = true)
    public ConteudoDoModuloResposta buscarPorCodigoDoModulo(String codigo) {
        ConteudoDoModulo conteudo = conteudoRepositorio.buscarComExemplos(codigo)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Este módulo ainda não tem conteúdo de ensino."));

        // Segunda consulta na mesma transacao: preenche os erros comuns na mesma
        // instancia ja carregada, sem cruzar as duas colecoes numa consulta so.
        conteudoRepositorio.buscarComErrosComuns(codigo);

        return ConteudoDoModuloResposta.de(conteudo);
    }
}
