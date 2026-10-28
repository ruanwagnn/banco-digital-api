# 🏦 Banco Digital API

API REST de um banco digital, feita em **Java 21 + Spring Boot 3**. O projeto implementa o
núcleo de um sistema bancário — cadastro de clientes, contas, depósitos, saques,
transferências e extrato — com as preocupações que um banco realmente cobra:
**segurança, integridade do dinheiro e segurança em acesso concorrente.**

> Projeto de portfólio. Não se conecta a nenhum banco real.

---

## ✨ O que o projeto demonstra

| Tema | Como aparece no código |
|---|---|
| **API REST** | Controllers com os verbos e status HTTP corretos (201, 200, 401, 404, 422, 409) |
| **Autenticação JWT** | Login por CPF/senha, token Bearer, filtro de autenticação, Spring Security stateless |
| **Senhas seguras** | Hash com BCrypt — a senha nunca é guardada em texto puro |
| **Dinheiro correto** | Saldo e valores em `BigDecimal` (nunca `double`), com escala de 2 casas |
| **Concorrência** | Bloqueio pessimista (`PESSIMISTIC_WRITE`) + versionamento otimista (`@Version`) para não corromper o saldo em operações simultâneas |
| **Prevenção de deadlock** | Em transferências, as duas contas são travadas sempre na mesma ordem |
| **Idempotência** | Header `Idempotency-Key` evita processar a mesma operação duas vezes |
| **Transações ACID** | `@Transactional` garante que uma transferência debita e credita tudo ou nada |
| **Validação de entrada** | Bean Validation nos DTOs, com mensagens claras |
| **Tratamento de erros** | `@RestControllerAdvice` com corpo de erro padronizado |
| **Testes** | Testes unitários (JUnit 5 + Mockito) e de integração (MockMvc) |
| **Documentação** | Swagger / OpenAPI em `/swagger-ui.html` |
| **Deploy** | Dockerfile multi-stage e `docker-compose` com PostgreSQL |

---

## 🛠️ Tecnologias

- Java 21, Spring Boot 3.3
- Spring Web, Spring Data JPA, Spring Security
- JWT (jjwt), BCrypt
- H2 (desenvolvimento) e PostgreSQL (produção)
- JUnit 5, Mockito, Spring Security Test
- springdoc-openapi (Swagger)
- Maven, Docker

---

## ▶️ Como rodar

### Opção 1 — direto com Maven (banco H2 em memória)

```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`.

- Swagger (documentação interativa): http://localhost:8080/swagger-ui.html
- Console do banco H2: http://localhost:8080/h2-console
  (JDBC URL: `jdbc:h2:mem:bancodigital`, usuário: `sa`, sem senha)

### Opção 2 — com Docker e PostgreSQL

```bash
docker compose up --build
```

---

## 🔑 Fluxo de uso

### 1. Registrar um cliente (abre a conta automaticamente)

```bash
curl -X POST http://localhost:8080/auth/registrar \
  -H "Content-Type: application/json" \
  -d '{"nome":"Maria Silva","cpf":"12345678900","email":"maria@email.com","senha":"senha12345"}'
```

Resposta:

```json
{ "numero": "04829173", "agencia": "0001", "titular": "Maria Silva", "saldo": 0.00 }
```

### 2. Fazer login e pegar o token

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"cpf":"12345678900","senha":"senha12345"}'
```

Resposta:

```json
{ "token": "eyJhbGciOi...", "tipo": "Bearer", "expiraEmSegundos": 7200 }
```

### 3. Usar o token nas operações

```bash
TOKEN="cole-o-token-aqui"

# Depositar
curl -X POST http://localhost:8080/transacoes/deposito \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"valor":250.75}'

# Consultar saldo
curl http://localhost:8080/contas/me -H "Authorization: Bearer $TOKEN"

# Transferir para outra conta
curl -X POST http://localhost:8080/transacoes/transferencia \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"contaDestino":"00000002","valor":100.00}'

# Ver extrato
curl http://localhost:8080/transacoes/extrato -H "Authorization: Bearer $TOKEN"
```

---

## 📁 Estrutura

```
src/main/java/br/com/portfolio/bancodigital
├── config/        Configuração de segurança e do Swagger
├── controller/    Endpoints REST (auth, contas, transações)
├── domain/        Entidades JPA (Cliente, Conta, Transacao)
├── dto/           Objetos de entrada e saída (records)
├── exception/     Exceções de negócio e handler global
├── repository/    Acesso a dados (Spring Data JPA)
├── security/      JWT, filtro de autenticação, UserDetails
└── service/       Regras de negócio (ContaService, TransacaoService, AuthService)
```

---

## ✅ Testes

```bash
mvn test
```

Cobrem as regras críticas: saque sem saldo é bloqueado, transferência move o valor
corretamente, valor negativo é rejeitado, idempotência não duplica operação, e o fluxo
completo registrar → login → depositar → consultar saldo pela API.

---

## 🔎 Decisões de projeto

- **Por que `BigDecimal` e não `double`?** `double` perde centavos em contas simples
  (`0.1 + 0.2 != 0.3`). Em dinheiro isso é inaceitável, então todo valor usa `BigDecimal`.
- **Por que travar a conta no banco?** Dois saques ao mesmo tempo poderiam ler o mesmo
  saldo e gravar valores errados. O bloqueio pessimista serializa as operações sobre a
  mesma conta.
- **Por que idempotência?** Em rede instável o cliente pode reenviar a mesma requisição. A
  `Idempotency-Key` garante que o dinheiro se move uma única vez.
