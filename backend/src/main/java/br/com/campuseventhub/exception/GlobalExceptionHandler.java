package br.com.campuseventhub.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Tratamento centralizado de erros da API.
 *
 * Como funciona:
 * 1. O @RestControllerAdvice faz esta classe "vigiar" todos os controllers.
 * 2. Quando um controller (ou o service que ele chama) lanca uma excecao, o
 *    Spring procura aqui o metodo cujo @ExceptionHandler indica aquele tipo
 *    de excecao e o executa no lugar da resposta normal.
 * 3. O metodo devolve um ProblemDetail, que o Spring converte para JSON no
 *    padrao Problem Details (RFC 9457) e usa o status dele como codigo HTTP.
 *    Os campos "type" e "instance" sao preenchidos automaticamente.
 *
 * Assim os controllers e services nao precisam montar respostas de erro:
 * basta lancar a excecao certa. O formato do JSON esta em doc/api.md (secao 5).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Recurso inexistente (ex.: GET /api/salas/999) -> 404 Not Found.
     * O "detail" e a mensagem escrita no service ao lancar a excecao.
     */
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail tratarRecursoNaoEncontradoException(RecursoNaoEncontradoException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problema.setTitle("Recurso não encontrado");
        return problema;
    }

    /**
     * Regra de negocio violada (ex.: choque de horario, vagas esgotadas)
     * -> 409 Conflict.
     * O "detail" e a mensagem escrita no service ao lancar a excecao.
     */
    @ExceptionHandler(RegraNegocioException.class)
    public ProblemDetail tratarRegraNegocioException(RegraNegocioException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problema.setTitle("Violação de regra de negócio");
        return problema;
    }

    /**
     * Falha no @Valid de um DTO (ex.: campo obrigatorio vazio, capacidade
     * negativa) -> 400 Bad Request.
     *
     * A mensagem e fixa e generica de proposito: a mensagem da propria
     * MethodArgumentNotValidException e tecnica, em ingles e expoe detalhes
     * internos (nome do controller e do metodo). A indicacao de qual campo
     * esta errado e feita pela validacao do formulario no frontend.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail tratarMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Um ou mais campos estão inválidos.");
        problema.setTitle("Validação de argumentos");
        return problema;
    }
}
