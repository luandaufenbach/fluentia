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
