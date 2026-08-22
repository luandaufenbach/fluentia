package br.com.agenteingles.seguranca;

import br.com.agenteingles.usuario.Usuario;
import br.com.agenteingles.usuario.UsuarioRepositorio;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Grava o que precisa sobreviver a uma tentativa recusada.
 *
 * <p>Esta classe existe por um motivo especifico, e ele custou um bug de seguranca
 * silencioso: recusar a credencial lanca excecao, excecao desfaz a transacao, e a
 * transacao desfeita levava junto <b>o incremento do contador de falhas</b> e <b>o
 * registro de auditoria da propria falha</b>. O resultado era um bloqueio que nunca
 * disparava e uma trilha que so guardava os acessos bem-sucedidos — justamente os
 * que menos interessam numa investigacao.
 *
 * <p>{@code REQUIRES_NEW} resolve, mas <b>so em bean separado</b>: anotacao de
 * transacao nao vale em chamada de um metodo para outro da mesma classe, porque a
 * chamada nao passa pelo proxy que aplica a anotacao. Foi assim que o defeito
 * apareceu na primeira versao, com a anotacao presente e sem efeito nenhum.
 */
@Component
public class RegistroDeSeguranca {

    private final UsuarioRepositorio usuarioRepositorio;
    private final EventoDeAutenticacaoRepositorio eventoRepositorio;

    public RegistroDeSeguranca(UsuarioRepositorio usuarioRepositorio,
                               EventoDeAutenticacaoRepositorio eventoRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.eventoRepositorio = eventoRepositorio;
    }

    /**
     * Evento que precisa sobreviver ao rollback de quem o originou: tentativa recusada,
     * bloqueio, saida. Transacao propria.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarEvento(Long usuarioId, String email, TipoDeEventoDeAutenticacao tipo,
                                String origem, String detalhe) {
        eventoRepositorio.save(new EventoDeAutenticacao(usuarioId, email, tipo, origem, detalhe));
    }

    /**
     * Evento que pertence a mesma transacao de quem o originou — hoje, so o cadastro.
     *
     * <p>Aqui {@code REQUIRES_NEW} seria errado por dois motivos. O tecnico: a transacao
     * nova nao enxerga o usuario ainda nao comitado, e a chave estrangeira do evento
     * falha. O semantico, que importa mais: se a criacao da conta for desfeita, o
     * registro de que ela foi criada tem que ser desfeito junto — auditoria de um
     * cadastro que nao existe e informacao falsa, e auditoria falsa e pior que nenhuma.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void registrarEventoJunto(Long usuarioId, String email, TipoDeEventoDeAutenticacao tipo,
                                     String origem, String detalhe) {
        eventoRepositorio.save(new EventoDeAutenticacao(usuarioId, email, tipo, origem, detalhe));
    }

    /**
     * Incrementa a falha e devolve se a conta ficou bloqueada.
     *
     * <p>O usuario e recarregado nesta transacao em vez de reaproveitar a instancia de
     * fora: a de fora pertence a transacao que sera desfeita.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean registrarFalhaEVerificarBloqueio(Long usuarioId,
                                                    int tentativasAteBloquear,
                                                    LocalDateTime bloqueioAte) {
        Usuario usuario = usuarioRepositorio.findById(usuarioId).orElse(null);
        if (usuario == null) {
            return false;
        }
        usuario.registrarFalhaDeLogin(tentativasAteBloquear, bloqueioAte);
        usuarioRepositorio.save(usuario);
        return usuario.estaBloqueado(LocalDateTime.now());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarAcessoBemSucedido(Long usuarioId, LocalDateTime agora) {
        usuarioRepositorio.findById(usuarioId).ifPresent(usuario -> {
            usuario.registrarAcessoBemSucedido(agora);
            usuarioRepositorio.save(usuario);
        });
    }
}
