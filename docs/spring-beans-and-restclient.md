# Spring beans: one RestClient, multiple services

A **bean is an object that Spring creates and manages**. The **application context** is the container that holds these objects and connects them to the classes that need them.

Picture one `RestClient` object shared by multiple services:

```text
Spring application context
│
├── RestClient bean (name: "myRestClient")
│       ├── passed into ContentService
│       └── passed into UserService
│
├── ContentService bean
└── UserService bean
```

Both services receive the same `RestClient` instance. By default, Spring creates one instance per bean definition in an application context. This default scope is called **singleton**.

## 1. Register the RestClient bean

Your project's `config/MyWebConfig.java` defines this bean:

```java
package com.your_domain_name.content_calendar_server_rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class MyWebConfig {

    /**
     * The bean name is "myRestClient" by default.
     * After Spring starts, you can retrieve it from the application context:
     * RestClient client = ctx.getBean("myRestClient", RestClient.class);
     * System.out.printf("client:\t%s%n", client);
     */
    @Bean
    public RestClient myRestClient() {
        return RestClient.create();
    }
}
```

| Part | Meaning |
| --- | --- |
| `@Configuration` | Marks a class that provides Spring configuration. |
| `@Bean` | Registers the returned object as a Spring-managed bean. |
| `public` | Makes the method accessible to other classes. |
| `RestClient` | The method's return type; Spring can inject this bean where a `RestClient` is needed. |
| `myRestClient` | A method name you choose. It also becomes the default bean name. |
| `()` | This method takes no arguments. |
| `RestClient.create()` | Creates the HTTP client object returned to Spring. |

`myRestClient` is **not a Java keyword**. Renaming it to `apiClient` would change the default bean name to `"apiClient"`; the object's type would still be `RestClient`.

## 2. Use the same bean in multiple services

Put each public class below in its own file under your application's base package. Spring discovers `@Service` classes in that package and its subpackages.

**ContentService.java**

```java
package com.your_domain_name.content_calendar_server_rest;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ContentService {

    private final RestClient client;

    public ContentService(RestClient client) {
        this.client = client;
    }

    public String fetchContent() {
        return client.get()
                .uri("https://example.com/api/content")
                .retrieve()
                .body(String.class);
    }
}
```

**UserService.java**

```java
package com.your_domain_name.content_calendar_server_rest;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class UserService {

    private final RestClient client;

    public UserService(RestClient client) {
        this.client = client;
    }

    public String fetchUsers() {
        return client.get()
                .uri("https://example.com/api/users")
                .retrieve()
                .body(String.class);
    }
}
```

The URLs are placeholders; replace them with real API endpoints before calling these methods.

When the application starts, Spring creates the `RestClient` bean and supplies it to each service's constructor. This is **constructor dependency injection**. With a single constructor, `@Autowired` is unnecessary.

You normally let Spring create these services too, rather than constructing them yourself with `new ContentService(...)`.

## 3. How does Spring connect the objects?

With one matching bean, Spring uses the **type**:

```text
@Bean method returns:       RestClient
                               ↕ matches
Service constructor needs:  RestClient
```

These names do not have to match:

```java
// Bean factory method name: myRestClient
public RestClient myRestClient() { ... }

// Constructor parameter name: client
public ContentService(RestClient client) { ... }
```

`ContentService` does not need to import or call `MyWebConfig`. Spring connects them through the application context.

## 4. When do you use the name "myRestClient"?

### Explicit lookup from the application context

In your application's `main` method, after starting Spring:

```java
ConfigurableApplicationContext ctx =
        SpringApplication.run(ContentCalendarApplication.class, args);

RestClient client = ctx.getBean("myRestClient", RestClient.class);
System.out.printf("client:\t%s%n", client);
```

Import `org.springframework.context.ConfigurableApplicationContext` and `org.springframework.web.client.RestClient` if needed. `SpringApplication` is from `org.springframework.boot`.

- `"myRestClient"` selects the bean by name.
- `RestClient.class` specifies the expected type, so the result can be assigned directly to a `RestClient` variable.
- The print statement displays the object's string representation. It does **not** call an API.

This lookup is useful for learning or inspecting beans. Application services usually use constructor injection instead.

### Selecting a bean when several have the same type

If you later define multiple `RestClient` beans, explicitly select one with `@Qualifier`:

```java
import org.springframework.beans.factory.annotation.Qualifier;

public ContentService(@Qualifier("myRestClient") RestClient client) {
    this.client = client;
}
```

If you rename the bean method, update any matching string names in `getBean(...)` and `@Qualifier(...)`. With only one matching bean, constructor injection by type continues to work after renaming the method.

## 5. APIs versus databases

A service coordinates your application's work. It can use different beans for different jobs:

```text
Controller → Service → RestClient → External HTTP API
                    └→ Repository → Database
```

`RestClient` makes HTTP requests. For database reads and writes, a service typically uses a repository, such as a Spring Data JPA repository.

## Quick review

- **Bean:** an object Spring manages.
- **Application context:** the container holding those beans.
- **`@Bean` method:** tells Spring how to create a bean.
- **Method name:** becomes the default bean name, such as `"myRestClient"`.
- **Constructor injection:** asks Spring for a dependency, normally by type.
- **One RestClient, multiple services:** each service can receive the same shared bean.
- **Multiple RestClient beans:** use `@Qualifier` when you need to select a specific one.
