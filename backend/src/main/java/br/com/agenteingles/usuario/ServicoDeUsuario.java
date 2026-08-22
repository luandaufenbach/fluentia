package br.com.agenteingles.usuario;

import br.com.agenteingles.modulo.NivelCefr;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicoDeUsuario {

    private final UsuarioRepositorio usuarioRepositorio;

    public ServicoDeUsuario(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    /**
     * Usuario da sessao atual, resolvido a partir da autenticacao.
     *
     * <p>Esta e a unica porta de entrada para "de quem sao estes dados". Nenhum
     * endpoint recebe identificador de usuario do cliente: todo servico abaixo daqui
     * recebe o usuario ja resolvido. E o que impede o ataque mais banal de todos —
     * trocar o id na URL e ler o progresso de outra pessoa.
     *
     * <p>A conta e reconferida no banco a cada requisicao, e nao lida da sessao. Uma
     * conta desativada perde o acesso na requisicao seguinte, sem esperar a sessao
     * expirar.
     */
    @Transactional(readOnly = true)
    public Usuario usuarioAtual() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao == null || !autenticacao.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("Nenhum usuario autenticado.");
        }

        return usuarioRepositorio.buscarPorEmail(autenticacao.getName())
                .filter(Usuario::podeAutenticar)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException(
                        "Sessao valida para uma conta que nao pode mais autenticar."));
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
