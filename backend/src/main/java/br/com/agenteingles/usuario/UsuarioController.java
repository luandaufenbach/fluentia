package br.com.agenteingles.usuario;

import br.com.agenteingles.modulo.NivelCefr;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Perfil e preferencias do usuario: objetivo, ritmo e tipo de correcao. */
@RestController
@RequestMapping("/api/usuario")
public class UsuarioController {

    private final ServicoDeUsuario servicoDeUsuario;

    public UsuarioController(ServicoDeUsuario servicoDeUsuario) {
        this.servicoDeUsuario = servicoDeUsuario;
    }

    public record UsuarioResposta(
            Long id,
            String nome,
            String email,
            ObjetivoDoUsuario objetivo,
            Integer minutosPorDia,
            TipoDeCorrecao tipoDeCorrecao,
            NivelCefr nivelEstimado,
            /** Nulo = sem preferencia; quem decide a cena e o objetivo. */
            Long temaPreferidoId,
            /**
             * Se esta conta e administradora.
             *
             * <p>Um booleano, e nao o papel. A resposta do login segue sem nada disso —
             * la o dado nao tem uso e so aumentaria a superficie. Aqui tem: e o que
             * decide se a aba do painel aparece.
             *
             * <p>Contar isso a propria pessoa nao vaza nada: ela ja sabe o que e. E a
             * aba e conveniencia de interface, nao protecao — quem barra o acesso e o
             * papel exigido no servidor.
             */
            boolean ehAdministrador) {
    }

    /**
     * @param minutosPorDia a faixa acompanha o que de fato muda a meta. Ela sai de
     *        {@code minutos / 3}, limitada entre 3 e 20 desafios: abaixo de 9 minutos
     *        e acima de 60 o resultado para de mudar. Aceitar 240 era prometer um
     *        ajuste que nao existia — quem escolhesse 240 recebia o mesmo de 60 e nao
     *        tinha como perceber.
     */
    public record PreferenciasRequisicao(
            ObjetivoDoUsuario objetivo,
            @Min(value = 9, message = "o ritmo minimo e de 9 minutos por dia")
            @Max(value = 60, message = "o ritmo maximo e de 60 minutos por dia")
            Integer minutosPorDia,
            TipoDeCorrecao tipoDeCorrecao,
            NivelCefr nivelEstimado,
            Long temaPreferidoId) {
    }

    @GetMapping
    public UsuarioResposta perfil() {
        return converter(servicoDeUsuario.usuarioAtual());
    }

    @PutMapping("/preferencias")
    public UsuarioResposta atualizarPreferencias(@Valid @RequestBody PreferenciasRequisicao requisicao) {
        return converter(servicoDeUsuario.atualizarPreferencias(
                requisicao.objetivo(),
                requisicao.minutosPorDia(),
                requisicao.tipoDeCorrecao(),
                requisicao.nivelEstimado(),
                requisicao.temaPreferidoId()));
    }

    private UsuarioResposta converter(Usuario usuario) {
        return new UsuarioResposta(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getObjetivo(),
                usuario.getMinutosPorDia(),
                usuario.getTipoDeCorrecao(),
                usuario.getNivelEstimado(),
                usuario.getTemaPreferidoId(),
                usuario.getPapel() == PapelDoUsuario.ADMINISTRADOR);
    }
}
