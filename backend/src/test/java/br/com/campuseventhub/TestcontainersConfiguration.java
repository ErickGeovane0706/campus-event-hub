package br.com.campuseventhub;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Configuracao do banco de dados usado nos testes.
 *
 * Como funciona:
 * 1. Ao iniciar um teste que importa esta classe, o Testcontainers sobe um
 *    container Docker com o PostgreSQL (mesma versao do docker-compose.yml).
 * 2. O @ServiceConnection faz o Spring Boot descobrir sozinho a URL, o usuario
 *    e a senha desse container, substituindo o spring.datasource.* do
 *    application.properties. Nao precisa configurar nada a mais.
 * 3. O Flyway roda as migrations nesse banco vazio, entao os testes sempre
 *    comecam com o schema atualizado.
 * 4. Ao final dos testes, o container e removido automaticamente.
 *
 * O banco de desenvolvimento (docker compose) NAO e afetado pelos testes.
 *
 * Uso: anote a classe de teste com @Import(TestcontainersConfiguration.class).
 * O Spring reaproveita o mesmo container entre as classes de teste que usam
 * esta configuracao, entao ele sobe apenas uma vez por execucao do mvn test.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgresContainer() {
		return new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));
	}

}
