package com.your_domain_name.content_calendar_server_rest.repository;

import java.util.List;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import com.your_domain_name.content_calendar_server_rest.model.Content;
import com.your_domain_name.content_calendar_server_rest.model.Status;

public interface ContentRepository extends ListCrudRepository<Content, Integer> {

    @Query("SELECT * FROM content WHERE title LIKE CONCAT('%', :keyword, '%')")
    List<Content> findAllByTitleContains(@Param("keyword") String keyword);

    @Query("SELECT * FROM content WHERE status = :status")
    List<Content> listByStatus(@Param("status") Status status);

}
