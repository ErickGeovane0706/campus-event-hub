package br.com.campuseventhub;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

// Sobe a aplicacao inteira usando o PostgreSQL temporario do Testcontainers
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class CampusEventHubApplicationTests {

	// Verifica se a aplicacao consegue iniciar: conecta no banco,
	// roda as migrations do Flyway e carrega todos os componentes do Spring
	@Test
	void contextLoads() {
	}

}
