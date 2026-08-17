package br.com.agenteingles.modulo;

import br.com.agenteingles.nota.FaixaDeNota;
import br.com.agenteingles.usuario.ServicoDeUsuario;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Lista de modulos do curriculo com a nota e a faixa de cor do usuario. */
@RestController
@RequestMapping("/api/modulos")
public class ModuloController {

    private final ServicoDeModulo servicoDeModulo;
    private final ServicoDeUsuario servicoDeUsuario;

    public ModuloController(ServicoDeModulo servicoDeModulo, ServicoDeUsuario servicoDeUsuario) {
        this.servicoDeModulo = servicoDeModulo;
        this.servicoDeUsuario = servicoDeUsuario;
    }

    /**
     * @param nota {@code null} quando o modulo nunca foi praticado — o frontend mostra "novo"
     */
    public record ModuloNaListaResposta(
            Long id,
            String codigo,
            String nome,
            String descricao,
            NivelCefr nivel,
            BigDecimal nota,
            FaixaDeNota faixa,
            boolean liberado,
            List<String> preRequisitosPendentes,
            LocalDateTime dataDaUltimaPratica,
            int quantidadeDePraticas) {
    }

    public record NivelComModulosResposta(NivelCefr nivel, List<ModuloNaListaResposta> modulos) {
    }

    /** Modulos agrupados por nivel CEFR, na ordem de progressao do curriculo. */
    @GetMapping
    public List<NivelComModulosResposta> listar() {
        List<SituacaoDoModulo> situacoes =
                servicoDeModulo.situacaoDeTodosOsModulos(servicoDeUsuario.usuarioAtual());

        Map<NivelCefr, List<ModuloNaListaResposta>> porNivel = new LinkedHashMap<>();
        for (NivelCefr nivel : NivelCefr.values()) {
            porNivel.put(nivel, new java.util.ArrayList<>());
        }
        for (SituacaoDoModulo situacao : situacoes) {
            porNivel.get(situacao.modulo().getNivelCefr()).add(converter(situacao));
        }

        return porNivel.entrySet().stream()
                .filter(entrada -> !entrada.getValue().isEmpty())
                .map(entrada -> new NivelComModulosResposta(entrada.getKey(), entrada.getValue()))
                .toList();
    }

    public static ModuloNaListaResposta converter(SituacaoDoModulo situacao) {
        return new ModuloNaListaResposta(
                situacao.modulo().getId(),
                situacao.modulo().getCodigo(),
                situacao.modulo().getNome(),
                situacao.modulo().getDescricao(),
                situacao.modulo().getNivelCefr(),
                situacao.nota(),
                situacao.faixa(),
                situacao.liberado(),
                situacao.preRequisitosPendentes(),
                situacao.dataDaUltimaPratica(),
                situacao.quantidadeDePraticas());
    }
}
