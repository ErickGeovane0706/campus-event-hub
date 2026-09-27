package br.com.campuseventhub.exception;

/**
 * Lancada quando os dados estao em formato valido, mas uma regra do sistema
 * impede a operacao (ex.: choque de horario, vagas esgotadas, nome de sala
 * duplicado, inscricao repetida).
 *
 * O GlobalExceptionHandler converte esta excecao em uma resposta 409 Conflict.
 * Erros de formato (campo vazio, numero negativo) NAO usam esta excecao: sao
 * barrados antes pelo @Valid e viram 400.
 *
 * Uso no service:
 *   throw new RegraNegocioException("A sala Lab 01 já está reservada nesse horário.");
 */
public class RegraNegocioException extends RuntimeException {

    /**
     * @param mensagem texto exibido ao usuario, em portugues. Vai para o
     *                 campo "detail" da resposta de erro.
     */
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
