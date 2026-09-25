# 10. Ambiente de Desenvolvimento e Testes

Esta seção descreve como o ambiente do Campus Event Hub é padronizado com Docker, garantindo que todos os integrantes da equipe utilizem a mesma versão do banco de dados, independentemente do sistema operacional ou das configurações de cada máquina.

## 10.1 Visão geral

O projeto utiliza o Docker em dois momentos distintos:

| Situação | Comando | O que roda no Docker |
|---|---|---|
| Desenvolvimento do dia a dia | `docker compose up -d` | Apenas o PostgreSQL (banco fixo, com os dados de desenvolvimento) |
| Execução dos testes automatizados | `./mvnw test` | Um PostgreSQL **temporário**, criado e removido automaticamente pelo Testcontainers |

Durante o desenvolvimento, o backend (Spring Boot) e o frontend (React) são executados diretamente na máquina, pelas IDEs IntelliJ IDEA e VS Code. Essa decisão mantém o recarregamento automático (hot reload) e a depuração (debug) funcionando sem configurações adicionais.

Ao final do projeto, está prevista a criação de imagens Docker para o backend e o frontend, permitindo executar o sistema completo com um único comando (`docker compose --profile full up -d`).

## 10.2 Tecnologias do ambiente

| Tecnologia | Versão | Finalidade |
|---|---|---|
| Docker Desktop / Docker Compose | 29 / v2 | Execução do banco de dados em container |
| PostgreSQL | 17 (imagem `postgres:17-alpine`) | Banco de dados relacional |
| Flyway | 12 | Versionamento do schema do banco (migrations) |
| Testcontainers | 2.0 | Banco de dados real e descartável para os testes automatizados |
| Java / Spring Boot | 21 / 4.1 | Backend (API REST) |
| Node.js / React / Vite | 18+ / 19 / 8 | Frontend |

## 10.3 Banco de dados de desenvolvimento (Docker Compose)

O arquivo `docker-compose.yml`, localizado na raiz do repositório, define um serviço chamado `db` com as seguintes características:

- **Imagem:** `postgres:17-alpine`, a versão oficial e leve do PostgreSQL 17.
- **Porta:** `5432`, a porta padrão do PostgreSQL, acessível em `localhost:5432`.
- **Credenciais padrão:** banco `campus_event_hub`, usuário `postgres` e senha `postgres`. Esses valores podem ser alterados por meio de um arquivo `.env` na raiz do projeto.
- **Persistência:** os dados são armazenados no volume Docker `postgres-data` e continuam existindo mesmo que o container seja parado ou removido.
- **Healthcheck:** o Docker verifica periodicamente se o banco está pronto para receber conexões.

Comandos principais, executados na raiz do projeto:

| Comando | Efeito |
|---|---|
| `docker compose up -d` | Sobe o banco em segundo plano |
| `docker compose ps` | Mostra o estado do container (deve aparecer como *healthy*) |
| `docker compose logs -f db` | Exibe os logs do banco |
| `docker compose stop` | Para o banco, mantendo os dados |
| `docker compose down -v` | Remove o container **e apaga todos os dados** |

## 10.4 Configuração da aplicação (variáveis de ambiente)

Senhas e dados de conexão não são gravados no código. A aplicação lê essas informações de arquivos `.env`, que são ignorados pelo Git. O repositório contém apenas os modelos `.env.example`.

| Arquivo | Uso |
|---|---|
| `.env` (raiz, opcional) | Altera os valores padrão do Docker Compose (nome do banco, usuário, senha e porta) |
| `backend/.env` | Dados de conexão do Spring Boot com o banco (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`), porta do servidor e origem permitida no CORS |
| `frontend/.env` | Endereço da API usado pelo Vite (`VITE_API_URL`). Não deve conter senhas, pois as variáveis `VITE_` ficam visíveis no navegador |

O schema do banco é criado e atualizado exclusivamente pelo **Flyway**, a partir dos scripts SQL em `backend/src/main/resources/db/migration`. O Hibernate apenas valida se as entidades Java correspondem às tabelas (`spring.jpa.hibernate.ddl-auto=validate`).

## 10.5 Testes automatizados com Testcontainers

Os testes do backend utilizam a biblioteca **Testcontainers**. Ao executar `./mvnw test`, ocorre o seguinte fluxo:

1. O Testcontainers cria um container PostgreSQL temporário, com a mesma versão utilizada em desenvolvimento.
2. O Spring Boot se conecta automaticamente a esse container (anotação `@ServiceConnection`), sem necessidade de configuração manual.
3. O Flyway executa todas as migrations em um banco vazio, validando também os scripts SQL.
4. Os testes são executados contra um banco de dados real.
5. Ao final, o container é removido automaticamente.

Essa abordagem traz os seguintes benefícios:

- **Fidelidade:** as consultas, em especial a validação de choque de horários (RF06), são testadas no mesmo banco utilizado em produção, e não em um banco em memória com comportamento diferente.
- **Isolamento:** os testes nunca alteram os dados do banco de desenvolvimento.
- **Reprodutibilidade:** qualquer máquina com Docker executa os testes da mesma forma, contribuindo para a meta de cobertura de 70% das regras de negócio (RNF05).

A configuração fica na classe `backend/src/test/java/br/com/campuseventhub/TestcontainersConfiguration.java`. Para utilizá-la, basta anotar a classe de teste com `@Import(TestcontainersConfiguration.class)`.

**Requisito:** o Docker Desktop precisa estar em execução durante os testes.

## 10.6 Passo a passo para um novo integrante

1. Instalar o Java 21, o Node.js 18+ e o Docker Desktop.
2. Clonar o repositório.
3. Na raiz do projeto, subir o banco com `docker compose up -d`.
4. Em `backend/`, copiar `.env.example` para `.env` e executar `./mvnw spring-boot:run`.
5. Em `frontend/`, copiar `.env.example` para `.env` e executar `npm install` e depois `npm run dev`.
6. Acessar o frontend em `http://localhost:5173` e a documentação da API (Swagger) em `http://localhost:8080/swagger-ui.html`.
