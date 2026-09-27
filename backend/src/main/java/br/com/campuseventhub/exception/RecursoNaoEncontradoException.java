package br.com.campuseventhub.exception;

/**
 * Lancada quando um recurso buscado pelo id nao existe no banco
 * (ex.: GET /api/salas/999).
 *
 * O GlobalExceptionHandler converte esta excecao em uma resposta 404 Not Found.
 *
 * Uso no service:
 *   throw new RecursoNaoEncontradoException("Sala não encontrada.");
 *
 * E uma RuntimeException (nao checada), entao nao precisa de "throws" na
 * assinatura dos metodos que a lancam.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    /**
     * @param mensagem texto exibido ao usuario, em portugues. Vai para o
     *                 campo "detail" da resposta de erro.
     */
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
