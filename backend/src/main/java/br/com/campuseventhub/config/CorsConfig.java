package br.com.campuseventhub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuracao de CORS: permite que o frontend (React) chame a API.
 *
 * Por que precisa:
 * Em desenvolvimento o frontend roda em http://localhost:5173 e o backend em
 * http://localhost:8080. Sao origens diferentes, e o NAVEGADOR bloqueia
 * chamadas entre origens diferentes, a menos que o servidor as autorize.
 * (Por isso a mesma chamada funciona no Postman/Swagger e falha no React.)
 *
 * Como funciona:
 * 1. As origens permitidas vem de app.cors.allowed-origins no
 *    application.properties (variavel CORS_ALLOWED_ORIGINS). Para liberar
 *    mais de uma, separe por virgula; o Spring converte em String[].
 * 2. Antes de um POST/PUT/PATCH/DELETE com JSON, o navegador envia uma
 *    requisicao OPTIONS ("preflight") perguntando se pode. Estas regras
 *    respondem a essa pergunta; por isso o OPTIONS esta na lista de metodos.
 * 3. Os metodos liberados seguem a tabela do doc/api.md (secao 2). O
 *    cabecalho Authorization ja esta liberado para o token de login.
 *
 * ATENCAO - quando o Spring Security for adicionado (issue #7):
 * o Security intercepta as requisicoes antes do Spring MVC e bloqueia o
 * preflight. Estas regras devem ser movidas para um @Bean do tipo
 * CorsConfigurationSource e ativadas na configuracao de seguranca com
 * http.cors(...). Depois disso, esta classe pode ser removida.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization");
    }
}
