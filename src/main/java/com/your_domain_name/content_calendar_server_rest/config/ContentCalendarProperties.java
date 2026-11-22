package com.your_domain_name.content_calendar_server_rest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties (value="cc") //prefix cc
public record ContentCalendarProperties(String welcomeMessage, String about) {

}
