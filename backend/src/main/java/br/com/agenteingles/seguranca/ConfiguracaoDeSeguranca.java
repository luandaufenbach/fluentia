package br.com.agenteingles.seguranca;

import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Politica de acesso da API.
 *
 * <p>Escolhas que definem a postura de seguranca aqui, e o porque de cada uma:
 *
 * <ul>
 *   <li><b>Sessao em cookie, nao token no navegador.</b> Guardar token em
 *       {@code localStorage} o deixa legivel por qualquer script — uma unica falha de
 *       XSS entrega a credencial. Cookie {@code HttpOnly} nao e alcancavel por
 *       JavaScript, e sessao no servidor pode ser revogada na hora, coisa que token
 *       autocontido nao permite.</li>
 *   <li><b>Negar por padrao.</b> {@code anyRequest().authenticated()} vem por ultimo:
 *       endpoint novo nasce protegido. A lista de exececoes e explicita e curta —
 *       esquecer de proteger uma rota deixa de ser possivel por omissao.</li>
 *   <li><b>CSRF ligado.</b> Com credencial em cookie, o navegador a envia sozinho em
 *       qualquer requisicao para este dominio, inclusive as disparadas por outro site.
 *       O token quebra isso porque o site atacante nao consegue le-lo.</li>
 *   <li><b>Falha responde 401 em JSON.</b> O padrao do Spring redireciona para uma
 *       tela de login que nao existe numa API; o cliente receberia HTML no lugar de
 *       um erro que sabe tratar.</li>
 * </ul>
 */
@Configuration
@EnableMethodSecurity
public class ConfiguracaoDeSeguranca {

    /**
     * Custo do BCrypt. Cada incremento dobra o tempo de calculo — e o que torna
     * ataque de forca bruta caro. 12 fica na faixa de ~250 ms por verificacao no
     * hardware atual: imperceptivel no login, proibitivo em escala.
     */
    private static final int CUSTO_DO_BCRYPT = 12;

    private final PropriedadesDeSeguranca propriedades;

    public ConfiguracaoDeSeguranca(PropriedadesDeSeguranca propriedades) {
        this.propriedades = propriedades;
    }

    /**
     * O hash sai com o prefixo do algoritmo ({@code {bcrypt}...}), e a verificacao le
     * esse prefixo para escolher como conferir. E o que permite trocar de algoritmo
     * no futuro sem invalidar a senha de quem ja se cadastrou: hashes antigos
     * continuam sendo conferidos pelo algoritmo antigo.
     */
    @Bean
    public PasswordEncoder codificadorDeSenha() {
        return new DelegatingPasswordEncoder(
                "bcrypt", Map.of("bcrypt", new BCryptPasswordEncoder(CUSTO_DO_BCRYPT)));
    }

    @Bean
    public SecurityFilterChain cadeiaDeFiltros(HttpSecurity http) throws Exception {
        // Sem o handler explicito, o token so e resolvido sob demanda e o cookie nao
        // chega ao cliente na primeira visita — o primeiro POST falharia.
        CsrfTokenRequestAttributeHandler tratadorDeCsrf = new CsrfTokenRequestAttributeHandler();
        tratadorDeCsrf.setCsrfRequestAttributeName(null);

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf
                        // withHttpOnlyFalse: o SPA precisa LER o token para devolve-lo
                        // no cabecalho. Quem nao pode ler e o site atacante, barrado
                        // pela politica de mesma origem do navegador.
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(tratadorDeCsrf))

                .sessionManagement(sessao -> sessao
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        // Troca o identificador da sessao no login: sem isso, um
                        // identificador plantado antes continuaria valido depois da
                        // autenticacao (fixacao de sessao).
                        // "Uma sessao por conta" NAO e configurado aqui: maximumSessions
                        // so age dentro do filtro de autenticacao, por onde este login
                        // nao passa. Quem cuida disso e SessoesAtivas, com o motivo
                        // documentado la.
                        .sessionFixation(fixacao -> fixacao.changeSessionId()))

                .headers(cabecalhos -> cabecalhos
                        // Impede que a pagina seja embutida em iframe de terceiro,
                        // que e como se monta clickjacking.
                        .frameOptions(frame -> frame.deny())
                        // Impede o navegador de "adivinhar" o tipo do conteudo e
                        // executar como script algo servido como texto.
                        .contentTypeOptions(Customizer.withDefaults())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000))
                        .referrerPolicy(politica -> politica.policy(
                                org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter
                                        .ReferrerPolicy.SAME_ORIGIN))
                        // A API so devolve JSON: nada aqui deve executar script.
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'none'; frame-ancestors 'none'")))

                .authorizeHttpRequests(rotas -> rotas
                        .requestMatchers("/api/autenticacao/cadastro", "/api/autenticacao/login").permitAll()
                        // Sonda de vida do orquestrador de containers. Os detalhes
                        // ficam desligados na configuracao, entao nao ha vazamento
                        // de estado interno aqui.
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/api/diagnostico").hasRole("ADMINISTRADOR")
                        .requestMatchers("/actuator/**").hasRole("ADMINISTRADOR")
                        .anyRequest().authenticated())

                .exceptionHandling(excecoes -> excecoes
                        .authenticationEntryPoint((requisicao, resposta, falha) ->
                                responderComJson(resposta, HttpStatus.UNAUTHORIZED,
                                        "Autenticacao necessaria."))
                        .accessDeniedHandler((requisicao, resposta, falha) ->
                                responderComJson(resposta, HttpStatus.FORBIDDEN,
                                        "Sem permissao para este recurso.")))

                // Nao existe formulario de login nem Basic: a autenticacao passa pelo
                // endpoint proprio, que e onde vivem o bloqueio e a auditoria.
                .formLogin(login -> login.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable());

        return http.build();
    }

    private void responderComJson(jakarta.servlet.http.HttpServletResponse resposta,
                                  HttpStatus status,
                                  String mensagem) throws java.io.IOException {
        resposta.setStatus(status.value());
        resposta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resposta.setCharacterEncoding("UTF-8");
        resposta.getWriter().write("""
                {"status":%d,"mensagem":"%s"}""".formatted(status.value(), mensagem));
    }

    /**
     * Origens vem de configuracao e nunca sao curinga.
     *
     * <p>{@code allowCredentials} com origem {@code *} e recusado pelo proprio
     * navegador, e por bom motivo: seria autorizar qualquer site a chamar a API com
     * o cookie de sessao do usuario.
     */
    @Bean
    public CorsConfigurationSource origensPermitidas() {
        CorsConfiguration configuracao = new CorsConfiguration();
        configuracao.setAllowedOrigins(propriedades.origensPermitidas());
        configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN"));
        configuracao.setAllowCredentials(true);
        configuracao.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/api/**", configuracao);
        return fonte;
    }
}
