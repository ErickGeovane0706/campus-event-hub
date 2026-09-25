# 🎓 Campus Event Hub

Plataforma de gestão de eventos e alocação de espaços físicos para a comunidade acadêmica do **IFPB Campus Monteiro**. O **Campus Event Hub** permite que discentes e docentes publiquem e participem de cursos, palestras e workshops, além de integrar a reserva inteligente de salas com verificação automática de choques de horários.

---

## 📌 Funcionalidades Principais

- **Gestão de Eventos:** Criação, edição e divulgação de cursos, minicursos e workshops.
- **Reserva de Espaços:** Agendamento de salas de aula, laboratórios e auditórios do campus.
- **Prevenção de Conflitos:** Validação em tempo real para impedir choque de horários no mesmo local.
- **Inscrições & Lotação:** Inscrição rápida para participantes com controle de limite de vagas.
- **Lembretes Automáticos:** Disparo de e-mails para confirmações e lembretes antes do início do evento.

---

## 🛠️ Tecnologias Utilizadas

- **Front-end:** [React](https://react.dev/), [Tailwind CSS](https://tailwindcss.com/)
- **Back-end:** [Java](https://www.java.com/), [Spring Boot](https://spring.io/projects/spring-boot) (Spring Data JPA, Bean Validation, Spring Mail, Scheduling)
- **Banco de Dados:** [PostgreSQL](https://www.postgresql.org/)

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
- Java 21+
- Node.js 18+ (com npm ou yarn)
- Docker Desktop (o PostgreSQL roda em container; não precisa instalar)

### Estrutura do repositório

```
campus-event-hub/
├── backend/             # API REST em Spring Boot (Model + Controller)
├── frontend/            # Aplicação React (View)
├── doc/                 # Documentação e diagramas
└── docker-compose.yml   # Banco de dados PostgreSQL para desenvolvimento
```

### Banco de dados

```bash
docker compose up -d      # na raiz do projeto: sobe o PostgreSQL
```

Detalhes (comandos, testes e como tudo funciona) em [doc/ambiente-de-desenvolvimento.md](doc/ambiente-de-desenvolvimento.md).

### Backend

```bash
cd backend
cp .env.example .env      # os valores padrão já batem com o docker-compose
./mvnw spring-boot:run    # no Windows: mvnw.cmd spring-boot:run
./mvnw test               # roda os testes (sobe um PostgreSQL temporário via Testcontainers)
```

A API sobe em `http://localhost:8080` e a documentação Swagger fica em `http://localhost:8080/swagger-ui.html`.

### Frontend

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

A aplicação sobe em `http://localhost:5173`.

---
