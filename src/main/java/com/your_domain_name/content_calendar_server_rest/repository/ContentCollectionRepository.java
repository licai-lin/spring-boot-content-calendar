package com.your_domain_name.content_calendar_server_rest.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import com.your_domain_name.content_calendar_server_rest.model.Content;
import com.your_domain_name.content_calendar_server_rest.model.Status;
import com.your_domain_name.content_calendar_server_rest.model.Type;

import jakarta.annotation.PostConstruct;

@Repository
public class ContentCollectionRepository {

    private final List<Content> contentList = new ArrayList<>();

    public ContentCollectionRepository() {

    }

    public List<Content> findAll() {
        return contentList;
    }

    public Optional<Content> findById(Integer id) {
        return contentList.stream().filter(c -> c.id().equals(id)).findFirst();
    }

    @PostConstruct
    private void init() {
        Content c1 = new Content(
                1,
                "My First Blog Post", "My First Blog Post",
                Status.IDEA, Type.ARTICLE, LocalDateTime.now(), null,
                "");

        Content c2 = new Content(
                2,
                "My Second Blog Post", "My Second Blog Post",
                Status.IDEA, Type.COURSE, LocalDateTime.now(), null,
                "");

        contentList.add(c1);
        contentList.add(c2);

    }

    public void save(Content content) {
        contentList.add(content);
    }

    public void update(Content content) {
        for (int i = 0; i < contentList.size(); i++) {
            if (contentList.get(i).id().equals(content.id())) {
                contentList.set(i, content);
                return;
            }
        }
    }

    public boolean existsById(Integer id) {
        return contentList.stream().filter(c -> c.id().equals(id)).count() == 1;
    }

    public void delete(Integer id) {
        contentList.removeIf(content -> content.id().equals(id));
    }


}
