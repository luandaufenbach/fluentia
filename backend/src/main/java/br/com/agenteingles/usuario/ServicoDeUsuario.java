package br.com.agenteingles.usuario;

import br.com.agenteingles.agente.PropriedadesDoAgente;
import br.com.agenteingles.comum.RecursoNaoEncontradoException;
import br.com.agenteingles.modulo.NivelCefr;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicoDeUsuario {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PropriedadesDoAgente propriedades;

    public ServicoDeUsuario(UsuarioRepositorio usuarioRepositorio, PropriedadesDoAgente propriedades) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.propriedades = propriedades;
    }

    /**
     * Usuario da sessao atual. Enquanto a autenticacao nao entra, resolve o usuario de
     * desenvolvimento semeado nas migrations — os servicos ja recebem o usuario resolvido,
     * entao ligar login depois nao muda a assinatura de nada abaixo daqui.
     */
    @Transactional(readOnly = true)
    public Usuario usuarioAtual() {
        return usuarioRepositorio.buscarPorEmail(propriedades.emailDoUsuarioPadrao())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuario de desenvolvimento nao encontrado: " + propriedades.emailDoUsuarioPadrao()));
    }

    @Transactional
    public Usuario atualizarPreferencias(ObjetivoDoUsuario objetivo,
                                         Integer minutosPorDia,
                                         TipoDeCorrecao tipoDeCorrecao,
                                         NivelCefr nivelEstimado) {
        Usuario usuario = usuarioAtual();
        if (objetivo != null) {
            usuario.setObjetivo(objetivo);
        }
        if (minutosPorDia != null) {
            usuario.setMinutosPorDia(minutosPorDia);
        }
        if (tipoDeCorrecao != null) {
            usuario.setTipoDeCorrecao(tipoDeCorrecao);
        }
        if (nivelEstimado != null) {
            usuario.setNivelEstimado(nivelEstimado);
        }
        return usuarioRepositorio.save(usuario);
    }
}
