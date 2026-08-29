package br.com.agenteingles.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Painel do administrador.
 *
 * <p>A anotacao de papel fica aqui <b>alem</b> da regra na cadeia de seguranca, e a
 * duplicacao e intencional. A regra da cadeia protege pelo caminho da URL; esta protege
 * pelo metodo. Um endpoint novo neste controller nasce protegido mesmo que alguem
 * esqueca de acrescenta-lo na lista de rotas — e esse esquecimento e exatamente o tipo
 * de coisa que passa numa revisao.
 *
 * <p>Os dados aqui sao de todas as contas: e-mail, quando entrou pela ultima vez,
 * quanto gastou. Nao ha caminho pela interface para virar administrador, de proposito.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class PainelController {

    private final ServicoDoPainel servicoDoPainel;

    public PainelController(ServicoDoPainel servicoDoPainel) {
        this.servicoDoPainel = servicoDoPainel;
    }

    @GetMapping("/painel")
    public PainelDoAdministrador painel() {
        return servicoDoPainel.montar();
    }
}
