package com.your_domain_name.content_calendar_server_rest.config;

import java.io.InputStream;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.your_domain_name.content_calendar_server_rest.model.Content;
import com.your_domain_name.content_calendar_server_rest.repository.ContentRepository;


import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;


@Profile({"production", "development", "h2"})
@Component 
public class DataLoader implements CommandLineRunner {

    private final ContentRepository repository;
    private final ObjectMapper objectMapper;

    public DataLoader(ContentRepository repository, ObjectMapper objectMapper){
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {

        if (repository.count() > 0) {
            return;
        }

        try (InputStream inputStream = DataLoader.class.getResourceAsStream("/data/content.json")) {
            if (inputStream == null) {
                throw new IllegalStateException("Missing initial content resource: /data/content.json");
            }
            repository.saveAll(objectMapper.readValue(inputStream, new TypeReference<List<Content>>() {}));
        }
    }

    /**
     * 
     * @param repository
     * @return

    @Bean
    CommandLineRunner conmandLineRunner(ContentRepository repository) {
        return ars -> {
            // System.out.println("hello dan");

            if (repository.count() == 0) {
                LocalDateTime now = LocalDateTime.now();
                repository.saveAll(List.of(
                        new Content(null, "My First Blog Post", "An introduction to Spring Boot.",
                                Status.IDEA, Type.ARTICLE, now, null, "https://example.com/blog/first-post"),
                        new Content(null, "Spring Data JDBC Tutorial",
                                "Build a content repository with Spring Data JDBC.",
                                Status.IN_PROGRESS, Type.VIDEO, now, null,
                                "https://example.com/videos/spring-data-jdbc"),
                        new Content(null, "REST API Fundamentals", "Learn how to build REST APIs with Spring Boot.",
                                Status.COMPLETED, Type.COURSE, now, null, "https://example.com/courses/rest-api")));
            }

        };

    }
     */
}
