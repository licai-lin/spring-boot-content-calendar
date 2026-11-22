# Neon PostgreSQL and local H2

The `production` profile connects to Neon using the settings stored directly in
`src/main/resources/application-production.properties`. The default `development`
profile includes the `h2` profile for local development. The original H2 settings
remain commented out in `application.properties`.

The password is supplied by the `NEON_DATABASE_PASSWORD` environment variable;
no `.env` file is used. See [GitHub and Vercel deployment](vercel-deployment.md)
for the hosting setup.

## Local development with Neon

Select the production profile to check Neon from your local machine:

Set `NEON_DATABASE_PASSWORD` in your terminal or IDE run configuration first.
To enter it in zsh without displaying it or saving it in command history:

```sh
read -rs 'NEON_DATABASE_PASSWORD?Neon password: '; echo
export NEON_DATABASE_PASSWORD
```

```sh
./mvnw spring-boot:run -Dspring-boot.run.profiles=production
```

The app listens on port 9090. It uses the actual Neon database, so API changes
persist there. Both production and development load initial content from
`data/content.json` on startup when the content table is empty. A nonempty table
is left unchanged. The production profile disables the H2 console.

## Local development with H2

Run with the default development profile to use H2:

```sh
./mvnw spring-boot:run
```

Open <http://localhost:8080/h2-console> and connect with:

- Driver: `org.h2.Driver`
- JDBC URL: `jdbc:h2:mem:content;MODE=PostgreSQL`
- Username: `sa`
- Password: leave empty

H2 data is discarded when the app stops. Automated tests select this profile
so they do not depend on Neon or write to it. There is no automatic database
switch when a Neon connection fails.

## Production

Use the production profile with the database settings already configured in
`application-production.properties`:

```sh
java -jar target/content-calendar-0.0.1-SNAPSHOT.jar --spring.profiles.active=production
```

The production profile uses `PORT` when set, otherwise port 9090. Do not enable
the `h2` profile in production.

The existing `spring.sql.init.mode=always` setting runs `schema.sql` on startup.
Its `CREATE TABLE IF NOT EXISTS` creates a missing table; it does not migrate an
existing table when columns change.

Spring uses a JDBC URL with separate credentials. Its PostgreSQL driver uses
`channelBinding`, whereas Neon's supplied PostgreSQL URI uses `channel_binding`.
The Prisma comments and unpooled URI are not needed for this Spring application.

References: [pgJDBC connection settings](https://jdbc.postgresql.org/documentation/use/)
and [Spring Boot external configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html).
