# Portfolio Backend

REST API for the projects, comments and contact messages of [Tiago Rodrigues's portfolio](https://github.com/TiagoRodrigues-GitH/tiago-rodrigues-portfolio).

| | |
|---|---|
| **Author** | Tiago Rodrigues · Universidade Tecnológica Federal do Paraná (UTFPR) |
| **Date** | 2026-06-11 |
| **Context** | Personal project |
| **Stack** | Java 17 · Spring Boot 3.5 · Spring Data JPA · Spring Security · H2 / PostgreSQL · Docker |

> **Resumo (PT).** API REST em Spring Boot que serve os projetos, comentários e mensagens de contato do portfólio, com H2 em desenvolvimento, PostgreSQL em produção e imagem Docker.

## Endpoints

| Method | Path | Purpose |
|---|---|---|
| GET | `/` | Welcome message |
| GET | `/api/health` | Health check |
| GET | `/api/projects/all` | List projects |
| GET | `/api/projects/{id}` | One project |
| POST | `/api/projects` | Create a project |
| PUT | `/api/projects/{id}` | Update a project |
| DELETE | `/api/projects/{id}` | Delete a project |

## Structure

```
src/main/java/org/example/
  config/        Spring Security configuration
  controller/    REST controllers
  dto/           request and response objects
  entity/        JPA entities: Project, Comment, ContactMessage, User
  exception/     error handling
  repository/    Spring Data repositories
  service/       business logic
src/test/java/   controller, repository and integration tests
```

## How to run

```bash
mvn spring-boot:run     # H2 in memory, port 8080
mvn test
```

With Docker:

```bash
docker build -t portfolio-backend .
docker run -p 8080:8080 portfolio-backend
```
