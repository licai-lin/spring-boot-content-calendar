# Spring Boot profiles: development and production

This guide uses one consistent setup: keep `development` in the base configuration,
and override it when starting the application in production.

## 1. The setup to remember

Keep this in `src/main/resources/application.properties`, including when building
and deploying the application:

```properties
spring.profiles.active=development
```

```text
+---------------------------------------------------------+
| application.properties                                  |
| spring.profiles.active=development                       |
| Base settings and the profile used without an override   |
+----------------------------+----------------------------+
                             |
                             v
+---------------------------------------------------------+
| Does startup supply SPRING_PROFILES_ACTIVE?              |
+----------------------------+----------------------------+
                             |
             +---------------+---------------+
             |                               |
             v                               v
     No environment override          Set to production
             |                               |
             v                               v
     development profile              production profile
             |                               |
             v                               v
 application-development.properties  application-production.properties
```

This flow assumes no other profile overrides, such as a command-line argument.
If the production environment variable is missing, this setup uses **development**.

## 2. How the three configuration files work

```text
src/main/resources/
|-- application.properties              Shared settings + development selection
|-- application-development.properties  Development settings
`-- application-production.properties   Production settings
```

Spring Boot loads the base settings and then the active profile's settings.
Matching settings in the profile file override those in the base file.

| Active profile | Configuration loaded |
| --- | --- |
| `development` | `application.properties` + `application-development.properties` |
| `production` | `application.properties` + `application-production.properties` |

For example:

```properties
# application.properties
spring.profiles.active=development
server.port=8080
```

```properties
# application-production.properties
server.port=9090
```

With the `production` profile active, the port is `9090`. Base settings that are
not overridden still apply. The development file is not loaded in this example.

Use the exact names: `production` selects `application-production.properties`;
`prod` would select `application-prod.properties` instead.

Keep `spring.profiles.active` in the base file or supply it externally. Do not
put it inside a profile-specific file.

## 3. Which setting wins?

For the configuration methods used in this guide, priority is highest first:

```text
+---------------------------------------------------------+
| 1. Application command-line argument                     |
|    --spring.profiles.active=production                   |
+---------------------------------------------------------+
| 2. Environment variable                                 |
|    SPRING_PROFILES_ACTIVE=production                     |
+---------------------------------------------------------+
| 3. Base application.properties                          |
|    spring.profiles.active=development                    |
+---------------------------------------------------------+
```

This is a simplified list; Spring supports other configuration sources too.

| Base file | Startup environment variable | Active profile |
| --- | --- | --- |
| `development` | Not set | `development` |
| `development` | `development` | `development` |
| `development` | `production` | `production` |

`spring.profiles.active` is the Spring property name.
`SPRING_PROFILES_ACTIVE` is its environment variable form.

## 4. Toggle the profile in VS Code

Edit `.vscode/launch.json`. Keep your existing configuration and change only the
value in its `env` section.

**Run with development settings:**

```json
"env": {
    "SPRING_PROFILES_ACTIVE": "development"
}
```

**Run with production settings:**

```json
"env": {
    "SPRING_PROFILES_ACTIVE": "production"
}
```

These are fragments inside the launch configuration, not complete JSON files.
Here is a complete example matching this project's main class:

```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Spring Boot-ContentCalendarApplication<content-calendar>",
            "request": "launch",
            "cwd": "${workspaceFolder}",
            "mainClass": "com.your_domain_name.content_calendar_server_rest.ContentCalendarApplication",
            "projectName": "content-calendar",
            "args": "",
            "envFile": "${workspaceFolder}/.env",
            "env": {
                "SPRING_PROFILES_ACTIVE": "development"
            }
        }
    ]
}
```

To apply a change:

1. Save `launch.json`.
2. Stop the running application.
3. Open **Run and Debug** (`Cmd + Shift + D` on macOS).
4. Select this configuration from the dropdown.
5. Press **F5** to debug, or choose **Run > Run Without Debugging**.

```text
Selected VS Code launch configuration
                  |
                  v
Passes env values to the Java process
                  |
                  v
Overrides the profile in application.properties
                  |
                  v
Spring loads the selected profile's settings
```

Keep `SPRING_PROFILES_ACTIVE` out of `.env` when using `launch.json` as your toggle.
That gives you one local place to control it. `.env` can still hold other local
variables. If you keep `envFile`, keep the referenced local file available.

The project's Git ignore rules exclude `.vscode/` and `.env`, so these local
settings are not included in normal commits. Spring Boot itself does not
automatically load `.env`; the VS Code debugger loads the referenced file.

Selecting the production profile locally uses production configuration on your
computer; it does not deploy the application.

## 5. GitHub Actions: build first, select production at startup

The active Spring profile is selected when Spring starts. A normal Maven build
packages the base and profile files into the same JAR.

```text
+---------------------------------------------------------+
| Source code                                             |
| application.properties: development                     |
| Includes development and production configuration files |
+----------------------------+----------------------------+
                             |
                             v
+---------------------------------------------------------+
| GitHub Actions build                                    |
| ./mvnw clean package                                    |
| Produces the application JAR                            |
+----------------------------+----------------------------+
                             |
                             v
+---------------------------------------------------------+
| Deploy the JAR to the production host                    |
+----------------------------+----------------------------+
                             |
                             v
+---------------------------------------------------------+
| Start on the production host with                       |
| SPRING_PROFILES_ACTIVE=production                       |
+----------------------------+----------------------------+
                             |
                             v
+---------------------------------------------------------+
| Environment overrides the packaged development setting  |
| Spring activates production                             |
+---------------------------------------------------------+
```

A build step in `.github/workflows/<your-workflow>.yml` can simply be:

```yaml
- name: Build and test
  run: ./mvnw clean package
```

This is a step fragment for a job that already checks out the repository and
sets up Java. No production profile is required just to package the JAR.
Tests that start Spring may use the base development profile unless they
explicitly select another profile.

You can set an environment variable in an Actions step:

```yaml
- name: Build and test with production profile
  env:
    SPRING_PROFILES_ACTIVE: production
  run: ./mvnw clean package
```

Use that only if you intend Spring processes started by that step, including
applicable tests, to use production settings. It **does not save the environment
variable into the JAR**. It also does not configure a separate production server.

Your deployment step must configure the destination's runtime environment using
the mechanism supported by that host: service configuration, container settings,
or the hosting platform's environment variable settings.

## 6. Start the application in production

On a Linux server, the equivalent direct startup command is:

```sh
SPRING_PROFILES_ACTIVE=production java -jar app.jar
```

Replace `app.jar` with your built JAR's filename. For a managed service, configure
the variable in that service so it is also present after restarts.

Alternatively, a command-line argument selects the profile:

```sh
java -jar app.jar --spring.profiles.active=production
```

Choose one method for the production startup configuration. You do not need both.

If deploying with Docker, pass the variable to the container at runtime:

```sh
docker run -e SPRING_PROFILES_ACTIVE=production your-image:tag
```

Replace the image name and add your deployment's required ports and other settings.

## 7. Confirm the selected profile

After restarting, check the Spring Boot startup logs for a message like:

```text
The following 1 profile is active: "production"
```

Exact wording can vary. Verify the profile before treating the deployment as
ready. If it reports `development`, check that the production startup process
actually received the environment variable and that no command-line argument
overrides it.

## 8. Quick reference

| Situation | Where to set the profile | Value |
| --- | --- | --- |
| Base configuration, kept in Git | `application.properties` | `spring.profiles.active=development` |
| Local development in VS Code | Selected `launch.json` configuration's `env` | `SPRING_PROFILES_ACTIVE=development` |
| Local check using production settings | Selected `launch.json` configuration's `env` | `SPRING_PROFILES_ACTIVE=production` |
| GitHub Actions packaging | No production override needed | Build the same JAR |
| Production startup | Production host, service, or container environment | `SPRING_PROFILES_ACTIVE=production` |

Keep passwords and API keys out of committed configuration files. Supply those
through the runtime environment or the hosting platform's secret settings.

## References

- [Spring Boot profiles](https://docs.spring.io/spring-boot/reference/features/profiles.html)
- [Spring Boot configuration precedence](https://docs.spring.io/spring-boot/reference/features/external-config.html)
- [VS Code Java launch configurations](https://code.visualstudio.com/docs/java/java-debugging)
- [GitHub Actions environment variables](https://docs.github.com/en/actions/how-tos/write-workflows/choose-what-workflows-do/use-variables)
