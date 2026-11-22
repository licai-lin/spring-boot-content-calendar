package com.your_domain_name.content_calendar_server_rest.config;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.your_domain_name.content_calendar_server_rest.model.Content;
import com.your_domain_name.content_calendar_server_rest.model.Status;
import com.your_domain_name.content_calendar_server_rest.model.Type;
import com.your_domain_name.content_calendar_server_rest.repository.ContentRepository;

// @Profile ("!dev")
@Component 
public class DataLoader implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {

        System.out.println("Hello!! ");
    }

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

}
