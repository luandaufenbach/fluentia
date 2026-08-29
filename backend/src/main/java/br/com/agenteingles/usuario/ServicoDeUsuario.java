package br.com.agenteingles.usuario;

import br.com.agenteingles.comum.RecursoNaoEncontradoException;
import br.com.agenteingles.modulo.NivelCefr;
import br.com.agenteingles.tema.TemaRepositorio;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicoDeUsuario {

    private final UsuarioRepositorio usuarioRepositorio;
    private final TemaRepositorio temaRepositorio;

    public ServicoDeUsuario(UsuarioRepositorio usuarioRepositorio, TemaRepositorio temaRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.temaRepositorio = temaRepositorio;
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
    /**
     * @param temaPreferidoId <b>nulo aqui nao e "nao mexer", e "sem preferencia"</b> —
     *        diferente dos outros campos. Sem essa distincao nao haveria como desfazer
     *        a escolha: quem marcasse um tema uma vez ficaria preso a ele para sempre.
     *        Em compensacao, um PUT parcial que omita o campo limpa a preferencia; e o
     *        comportamento correto de um PUT, que substitui o recurso, e o unico
     *        cliente deste endpoint sempre envia o conjunto inteiro.
     */
    public Usuario atualizarPreferencias(ObjetivoDoUsuario objetivo,
                                         Integer minutosPorDia,
                                         TipoDeCorrecao tipoDeCorrecao,
                                         NivelCefr nivelEstimado,
                                         Long temaPreferidoId) {
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

        // Confere aqui em vez de deixar a chave estrangeira barrar: a violacao de
        // integridade viraria um 409 dizendo "este recurso ja existe", que nao tem
        // nada a ver com o problema e mandaria quem investiga para o lado errado.
        if (temaPreferidoId != null && !temaRepositorio.existsById(temaPreferidoId)) {
            throw new RecursoNaoEncontradoException("Tema " + temaPreferidoId + " nao existe.");
        }
        usuario.setTemaPreferidoId(temaPreferidoId);

        return usuarioRepositorio.save(usuario);
    }
}
