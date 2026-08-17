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
            NivelCefr nivelEstimado) {
    }

    public record PreferenciasRequisicao(
            ObjetivoDoUsuario objetivo,
            @Min(value = 5, message = "o ritmo minimo e de 5 minutos por dia")
            @Max(value = 240, message = "o ritmo maximo e de 240 minutos por dia")
            Integer minutosPorDia,
            TipoDeCorrecao tipoDeCorrecao,
            NivelCefr nivelEstimado) {
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
                requisicao.nivelEstimado()));
    }

    private UsuarioResposta converter(Usuario usuario) {
        return new UsuarioResposta(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getObjetivo(),
                usuario.getMinutosPorDia(),
                usuario.getTipoDeCorrecao(),
                usuario.getNivelEstimado());
    }
}
