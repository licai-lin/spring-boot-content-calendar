# Generating Your Own Configuration Metadata Using the Annotation Processor

This guide uses the Content Calendar application's `ContentCalendarProperties` record and custom `cc` properties. The project already contains the processor configuration, properties record, and runtime registration described below.

Configuration metadata describes property names, Java types, and descriptions. Supporting IDEs use it for autocomplete and documentation in `application.properties`. Spring Boot generates this metadata during compilation from types annotated with `@ConfigurationProperties`.

## 1. Configure the annotation processor

In `pom.xml`, add the compiler plugin alongside the existing Spring Boot plugin, inside `<build><plugins>`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>

        <!-- Generate metadata for custom configuration properties -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-configuration-processor</artifactId>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

This makes the processor available to the Java compiler. No additional entry in `<dependencies>` is required with this approach. The project's Spring Boot parent manages the versions.

The plugin-level configuration applies to main and test compilation. Spring Initializr may instead put it inside an execution named `default-compile` to scope it to main compilation. Both approaches generate metadata for this application's main properties record.

## 2. Define your custom properties

File: `src/main/java/com/your_domain_name/content_calendar_server_rest/config/ContentCalendarProperties.java`

```java
package com.your_domain_name.content_calendar_server_rest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cc")
public record ContentCalendarProperties(
        String welcomeMessage,
        String about
) {
}
```

`prefix = "cc"` has the same meaning as the project's existing `value = "cc"`.

The processor derives these metadata names:

| Record component | Metadata property | Type |
| --- | --- | --- |
| `welcomeMessage` | `cc.welcome-message` | `java.lang.String` |
| `about` | `cc.about` | `java.lang.String` |

## 3. Register the properties for runtime binding

The application's `ContentCalendarApplication` already includes these annotations:

```java
@EnableConfigurationProperties(ContentCalendarProperties.class)
@SpringBootApplication
public class ContentCalendarApplication {
    // Existing application code
}
```

`@EnableConfigurationProperties` registers the record as a Spring bean and enables binding configuration values to it at runtime. The annotation processor separately generates metadata during compilation. Generating metadata does not register the bean.

## 4. Supply and use the application values

File: `src/main/resources/application.properties`

The current application defines:

```properties
cc.welcomeMessage=Welcome to Dan's Content Calendar
cc.about=This is Dan's content calendar
```

Spring Boot's relaxed binding maps `cc.welcomeMessage` to the record component `welcomeMessage`. The generated metadata uses the canonical name `cc.welcome-message`.

The current `HomeController` receives the properties bean through constructor injection:

```java
private final ContentCalendarProperties properties;

public HomeController(ContentCalendarProperties properties) {
    this.properties = properties;
}
```

Access individual values with the record's accessors:

```java
properties.welcomeMessage();
properties.about();
```

The controller's `/` endpoint currently returns this record. Its separate `@Value` fields are not needed to read values through the injected record, and those fields are not the source of this generated metadata.

## 5. Generate and inspect the metadata

From the project root, run:

```bash
./mvnw clean compile
```

After a successful build, inspect:

```text
target/classes/META-INF/spring-configuration-metadata.json
```

For example, the generated file should contain a property entry similar to:

```json
{
  "name": "cc.welcome-message",
  "type": "java.lang.String",
  "sourceType": "com.your_domain_name.content_calendar_server_rest.config.ContentCalendarProperties"
}
```

This is an illustrative excerpt, not the complete generated file. Compilation regenerates the file, so make changes in the source record rather than editing the generated JSON.

Values in `application.properties` supply runtime configuration; they are not automatically recorded as metadata defaults.

## 6. Add property descriptions

To include descriptions in the metadata, add class-level Javadoc with `@param` tags to the record:

```java
/**
 * Configuration for the content calendar.
 *
 * @param welcomeMessage welcome message displayed by the application
 * @param about description of the content calendar
 */
@ConfigurationProperties(prefix = "cc")
public record ContentCalendarProperties(
        String welcomeMessage,
        String about
) {
}
```

Recompile after updating the Javadoc. Supporting IDEs can display these descriptions alongside property suggestions.

## Troubleshooting

- If the metadata file is missing, confirm that compilation succeeded, the compiler plugin contains the processor path, and the record has `@ConfigurationProperties`.
- If IDE suggestions are missing, reload the Maven project and confirm that the IDE supports Spring Boot configuration metadata.
- If you add or rename record components, recompile to refresh the metadata.
- If the application cannot inject the properties bean, check `@EnableConfigurationProperties(ContentCalendarProperties.class)` separately from the processor configuration.

## References

- [Spring Boot: Generating Your Own Metadata by Using the Annotation Processor](https://docs.spring.io/spring-boot/specification/configuration-metadata/annotation-processor.html)
- [Maven: Configuring Default Plugin Executions](https://maven.apache.org/guides/mini/guide-default-execution-ids.html)
