package com.your_domain_name.content_calendar_server_rest;

// import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
// import org.springframework.context.annotation.Bean;
// import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClient;

import com.your_domain_name.content_calendar_server_rest.config.ContentCalendarProperties;

@EnableConfigurationProperties (ContentCalendarProperties.class)
@SpringBootApplication
public class ContentCalendarApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = SpringApplication.run(ContentCalendarApplication.class, args);
		// Arrays.stream(ctx.getBeanDefinitionNames()).forEach(System.out::println);


		// SpringApplication.run(ContentCalendarApplication.class, args);

		// Object obj = ctx.getBean("restClient");
		// System.out.printf("obj:\t%s%n", obj);

		RestClient client = ctx.getBean("myRestClient", RestClient.class);
		System.out.printf("client:\t%s%n", client);
	
	}

}
