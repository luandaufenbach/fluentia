package br.com.agenteingles.seguranca;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Sessoes vivas por conta, para derrubar as anteriores quando a mesma conta entra
 * de novo.
 *
 * <p>Escrito a mao depois de duas tentativas frustradas. A primeira foi apenas
 * configurar {@code maximumSessions(1)}: nao teve efeito nenhum, porque a estrategia
 * de concorrencia do Spring roda dentro do filtro de autenticacao e a autenticacao
 * aqui acontece em endpoint proprio. A segunda foi usar o {@code SessionRegistry} e
 * marcar as sessoes anteriores como expiradas: o registro passou a encontra-las
 * corretamente, mas a sessao antiga continuava respondendo 200 — a expiracao so vale
 * se o filtro de concorrencia estiver na cadeia e agindo, o que nao se confirmou.
 *
 * <p>Guardar a referencia da sessao e invalida-la diretamente nao depende de nenhuma
 * dessas condicoes: a invalidacao e do proprio container, e vale na requisicao
 * seguinte, sem intermediario.
 *
 * <p><b>Limite conhecido:</b> o mapa vive na memoria deste processo. Com mais de uma
 * instancia da aplicacao, cada uma so derruba as sessoes que ela mesma abriu. A
 * solucao para varias instancias e sessao compartilhada (Spring Session com Redis),
 * que traz junto o armazenamento fora do processo.
 */
@Component
public class SessoesAtivas implements HttpSessionListener {

    private static final Logger log = LoggerFactory.getLogger(SessoesAtivas.class);

    private final Map<String, Set<HttpSession>> porConta = new ConcurrentHashMap<>();

    /** Vincula a sessao recem-aberta a conta. */
    public void registrar(String email, HttpSession sessao) {
        porConta.computeIfAbsent(email, chave -> ConcurrentHashMap.newKeySet()).add(sessao);
    }

    /**
     * Invalida toda sessao anterior desta conta.
     *
     * @return quantas foram derrubadas
     */
    public int derrubarSessoesDe(String email, String idDaSessaoAtual) {
        Set<HttpSession> sessoes = porConta.get(email);
        if (sessoes == null) {
            return 0;
        }

        int derrubadas = 0;
        for (HttpSession sessao : Set.copyOf(sessoes)) {
            try {
                if (sessao.getId().equals(idDaSessaoAtual)) {
                    continue;
                }
                sessao.invalidate();
                derrubadas++;
            } catch (IllegalStateException jaInvalidada) {
                // Corrida com a expiracao natural: o resultado desejado ja aconteceu.
            } finally {
                sessoes.remove(sessao);
            }
        }

        if (derrubadas > 0) {
            log.info("{} sessao(oes) anterior(es) derrubada(s) por novo acesso da mesma conta", derrubadas);
        }
        return derrubadas;
    }

    public void esquecer(String email, HttpSession sessao) {
        Set<HttpSession> sessoes = porConta.get(email);
        if (sessoes != null) {
            sessoes.remove(sessao);
        }
    }

    /** Sessao expirada ou invalidada pelo container sai do mapa sozinha. */
    @Override
    public void sessionDestroyed(HttpSessionEvent evento) {
        porConta.values().forEach(sessoes -> sessoes.remove(evento.getSession()));
        porConta.entrySet().removeIf(entrada -> entrada.getValue().isEmpty());
    }
}
