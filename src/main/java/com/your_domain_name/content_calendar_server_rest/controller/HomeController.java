package com.your_domain_name.content_calendar_server_rest.controller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.your_domain_name.content_calendar_server_rest.config.ContentCalendarProperties;

@RestController
public class HomeController {

    @Value("${cc.welcomeMessage: Default Welcome Message }")
    private String welcomeMessage;

    @Value("${cc.about}")
    private String about;

    private final ContentCalendarProperties properties;

    public HomeController(ContentCalendarProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/")
    public ContentCalendarProperties home() {
        return properties;
    }
}
