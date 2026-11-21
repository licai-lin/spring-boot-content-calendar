package com.your_domain_name.content_calendar_server_rest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class MyWebConfig {

    /**
     * The bean name is "myRestClient"
     * RestClient client = ctx.getBean("myRestClient", RestClient.class);
		System.out.printf("client:\t%s%n", client);
     */
    
    @Bean
    public RestClient myRestClient() {
        return RestClient.create();
    }

}
