# Content Calendar

A Spring Boot REST API for organizing articles, videos, courses, and conference
talks. Create, update, delete, and filter content by title or publishing status.
The application uses H2 for local development and Neon PostgreSQL in production,
with container deployment to Vercel through GitHub Actions.

This learning project follows Dan Vega's YouTube course,
**Spring Boot Tutorial for Beginners - Crash Course using Spring Boot 3**.
The tutorial uses Spring Boot 3; this project currently uses Spring Boot 4.2.0.
As additional practice, the project integrates Neon PostgreSQL for production
storage and deploys to Vercel using Docker and GitHub Actions CI/CD.
The [`docs/`](docs/) folder contains notes taken while learning, along with
setup guides documenting the database integration and deployment process.

**Deployed API:** <https://spring-boot-content-calendar.vercel.app/api/content>

## Technology

- Java 17 and Spring Boot 4.2.0
- Spring Web MVC and Jakarta Bean Validation
- Spring Data JDBC and the PostgreSQL JDBC driver
- H2 with a browser console for local development and tests
- Neon PostgreSQL for persistent production storage
- Docker, GitHub Actions, and Vercel

## Run locally

Install JDK 17 or a compatible newer JDK. The repository includes the Maven
Wrapper, so a separate Maven installation is not required.

```sh
git clone https://github.com/licai-lin/spring-boot-content-calendar.git
cd spring-boot-content-calendar
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd` instead of `./mvnw`.

The default `development` profile includes the `h2` profile. No database server,
Docker installation, or credentials are needed for this mode.

| Resource | Local URL |
| --- | --- |
| Application information | <http://localhost:8080/> |
| Content API | <http://localhost:8080/api/content> |
| H2 Console | <http://localhost:8080/h2-console> |
| Health endpoint | <http://localhost:8080/actuator/health> |

Use these settings in H2 Console:

| Field | Value |
| --- | --- |
| Driver Class | `org.h2.Driver` |
| JDBC URL | `jdbc:h2:mem:content;MODE=PostgreSQL` |
| User Name | `sa` |
| Password | Leave empty |

H2 stores data in memory, so data is lost when the application stops.

## Run with Neon PostgreSQL

The production connection settings are in
[`application-production.properties`](src/main/resources/application-production.properties).
The password comes from the `NEON_DATABASE_PASSWORD` environment variable.
No `.env` file is used.

Set the password in your IDE's run configuration or enter it in zsh:

```sh
read -rs 'NEON_DATABASE_PASSWORD?Neon password: '; echo
export NEON_DATABASE_PASSWORD
./mvnw spring-boot:run -Dspring-boot.run.profiles=production
```

The production profile uses port **9090** unless `PORT` is set. Open
<http://localhost:9090/api/content> to test the API. This mode connects to the
actual Neon database, and API writes persist there. H2 Console is disabled.

For another Neon database, update the JDBC URL and username in the production
properties and supply its password through the environment.

See the [Neon PostgreSQL guide](docs/neon-database-setup.md) for dependencies,
connection settings, profiles, and troubleshooting.

## Database initialization

At startup, [`schema.sql`](src/main/resources/schema.sql) creates the content
table if it is missing. `DataLoader` then reads
[`data/content.json`](src/main/resources/data/content.json) when the table is empty.

Initial data loading is enabled for `production`, `development`, and `h2`.
A nonempty table is left unchanged on normal sequential restarts. Editing the
JSON file does not update existing database rows. The empty-table check does
not coordinate simultaneous application starts.

`CREATE TABLE IF NOT EXISTS` does not migrate existing tables when the schema changes.

## API endpoints

Use `http://localhost:8080` for local H2, `http://localhost:9090` for local
production with `PORT` unset, or the deployed Vercel domain as the base URL.

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/` | Read the welcome and about messages |
| `GET` | `/api/content` | List all content |
| `GET` | `/api/content/{id}` | Find content by ID |
| `POST` | `/api/content` | Create content; returns `201 Created` |
| `PUT` | `/api/content/{id}` | Update content; returns `204 No Content` |
| `DELETE` | `/api/content/{id}` | Delete content; returns `204 No Content` |
| `GET` | `/api/content/filter/{keyword}` | Find content whose title contains the keyword |
| `GET` | `/api/content/filter/status/{status}` | Filter by publishing status |
| `GET` | `/actuator/health` | Check application health |

For updates, include an `id` in the request body that matches the path ID;
the current controller passes the body directly to the repository for saving.

### Create content

```sh
curl -i -X POST http://localhost:8080/api/content \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Getting started with Spring Boot",
    "description": "A practical introduction to building a REST API.",
    "status": "IDEA",
    "contentType": "ARTICLE",
    "dateCreated": "2026-11-23T10:00:00",
    "dateUpdated": null,
    "url": "https://example.com/spring-boot"
  }'
```

Omit the ID when creating content so the database generates it. The title must
not be blank. The create response has no body; use the list endpoint to retrieve
the saved content and its generated ID.

### Filter content

```sh
curl http://localhost:8080/api/content/filter/GraphQL
curl http://localhost:8080/api/content/filter/status/PUBLISH
```

| Field | Accepted values |
| --- | --- |
| `status` | `IDEA`, `IN_PROGRESS`, `COMPLETED`, `PUBLISH` |
| `contentType` | `ARTICLE`, `VIDEO`, `COURSE`, `CONFEENCE_TALK` |

`CONFEENCE_TALK` is the spelling currently defined in the Java enum.

## Build and test

```sh
./mvnw --batch-mode --no-transfer-progress verify
```

Tests use H2 through `@ActiveProfiles("h2")` and do not require Neon credentials.
The build generates `target/content-calendar-0.0.1-SNAPSHOT.jar`.

To run the packaged application locally with H2:

```sh
java -jar target/content-calendar-0.0.1-SNAPSHOT.jar
```

## Deployment and CI/CD

[`Dockerfile.vercel`](Dockerfile.vercel) builds the JAR with Maven and copies it
into a Java 17 runtime image. The image defaults to the production profile and
internal port **8080**. `ENTRYPOINT` runs the packaged application when the
container starts.

The workflows are split into two files:

| Workflow | Behavior |
| --- | --- |
| [CI](.github/workflows/ci.yml) | Runs Maven verification for pushes and pull requests to `main` |
| [CD](.github/workflows/deploy.yml) | Deploys the exact tested commit after successful CI for a push or manual CI run on `main` |

Failed or cancelled CI does not deploy. Pull requests only run CI. Changes only
to `docs/` and/or root `README*` / `readme*` files skip both workflows. Manual CI
runs remain available; successful manual CI on `main` triggers CD.

Vercel needs these **Production environment variables**:

| Name | Value |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `production` |
| `PORT` | `8080` |
| `NEON_DATABASE_PASSWORD` | Your Neon database password |

GitHub Actions needs these **repository secrets**:

| Name | Value |
| --- | --- |
| `VERCEL_TOKEN` | Vercel access token for the owning team |
| `VERCEL_ORG_ID` | Vercel Team ID |
| `VERCEL_PROJECT_ID` | Vercel Project ID |

Native Vercel Git deployments are disabled in [`vercel.json`](vercel.json), so
GitHub Actions controls automatic production deployment after CI succeeds.

Follow the [GitHub and Vercel deployment guide](docs/vercel-deployment.md) for
the complete dashboard setup: push to GitHub, import into Vercel, configure
environment variables, add GitHub secrets, and push a description change to
verify CI/CD.

## Project structure

```text
.github/workflows/       CI and CD workflows
src/main/java/          Controllers, models, repositories, and configuration
src/main/resources/     Application profiles, SQL schema, and JSON seed data
src/test/java/          Application context test
Dockerfile.vercel       Container build and application startup
vercel.json             Vercel container and Git deployment settings
docs/                   Setup guides and development notes
```

## Guides

- [Neon PostgreSQL integration](docs/neon-database-setup.md)
- [GitHub Actions and Vercel deployment](docs/vercel-deployment.md)
- [Spring Boot profiles](docs/spring-boot-profiles-guide.md)
- [Spring Boot with VS Code](docs/spring-boot-vscode-setup.md)
