package br.com.agenteingles.conteudo.geracao;

import br.com.agenteingles.conteudo.ConteudoDoModuloRepositorio;
import br.com.agenteingles.modulo.Modulo;
import br.com.agenteingles.modulo.ModuloRepositorio;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Escreve o conteudo de ensino de todos os modulos como migration versionada.
 *
 * <p>Roda uma vez, sob demanda:
 *
 * <pre>./mvnw spring-boot:run -Dspring-boot.run.profiles=gerar-conteudo</pre>
 *
 * <p>O resultado e um arquivo SQL comum, revisavel e editavel a mao no git. A escolha
 * de gravar SQL em vez de escrever direto no banco e deliberada: o conteudo didatico
 * e material do produto, entao ele precisa passar por revisao e ficar versionado —
 * nao pode depender de uma chamada de API para existir de novo.
 */
@Component
@Profile("gerar-conteudo")
public class RotinaDeGeracaoDeConteudo implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RotinaDeGeracaoDeConteudo.class);

    private static final Path PASTA_DAS_MIGRATIONS =
            Path.of("src", "main", "resources", "db", "migration");

    private final ModuloRepositorio moduloRepositorio;
    private final ConteudoDoModuloRepositorio conteudoRepositorio;
    private final GeradorDeConteudoComClaude gerador;
    private final ApplicationContext contexto;

    public RotinaDeGeracaoDeConteudo(ModuloRepositorio moduloRepositorio,
                                     ConteudoDoModuloRepositorio conteudoRepositorio,
                                     GeradorDeConteudoComClaude gerador,
                                     ApplicationContext contexto) {
        this.moduloRepositorio = moduloRepositorio;
        this.conteudoRepositorio = conteudoRepositorio;
        this.gerador = gerador;
        this.contexto = contexto;
    }

    @Override
    public void run(ApplicationArguments argumentos) throws Exception {
        List<String> jaTemConteudo = conteudoRepositorio.codigosDosModulosComConteudo();

        List<Modulo> modulos = moduloRepositorio.findAll().stream()
                .filter(modulo -> !jaTemConteudo.contains(modulo.getCodigo()))
                .sorted(Comparator.comparing(Modulo::getOrdem))
                .toList();

        if (modulos.isEmpty()) {
            log.info("Todos os modulos ja tem conteudo. Nada a gerar.");
            encerrar(0);
            return;
        }

        log.info("Gerando conteudo de {} modulo(s). Isso leva alguns minutos.", modulos.size());

        StringBuilder sql = new StringBuilder(cabecalho());
        int falhas = 0;

        for (Modulo modulo : modulos) {
            try {
                sql.append(escreverModulo(modulo, gerador.gerar(modulo)));
                log.info("OK: {}", modulo.getCodigo());
            } catch (RuntimeException falha) {
                // Uma falha nao pode perder o que ja foi gerado: o arquivo e escrito
                // no final com tudo que deu certo, e o modulo que falhou fica de fora.
                falhas++;
                log.error("Falhou no modulo {}: {}", modulo.getCodigo(), falha.getMessage());
            }
        }

        Path arquivo = proximoArquivoDeMigration();
        Files.writeString(arquivo, sql.toString(), StandardCharsets.UTF_8);
        log.info("Arquivo escrito em {} ({} modulo(s) gerado(s), {} falha(s))",
                arquivo, modulos.size() - falhas, falhas);

        encerrar(falhas == 0 ? 0 : 1);
    }

    /**
     * Proximo numero de versao livre na pasta. Como a rotina pula o que ja esta no
     * banco, uma segunda execucao gera so o que faltou — e precisa de arquivo proprio,
     * senao sobrescreveria a migration anterior e apagaria o conteudo ja aplicado.
     */
    private Path proximoArquivoDeMigration() throws Exception {
        try (var arquivos = Files.list(PASTA_DAS_MIGRATIONS)) {
            int maiorVersao = arquivos
                    .map(caminho -> caminho.getFileName().toString())
                    .filter(nome -> nome.matches("V\\d+__.*\\.sql"))
                    .mapToInt(nome -> Integer.parseInt(nome.substring(1, nome.indexOf("__"))))
                    .max()
                    .orElse(0);

            return PASTA_DAS_MIGRATIONS.resolve("V%d__conteudo_dos_modulos.sql".formatted(maiorVersao + 1));
        }
    }

    private void encerrar(int codigo) {
        SpringApplication.exit(contexto, () -> codigo);
    }

    private String cabecalho() {
        return """
                -- Conteudo de ensino dos modulos: o que o aluno le antes de praticar.
                --
                -- Gerado pela rotina do perfil "gerar-conteudo" e revisado a mao. Editar
                -- este arquivo direto e o caminho esperado para corrigir uma explicacao —
                -- ele e a fonte da verdade do material, nao a chamada de API que o produziu.

                """;
    }

    private String escreverModulo(Modulo modulo, ConteudoGerado conteudo) {
        StringBuilder sql = new StringBuilder();
        String codigo = texto(modulo.getCodigo());

        sql.append("-- %s (%s)\n".formatted(modulo.getNome(), modulo.getNivelCefr()));
        sql.append("""
                INSERT INTO conteudo_do_modulo (modulo_id, resumo, explicacao)
                SELECT id, %s, %s FROM modulo WHERE codigo = %s;

                """.formatted(texto(conteudo.resumo()), texto(conteudo.explicacao()), codigo));

        int ordem = 1;
        for (ConteudoGerado.ExemploGerado exemplo : conteudo.exemplos()) {
            sql.append("""
                    INSERT INTO exemplo_do_conteudo (conteudo_id, ordem, em_ingles, em_portugues, observacao)
                    SELECT c.id, %d, %s, %s, %s FROM conteudo_do_modulo c
                      JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = %s;
                    """.formatted(ordem++, texto(exemplo.emIngles()), texto(exemplo.emPortugues()),
                    texto(exemplo.observacao()), codigo));
        }
        sql.append("\n");

        ordem = 1;
        for (ConteudoGerado.ErroComumGerado erro : conteudo.errosComuns()) {
            sql.append("""
                    INSERT INTO erro_comum_do_conteudo (conteudo_id, ordem, errado, certo, explicacao)
                    SELECT c.id, %d, %s, %s, %s FROM conteudo_do_modulo c
                      JOIN modulo m ON m.id = c.modulo_id WHERE m.codigo = %s;
                    """.formatted(ordem++, texto(erro.errado()), texto(erro.certo()),
                    texto(erro.explicacao()), codigo));
        }

        return sql.append("\n").toString();
    }

    /** Literal SQL com aspa simples duplicada, ou NULL quando o campo vem vazio. */
    private String texto(String valor) {
        return valor == null || valor.isBlank() ? "NULL" : "'" + valor.replace("'", "''") + "'";
    }
}
