package br.com.agenteingles.custo;

import br.com.agenteingles.usuario.ServicoDeUsuario;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Extrato de consumo da API.
 *
 * <p>Sempre da conta que esta pedindo: o usuario vem do contexto de seguranca, nunca
 * de parametro, entao nao ha como pedir o extrato de outra pessoa trocando um id na URL.
 */
@RestController
@RequestMapping("/api/consumo")
public class ConsumoController {

    private final ServicoDeConsumo servicoDeConsumo;
    private final ServicoDeUsuario servicoDeUsuario;

    public ConsumoController(ServicoDeConsumo servicoDeConsumo, ServicoDeUsuario servicoDeUsuario) {
        this.servicoDeConsumo = servicoDeConsumo;
        this.servicoDeUsuario = servicoDeUsuario;
    }

    @GetMapping
    public ResumoDeConsumo meuConsumo() {
        return servicoDeConsumo.doUsuario(servicoDeUsuario.usuarioAtual());
    }
}
