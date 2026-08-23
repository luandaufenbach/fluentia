package br.com.agenteingles.comum;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Converte as excecoes da aplicacao em respostas HTTP consistentes. */
@RestControllerAdvice
public class TratadorDeErros {

    private static final Logger log = LoggerFactory.getLogger(TratadorDeErros.class);

    public record RespostaDeErro(int status, String mensagem, LocalDateTime momento) {
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<RespostaDeErro> tratarRecursoNaoEncontrado(RecursoNaoEncontradoException excecao) {
        return montar(HttpStatus.NOT_FOUND, excecao.getMessage());
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<RespostaDeErro> tratarRegraDeNegocio(RegraDeNegocioException excecao) {
        return montar(HttpStatus.CONFLICT, excecao.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespostaDeErro> tratarValidacao(MethodArgumentNotValidException excecao) {
        String mensagem = excecao.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return montar(HttpStatus.BAD_REQUEST, mensagem);
    }

    /**
     * Recusa de credencial. Responde 401 com a mensagem generica que o servico ja
     * montou — o motivo real (conta inexistente, inativa ou senha errada) fica na
     * trilha de auditoria, nunca na resposta.
     */
    @ExceptionHandler(br.com.agenteingles.seguranca.ServicoDeAutenticacao.CredencialInvalidaException.class)
    public ResponseEntity<RespostaDeErro> tratarCredencialInvalida(
            br.com.agenteingles.seguranca.ServicoDeAutenticacao.CredencialInvalidaException excecao) {
        return montar(HttpStatus.UNAUTHORIZED, excecao.getMessage());
    }

    /** 429: o cliente precisa saber que deve esperar, nao tentar de novo agora. */
    @ExceptionHandler(br.com.agenteingles.seguranca.ServicoDeAutenticacao.ContaBloqueadaException.class)
    public ResponseEntity<RespostaDeErro> tratarContaBloqueada(
            br.com.agenteingles.seguranca.ServicoDeAutenticacao.ContaBloqueadaException excecao) {
        return montar(HttpStatus.TOO_MANY_REQUESTS, excecao.getMessage());
    }

    @ExceptionHandler(br.com.agenteingles.seguranca.ServicoDeAutenticacao.EmailJaCadastradoException.class)
    public ResponseEntity<RespostaDeErro> tratarEmailDuplicado(
            br.com.agenteingles.seguranca.ServicoDeAutenticacao.EmailJaCadastradoException excecao) {
        return montar(HttpStatus.CONFLICT, excecao.getMessage());
    }

    /**
     * Sessao valida para conta que nao pode mais autenticar — desativada depois do
     * login. Responde 401 para o cliente refazer a entrada.
     */
    @ExceptionHandler(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<RespostaDeErro> tratarSemAutenticacao(
            org.springframework.security.authentication.AuthenticationCredentialsNotFoundException excecao) {
        return montar(HttpStatus.UNAUTHORIZED, "Autenticacao necessaria.");
    }

    /**
     * O 403 do Spring Security precisa passar reto.
     *
     * <p>Sem esta excecao explicita, o tratador generico abaixo o converteria em 500 e
     * um acesso negado viraria "erro do servidor" no log — ruido que esconde tentativa
     * de acesso indevido em vez de evidencia-la.
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<RespostaDeErro> tratarAcessoNegado(
            org.springframework.security.access.AccessDeniedException excecao) {
        return montar(HttpStatus.FORBIDDEN, "Sem permissao para este recurso.");
    }

    /**
     * Metodo HTTP errado e erro de quem chamou, nao do servidor.
     *
     * <p>Sem esta entrada o tratador generico devolvia 500 para um simples POST numa
     * rota de GET, escondendo a causa e enchendo o log de "erro inesperado".
     */
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<RespostaDeErro> tratarMetodoNaoSuportado(
            org.springframework.web.HttpRequestMethodNotSupportedException excecao) {
        return montar(HttpStatus.METHOD_NOT_ALLOWED,
                "Metodo %s nao e aceito neste endereco.".formatted(excecao.getMethod()));
    }

    /**
     * Restricao do banco violada: duas requisicoes correram para criar a mesma coisa.
     *
     * <p>O detalhe da restricao fica so no log — nome de indice e de coluna descrevem o
     * esquema, e esquema nao vai para a resposta.
     */
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<RespostaDeErro> tratarConflitoDeDados(
            org.springframework.dao.DataIntegrityViolationException excecao) {
        log.warn("Restricao de integridade violada", excecao);
        return montar(HttpStatus.CONFLICT, "Este recurso ja existe ou acabou de ser criado.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespostaDeErro> tratarErroInesperado(Exception excecao) {
        log.error("Erro inesperado ao processar a requisicao", excecao);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro inesperado ao processar a requisicao. Consulte os logs do servidor.");
    }

    private ResponseEntity<RespostaDeErro> montar(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status)
                .body(new RespostaDeErro(status.value(), mensagem, LocalDateTime.now()));
    }
}
