# SQL Setup Guide for Content Calendar

> For the current Neon setup and the local H2 fallback, see
> [Neon database setup](neon-database-setup.md). The walkthrough below describes
> the earlier H2 learning setup.

This guide explains the current H2 setup, how to reproduce the result in your
screenshot, and what remains before the REST API stores content in SQL.

## 1. Understand the current setup

```text
Content Calendar
├── SQL database: H2
│   ├── Runs inside the Spring Boot application
│   ├── Connection URL: jdbc:h2:mem:content
│   ├── Stores data in memory for this application run
│   └── Needs no separate database server or Docker container
├── Database setup
│   ├── pom.xml → JDBC, H2, and H2 Console dependencies
│   ├── application.properties → database connection and console settings
│   └── schema.sql → creates Content and inserts one sample row
├── Browser console
│   └── http://localhost:8080/h2-console → run SQL directly
└── REST API: /api/content
    └── ContentController → ContentCollectionRepository → ArrayList<Content>
        └── Currently uses a separate Java list, with two sample posts
```

**Your screenshot confirms that H2 has a `CONTENT` table and the sample row.**
The API does not use that table yet. Changing a row in the console currently does
not change the API response, and API requests do not update H2.

SQL is the language used to query data. H2 is the database engine, JDBC connects
Java to the database, and the H2 Console is a browser interface for running SQL.

## 2. Check the required files

```text
content-calendar/
├── pom.xml
├── mvnw
└── src/main/
    ├── resources/
    │   ├── application.properties
    │   └── schema.sql
    └── java/com/your_domain_name/content_calendar_server_rest/
        ├── controller/ContentController.java
        ├── model/Content.java
        └── repository/ContentCollectionRepository.java
```

The SQL dependencies and configuration below are already present. Check them;
do not add duplicate dependencies.

### Step 1 — Include JDBC, H2, and the console

Inside `<dependencies>` in `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>

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

```text
Dependencies
├── spring-boot-starter-jdbc → database connections and JDBC query helpers
├── h2 → embedded database engine
└── spring-boot-h2console → browser console support in this Spring Boot 4 project
```

Save `pom.xml` and let VS Code reload the Maven project if you change it.

### Step 2 — Configure the connection

The relevant existing settings in `src/main/resources/application.properties` are:

```properties
spring.datasource.generate-unique-name=false
spring.datasource.name=content
spring.datasource.url=jdbc:h2:mem:content
spring.h2.console.enabled=true

logging.level.org.springframework.jdbc=DEBUG
```

```text
Connection settings
├── jdbc:h2:mem:content
│   ├── jdbc:h2 → H2 JDBC connection
│   ├── mem → in-memory storage
│   └── content → database name
├── generate-unique-name=false / name=content
│   └── Configure embedded database naming; the explicit URL selects this database
├── h2.console.enabled=true → enables the development console
└── JDBC DEBUG logging → optional troubleshooting output
```

No username or password is currently configured. For this embedded H2 setup,
connect with username `sa` and an empty password. To make those defaults explicit,
you can add:

```properties
spring.datasource.username=sa
spring.datasource.password=
```

Keep the console enabled only for local development. The commented PostgreSQL URL
in this file is inactive.

### Step 3 — Create the table and seed data

The existing `src/main/resources/schema.sql` contains:

```sql
CREATE TABLE IF NOT EXISTS Content (
    id INTEGER AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    desc TEXT,
    status VARCHAR(20) NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    date_created TIMESTAMP,
    url VARCHAR(255),
    PRIMARY KEY (id)
);

INSERT INTO Content (title, desc, status, content_type, date_created)
VALUES (
    'My Spring Data Blog Post',
    'A post about spring data',
    'IDEA',
    'ARTICLE',
    CURRENT_TIMESTAMP
);
```

```text
Application startup
├── Spring Boot opens the H2 connection
├── Runs schema.sql for this embedded in-memory database
│   ├── CREATE TABLE → defines columns and primary key
│   └── INSERT → creates the sample post
└── H2 Console can query the initialized table
```

`AUTO_INCREMENT` supplies the ID when an insert omits it. `CURRENT_TIMESTAMP`
supplies the insertion time. The sample insert omits `url`, so its value is `NULL`,
as shown in your screenshot. H2 displays unquoted table and column names in
uppercase, so `Content` appears as `CONTENT`.

This project keeps its sample insert in `schema.sql`. You can later move it into
`src/main/resources/data.sql`; Spring Boot runs that after the schema script.
Remove the original insert when moving it to avoid inserting it twice.

### Step 4 — Start the application

From the folder containing `pom.xml`:

```sh
./mvnw spring-boot:run
```

```text
Start application
├── Use the project's Java 17 target with a compatible installed JDK
├── Wait for the application startup message
├── Leave the process running
└── Open http://localhost:8080/h2-console
```

You can also start it using VS Code's Spring Boot Dashboard. Run only one instance
on port 8080.

### Step 5 — Log in to H2 Console

```text
H2 Console login
├── Driver Class: org.h2.Driver
├── JDBC URL: jdbc:h2:mem:content
├── User Name: sa
├── Password: leave empty
└── Click Connect
    ├── Expand CONTENT in the left panel to inspect columns
    ├── Enter SELECT * FROM CONTENT;
    └── Click Run
```

Use the console URL above rather than copying the session-specific `login.do`
URL from the screenshot. The JDBC URL must match the application's URL exactly.

Expected result after a fresh application start:

```text
CONTENT
└── One seeded row
    ├── ID: 1
    ├── TITLE: My Spring Data Blog Post
    ├── DESC: A post about spring data
    ├── STATUS: IDEA
    ├── CONTENT_TYPE: ARTICLE
    ├── DATE_CREATED: timestamp from initialization
    └── URL: NULL
```

## 3. Practice SQL in the console

Run these statements one at a time. The update and delete target only the practice
row added here.

```sql
-- Read all rows.
SELECT * FROM CONTENT;

-- Add a practice row; H2 generates its ID.
INSERT INTO CONTENT (title, desc, status, content_type, date_created, url)
VALUES (
    'SQL practice post', 'Learning JDBC and H2', 'IDEA', 'ARTICLE',
    CURRENT_TIMESTAMP, 'https://example.com/sql-practice'
);

-- Find the practice row and note its generated ID.
SELECT id, title, status FROM CONTENT WHERE title = 'SQL practice post';

-- Replace 2 with the practice row's actual ID before running these statements.
UPDATE CONTENT SET status = 'IN_PROGRESS' WHERE id = 2;
SELECT * FROM CONTENT WHERE id = 2;
DELETE FROM CONTENT WHERE id = 2;
```

The screenshot shows **Auto commit** enabled, so successful changes commit
automatically. An in-memory database still loses those changes when the
application stops. A fresh application start recreates the database and seed row.

## 4. Connect the REST API to SQL next

These are follow-up implementation steps; the application does not perform them
yet.

```text
Connect API storage
├── 1. Align schema.sql with model/Content.java
│   ├── Content has dateUpdated, but the table has no date_updated column
│   ├── Add date_updated TIMESTAMP if you intend to persist that field
│   └── Restart H2 after editing the schema to build a fresh table
├── 2. Replace ArrayList storage in ContentCollectionRepository
│   ├── Inject Spring's JdbcTemplate or JdbcClient through the constructor
│   ├── Remove the list and its @PostConstruct sample initialization
│   └── Keep the methods used by ContentController
├── 3. Implement database operations with parameterized SQL
│   ├── findAll → SELECT ... FROM Content
│   ├── findById → SELECT ... FROM Content WHERE id = ?
│   ├── save → INSERT INTO Content (...) VALUES (?, ...)
│   │   └── Omit id and retrieve the generated key when needed
│   ├── update → UPDATE Content SET ... WHERE id = ?
│   ├── existsById → SELECT COUNT(*) FROM Content WHERE id = ?
│   └── delete → DELETE FROM Content WHERE id = ?
├── 4. Map each result row to the Content record
│   ├── content_type → contentType
│   ├── date_created / date_updated → dateCreated / dateUpdated
│   ├── status / content_type strings → Status / Type enum values
│   └── Handle nullable timestamps before converting to LocalDateTime
├── 5. Check update IDs
│   └── Use the URL path ID consistently; currently update uses the body's ID
└── 6. Verify the complete flow
    ├── GET /api/content returns the SQL seed row
    ├── POST a new item, then SELECT it in H2 Console
    ├── PUT an item, then confirm its changed columns in H2
    └── DELETE an item, then confirm its row is gone
```

Bind request values as query parameters rather than concatenating them into SQL.
The current enum values are `IDEA`, `IN_PROGRESS`, `COMPLETED`, `PUBLISH` for
status, and `ARTICLE`, `VIDEO`, `COURSE`, `CONFEENCE_TALK` for type. The last value
is spelled that way in the current code; SQL strings must match the Java enums
when mapping them with `valueOf`.

## 5. Understand the optional PostgreSQL path

H2 is enough to reproduce your screenshot. PostgreSQL is a separate option for
server-based storage; simply adding its driver does not switch the application.

```text
Future PostgreSQL setup
├── pom.xml → PostgreSQL driver is already present
├── docker-compose.yml → contains a PostgreSQL service
│   ├── Correct POSTGRES_USERNAME to POSTGRES_USER
│   └── Plan persistent storage and choose an explicit image version
├── application.properties
│   ├── Select the PostgreSQL JDBC URL, username, and password
│   └── Disable the H2 development console
├── SQL scripts
│   ├── Adapt H2 syntax, including AUTO_INCREMENT, for PostgreSQL
│   ├── Review/rename desc to avoid SQL keyword conflicts
│   └── Choose initialization explicitly for the external database
│       ├── Learning setup: spring.sql.init.mode=always
│       └── Longer-lived setup: use versioned migrations
└── Seed data
    └── Make insertion repeatable; the current INSERT runs each initialization
```

`CREATE TABLE IF NOT EXISTS` does not modify an existing table and does not stop
the following sample insert from creating another row. Complete the JDBC
repository work in section 4 regardless of which database you choose.

## 6. Troubleshooting checklist

```text
Problem
├── /h2-console returns 404
│   └── Check the console dependency, enabled setting, and restart the application
├── Login fails or CONTENT is missing
│   ├── Match jdbc:h2:mem:content exactly; use sa and an empty password
│   ├── Keep the application running; use its own browser console
│   └── Check startup logs for schema.sql errors
├── SQL changes do not appear in /api/content
│   └── Expected today: the API still reads ContentCollectionRepository's list
├── Rows disappear after restarting
│   └── Expected for jdbc:h2:mem:content; initialization recreates the sample data
├── Changing schema.sql does not change the current table
│   └── Restart this in-memory setup; rerunning CREATE IF NOT EXISTS won't alter it
└── Application reports port 8080 is in use
    └── Stop the other application instance, then start this one again
```

## Official references

- [Spring Boot SQL database support and H2 Console](https://docs.spring.io/spring-boot/reference/data/sql.html)
- [Spring Boot database initialization and SQL scripts](https://docs.spring.io/spring-boot/how-to/data-initialization.html)
