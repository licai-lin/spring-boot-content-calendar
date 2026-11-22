# Guide 1: Integrate Spring Boot with Neon PostgreSQL

This guide documents the Content Calendar application's working database setup:
Neon PostgreSQL for production and H2 with its console for local development.
Configuration stays in Spring's application properties files; no `.env` or
`.env.example` file is used.

After configuring the database, continue with
[Guide 2: Deploy through GitHub and Vercel](vercel-deployment.md).

## 1. Required PostgreSQL dependencies

The following dependencies are already present in `pom.xml`. Do not add them twice.

```xml
<!-- Spring Data repositories, JDBC support, and connection pooling -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jdbc</artifactId>
</dependency>

<!-- PostgreSQL JDBC driver, also used to connect to Neon -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

| Dependency | Purpose |
| --- | --- |
| `spring-boot-starter-data-jdbc` | Provides Spring Data JDBC repositories and database connection support |
| `postgresql` | Lets Java communicate with a PostgreSQL server through JDBC |

Spring Boot's parent manages their dependency versions. Neon uses PostgreSQL;
this app does not need a separate Neon SDK, Prisma, or Spring Data JPA.
The repository extends `ListCrudRepository<Content, Integer>`.

For the optional local H2 setup, keep these existing dependencies too:

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-h2console</artifactId>
    <scope>runtime</scope>
</dependency>
```

`spring-boot-h2console` is the console module used by this Spring Boot 4 project.

## 2. Get the connection details from Neon

Create or open your Neon project and use its connection dialog to obtain the
hostname, database name, username, and password. This project uses the pooled
hostname, which contains `-pooler`.

A Neon connection URI looks like this, with placeholders instead of credentials:

```text
postgresql://<username>:<password>@<pooled-host>/<database>?sslmode=require&channel_binding=require
```

For Spring JDBC, use this format instead:

```text
jdbc:postgresql://<pooled-host>:5432/<database>?sslmode=require&channelBinding=require
```

Keep username and password in separate datasource properties. The Java driver's
parameter is `channelBinding`, while the supplied PostgreSQL URI uses
`channel_binding`. The Prisma comments and unpooled connection string are not
needed for this application's current configuration.

## 3. Configure the production profile

The database configuration lives in
`src/main/resources/application-production.properties`:

```properties
cc.welcomeMessage=This is production!!!
cc.about=Content Calendar integrated with Neon PostgreSQL for production.

server.port=${PORT:9090}
spring.h2.console.enabled=false

spring.datasource.url=jdbc:postgresql://ep-sparkling-cherry-bdgcq3f0-pooler.c-8.us-east-2.aws.neon.tech:5432/neondb?sslmode=require&channelBinding=require
spring.datasource.username=neondb_owner
spring.datasource.password=${NEON_DATABASE_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=never
logging.level.org.springframework.jdbc=INFO
```

For another Neon project, replace the hostname, database, and username with its
connection details. Keep the password as an environment-variable reference.
Set `NEON_DATABASE_PASSWORD` in your IDE, terminal, or hosting platform.

`server.port=${PORT:9090}` uses the value of `PORT` when available; otherwise,
it uses `9090`. Local production runs normally use 9090, while the Vercel
container uses 8080.

## 4. Keep local development on H2

The shared `src/main/resources/application.properties` includes:

```properties
spring.sql.init.mode=always
spring.profiles.default=development
spring.profiles.group.development=h2

# Original H2 settings are preserved as comments.
# spring.datasource.generate-unique-name=false
# spring.datasource.name=content
# spring.h2.console.enabled=true
# spring.datasource.url=jdbc:h2:mem:content
```

The default `development` profile includes the `h2` profile. Its active database
settings live in `src/main/resources/application-h2.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:content;MODE=PostgreSQL
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.generate-unique-name=false
spring.datasource.name=content
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

| Run mode | Database | Port | H2 Console |
| --- | --- | --- | --- |
| Default local development | In-memory H2 | 8080 | Enabled |
| Explicit local production profile | Neon PostgreSQL | 9090 unless `PORT` is set | Disabled |
| Production on Vercel | Neon PostgreSQL | 8080 internally | Disabled |

There is no automatic switch to H2 when Neon fails. Choose the intended profile
when starting the application.

## 5. Create the table and load initial JSON data

`spring.sql.init.mode=always` runs `src/main/resources/schema.sql` at startup,
including for the external PostgreSQL database. The current schema is:

```sql
CREATE TABLE IF NOT EXISTS Content (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    date_created TIMESTAMP,
    date_updated TIMESTAMP,
    url VARCHAR(255)
);
```

`SERIAL` generates IDs for inserted rows. H2's PostgreSQL compatibility mode also
supports this schema. `CREATE TABLE IF NOT EXISTS` creates a missing table; it
does not migrate an existing table when columns change.

After initialization, `DataLoader` reads
`src/main/resources/data/content.json` and saves content through the repository.
Its annotation keeps loading available in both environments:

```java
@Profile({"production", "development", "h2"})
@Component
public class DataLoader implements CommandLineRunner {
    // ...
}
```

The profile list means any one of the listed profiles enables the component.
The loader checks the table before inserting:

```java
if (repository.count() > 0) {
    return;
}
```

An empty table receives the JSON content; a nonempty table is left unchanged.
Normal sequential restarts therefore skip loading existing data. Editing the
JSON file does not update rows already in Neon. This empty-table check is not a
lock between simultaneously starting instances.

## 6. Test locally with Neon

Set `NEON_DATABASE_PASSWORD` in your IDE run configuration, or enter it in zsh
without displaying the password or placing it in shell history:

```sh
read -rs 'NEON_DATABASE_PASSWORD?Neon password: '; echo
export NEON_DATABASE_PASSWORD
```

Start the production profile:

```sh
./mvnw spring-boot:run -Dspring-boot.run.profiles=production
```

With `PORT` unset, check these URLs:

```text
http://localhost:9090/actuator/health
http://localhost:9090/api/content
http://localhost:9090/api/content/filter/Building
```

This connects to the actual Neon database. API writes persist there.

## 7. Run local development and automated checks

Start local H2:

```sh
./mvnw spring-boot:run
```

Open <http://localhost:8080/h2-console> and use:

| Field | Value |
| --- | --- |
| Driver Class | `org.h2.Driver` |
| JDBC URL | `jdbc:h2:mem:content;MODE=PostgreSQL` |
| User Name | `sa` |
| Password | Leave empty |

H2 data is discarded when the application stops. A fresh run initializes and
seeds the empty database again.

Build and test:

```sh
./mvnw --batch-mode --no-transfer-progress verify
```

The context test uses `@ActiveProfiles("h2")`, so automated checks use H2 and do
not need Neon credentials.

## Troubleshooting

| Problem | Check |
| --- | --- |
| `NEON_DATABASE_PASSWORD` cannot be resolved | Set the environment variable before starting the production profile |
| Authentication fails | Check the Neon username and current password |
| Connection fails | Check the hostname, network access, database, and SSL parameters |
| H2 Console returns 404 in production | Expected: the production profile disables it |
| JSON changes do not appear in Neon | The loader skips a nonempty table |
| A schema edit does not change an existing table | Use an explicit schema migration; `CREATE TABLE IF NOT EXISTS` does not alter it |

## References

- [PostgreSQL JDBC connection settings](https://jdbc.postgresql.org/documentation/use/)
- [Spring Boot database initialization](https://docs.spring.io/spring-boot/how-to/data-initialization.html)
- [Spring Boot external configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html)
