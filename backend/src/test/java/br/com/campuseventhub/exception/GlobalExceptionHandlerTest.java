package br.com.campuseventhub.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testa se o GlobalExceptionHandler converte cada excecao na resposta HTTP
 * certa: status, "title" e "detail" no formato Problem Details (doc/api.md,
 * secao 5).
 *
 * Como funciona:
 * 1. O @WebMvcTest sobe so a camada web do Spring (controllers, advices,
 *    conversao JSON), sem banco e sem services. Por isso roda rapido e nao
 *    precisa do Docker/Testcontainers.
 * 2. Como ainda nao existem controllers reais, o teste usa o ControllerFalso
 *    abaixo, cujos endpoints so lancam as excecoes, simulando o que um
 *    service real faria. O @Import registra esse controller no contexto.
 * 3. O MockMvc simula requisicoes HTTP sem abrir porta de rede e permite
 *    verificar a resposta (status e campos do JSON).
 *
 * Assim o handler e testado isolado: se um teste quebrar, o problema esta na
 * conversao da excecao, e nao em regra de negocio ou acesso a banco.
 */
@WebMvcTest
@Import(GlobalExceptionHandlerTest.ControllerFalso.class)
class GlobalExceptionHandlerTest {

    /** Cliente HTTP simulado, criado e injetado pelo @WebMvcTest. */
    @Autowired
    private MockMvc mockMvc;

    /**
     * RecursoNaoEncontradoException -> 404 Not Found.
     * O "detail" deve ser exatamente a mensagem passada na excecao.
     */
    @Test
    void deveRetornar404QuandoRecursoNaoEncontrado() throws Exception {
        mockMvc.perform(get("/api/teste/recurso-nao-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.detail").value("Sala não encontrada."));
    }

    /**
     * RegraNegocioException -> 409 Conflict.
     * O "detail" deve ser exatamente a mensagem passada na excecao.
     */
    @Test
    void deveRetornar409QuandoRegraNegocioViolada() throws Exception {
        mockMvc.perform(get("/api/teste/regra-negocio-violada"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Violação de regra de negócio"))
                .andExpect(jsonPath("$.detail").value("Vagas esgotadas."));
    }

    /**
     * Falha no @Valid -> 400 Bad Request com mensagem generica.
     *
     * O corpo manda o campo obrigatorio como string vazia. Isso passaria num
     * @NotNull, mas falha no @NotBlank, garantindo que a regra usada nos DTOs
     * barra tambem texto vazio. O Spring rejeita a requisicao antes de entrar
     * no metodo do controller e lanca MethodArgumentNotValidException, que o
     * handler converte em 400.
     */
    @Test
    void deveRetornar400QuandoFalhaValidacao() throws Exception {
        mockMvc.perform(post("/api/teste/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campoObrigatorio\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validação de argumentos"))
                .andExpect(jsonPath("$.detail").value("Um ou mais campos estão inválidos."));
    }

    /**
     * Controller que existe so para o teste. Cada endpoint provoca um tipo
     * de erro, para que o handler possa ser verificado sem depender dos
     * controllers e services reais.
     */
    @RestController
    static class ControllerFalso {

        /** Simula um service que nao achou o recurso buscado. */
        @GetMapping("/api/teste/recurso-nao-encontrado")
        public void recursoNaoEncontrado() {
            throw new RecursoNaoEncontradoException("Sala não encontrada.");
        }

        /** Simula um service que barrou a operacao por regra de negocio. */
        @GetMapping("/api/teste/regra-negocio-violada")
        public void regraNegocioViolada() {
            throw new RegraNegocioException("Vagas esgotadas.");
        }

        /**
         * Vazio de proposito: o @Valid faz o Spring validar o DtoFalso antes
         * de chamar o metodo. Com corpo invalido, o metodo nunca executa.
         */
        @PostMapping("/api/teste/validacao")
        public void validacao(@Valid @RequestBody DtoFalso dto) {
        }
    }

    /**
     * DTO minimo para o teste de validacao. O @Valid no parametro do
     * controller manda validar este objeto; o @NotBlank no campo define a
     * regra (rejeita null, "" e so espacos).
     */
    record DtoFalso(@NotBlank String campoObrigatorio) {
    }
}
