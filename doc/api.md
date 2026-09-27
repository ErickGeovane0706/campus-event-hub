# Contrato da API — Convenções Gerais

Este documento define as regras que valem para **toda** a API REST do Campus Event Hub. Ele é o acordo entre o frontend (React) e o backend (Spring Boot): os dois lados seguem estas convenções.

Os **endpoints** de cada entidade (caminhos, JSON de pedido e resposta, erros possíveis) são documentados no **Swagger**, gerado a partir do código:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Especificação OpenAPI (JSON): `http://localhost:8080/v3/api-docs`

> **Fluxo de cada fatia:** o backend começa pelo esqueleto (DTOs + Controller com os métodos ainda sem lógica). Assim o formato já aparece no Swagger e o frontend pode começar em paralelo enquanto a lógica é implementada.

---

## 1. Idioma

- Nomes de caminhos, campos e enums em **português, sem acentos e sem cedilha**: `inscricoes`, `descricao`, `horaInicio`.
- Mesma nomenclatura do modelo do banco (`doc/diagramas/bancoDeDados.plantuml`), adaptada ao padrão do JSON (ver seção 3).
- Mensagens de erro para o usuário em **português, com acentos**, pois são exibidas na tela.

## 2. URLs

| Regra | Exemplo |
|---|---|
| Todo endpoint começa com `/api` | `/api/salas` |
| Recurso é um **substantivo no plural** | `/api/eventos`, e não `/api/evento` nem `/api/listarEventos` |
| A ação é definida pelo **método HTTP**, não pelo caminho | `DELETE /api/salas/5`, e não `/api/salas/5/excluir` |
| Um item específico é identificado pelo id no caminho | `/api/salas/5` |
| Recurso que pertence a outro usa caminho aninhado | `/api/eventos/3/inscricoes` |
| Nomes compostos usam hífen (kebab-case) | `/api/reservas-pendentes` |
| Filtros usam parâmetros de consulta (query string) | `/api/salas?ativa=true` |
| Ações que não são criar/alterar/remover usam um verbo como sub-recurso com `PATCH` ou `POST` | `PATCH /api/salas/5/inativar`, `POST /api/reservas/8/aprovar` |
| Sem versão na URL | `/api/salas`, e não `/api/v1/salas` |

### Métodos HTTP

| Método | Uso | Resposta de sucesso |
|---|---|---|
| `GET` | Buscar um item ou uma lista | `200 OK` com os dados |
| `POST` | Criar | `201 Created` com o item criado e o cabeçalho `Location` |
| `PUT` | Atualizar o item inteiro | `200 OK` com o item atualizado |
| `PATCH` | Alterar uma parte ou executar uma ação (inativar, aprovar) | `200 OK` com o item atualizado |
| `DELETE` | Remover | `204 No Content` |

## 3. Campos no JSON

| Regra | Exemplo |
|---|---|
| Nomes em **camelCase** | `nomeIdentificador`, `limiteVagas`, `horaInicioGrade` |
| Coluna `snake_case` no banco vira `camelCase` no JSON | `id_organizador` → `idOrganizador` |
| IDs são números | `"id": 5` |
| Enums em texto maiúsculo, iguais ao modelo do banco | `"statusReserva": "PENDENTE"`, `"perfil": "ALUNO"` |
| Booleanos | `"ativa": true` |
| Campo sem valor vem como `null` (nunca é omitido) | `"motivoRejeicao": null` |
| Listas vazias vêm como `[]` (nunca `null`) | `"inscricoes": []` |

**A API nunca expõe as entidades JPA diretamente.** Entrada e saída usam **DTOs** (pacote `dto`), que definem exatamente os campos de cada operação. Campos sensíveis, como `senhaHash`, **nunca** aparecem em uma resposta.

## 4. Datas e horas

Formato **ISO-8601**, sempre no **horário de Brasília (UTC-3)**, sem fuso horário na string:

| Tipo | Formato | Exemplo | Tipo no Java |
|---|---|---|---|
| Data | `AAAA-MM-DD` | `"2026-10-15"` | `LocalDate` |
| Hora | `HH:mm` | `"14:00"` | `LocalTime` |
| Data e hora | `AAAA-MM-DDTHH:mm:ss` | `"2026-10-15T14:00:00"` | `LocalDateTime` |

- A formatação para exibição (`15/10/2026`, `14h`) é responsabilidade **somente do frontend**.
- Dias da semana (grade das salas) usam as siglas `SEG`, `TER`, `QUA`, `QUI`, `SEX`, `SAB`, `DOM`.

## 5. Formato padrão de erro

Toda resposta de erro usa o padrão **Problem Details (RFC 9457)**, suportado nativamente pelo Spring Boot (`ProblemDetail`). O frontend trata qualquer erro com o mesmo código.

```json
{
  "type": "about:blank",
  "title": "Violação de regra de negócio",
  "status": 409,
  "detail": "A sala Lab 01 já está reservada em 15/10/2026 das 14:00 às 16:00.",
  "instance": "/api/reservas"
}
```

| Campo | Significado |
|---|---|
| `type` | Identificador do tipo de erro (usar `about:blank`) |
| `title` | Resumo curto do erro |
| `status` | O mesmo código HTTP da resposta |
| `detail` | **Mensagem em português, pensada para ser exibida ao usuário** |
| `instance` | Caminho da requisição que falhou |

Exemplo de erro de validação (`400`):

```json
{
  "type": "about:blank",
  "title": "Validação de argumentos",
  "status": 400,
  "detail": "Um ou mais campos estão inválidos.",
  "instance": "/api/salas"
}
```

O erro de validação **não informa quais campos falharam**: a mensagem é sempre a mesma. A validação campo a campo (obrigatório, tamanho, valor mínimo) deve ser feita no **formulário do frontend**, antes de enviar; o `@Valid` do backend é a última barreira.

A conversão das exceções para este formato é centralizada no `GlobalExceptionHandler` (issue #3).

## 6. Códigos de status

| Código | Quando usar | Exemplo |
|---|---|---|
| `200 OK` | Sucesso com dados na resposta | Listar salas |
| `201 Created` | Recurso criado | Cadastrar evento |
| `204 No Content` | Sucesso sem conteúdo | Remover sala |
| `400 Bad Request` | Dado **mal formado** ou inválido | Capacidade negativa, campo obrigatório vazio |
| `401 Unauthorized` | Usuário não autenticado ou token inválido | Acessar a API sem login |
| `403 Forbidden` | Autenticado, mas **sem permissão** | Aluno tentando cadastrar sala |
| `404 Not Found` | Recurso inexistente | `GET /api/salas/999` |
| `409 Conflict` | Dado válido, mas **uma regra de negócio impede** | Choque de horário, vagas esgotadas, nome de sala duplicado, inscrição repetida |
| `423 Locked` | Conta bloqueada temporariamente | 5 tentativas de login incorretas (RF01 RN3) |
| `500 Internal Server Error` | Erro inesperado no servidor (nunca intencional) | — |

**Regra prática:** `400` é quando o **formato** está errado; `409` é quando o formato está certo, mas as **regras do sistema** não permitem.

## 7. Paginação

Nesta versão, **as listas não são paginadas**: os endpoints de listagem retornam um array JSON simples.

```json
[
  { "id": 1, "nomeIdentificador": "Lab 01" },
  { "id": 2, "nomeIdentificador": "Auditório" }
]
```

Se alguma lista crescer muito (por exemplo, o catálogo de eventos), a paginação será adicionada **apenas nela**, usando o padrão do Spring Data (`?page=0&size=20&sort=titulo,asc`), e registrada neste documento.

## 8. Autenticação

- Todos os endpoints exigem autenticação, **exceto** o login e o cadastro de usuário.
- Credenciais enviadas no cabeçalho `Authorization: Bearer <token>` (caso seja adotado JWT).
- A estratégia final (JWT ou sessão) será definida na issue #7 e registrada aqui.
- Permissões por perfil (ALUNO, PROFESSOR, ADMIN) seguem o modelo RBAC (RNF01) e são indicadas em cada endpoint no Swagger.

---

## Documentando os endpoints no Swagger

Cada Controller deve usar as anotações do springdoc para o Swagger ficar claro:

```java
@Tag(name = "Salas", description = "Cadastro de salas do instituto (RF02, RF03)")
@RestController
@RequestMapping("/api/salas")
public class SalaController {

    @Operation(summary = "Cadastrar sala", description = "Perfil: ADMIN")
    @ApiResponse(responseCode = "201", description = "Sala criada")
    @ApiResponse(responseCode = "409", description = "Já existe uma sala com esse nome")
    @PostMapping
    public ResponseEntity<SalaResponse> cadastrar(@RequestBody @Valid SalaRequest request) { ... }
}
```

- `@Tag`: agrupa os endpoints da entidade e cita os requisitos (RF).
- `@Operation`: resumo e **perfil que pode usar** o endpoint.
- `@ApiResponse`: os códigos de status possíveis, seguindo a seção 6.
- DTOs com nomes `XxxRequest` (entrada) e `XxxResponse` (saída).
