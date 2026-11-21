package com.your_domain_name.content_calendar_server_rest.model;

import java.time.LocalDateTime;

public record Content(
    Integer id,
    String title,
    String desc,
    Status status,
    Type contentType,
    LocalDateTime dateCreated,
    LocalDateTime dateUpdated,
    String url

) {
    

}
