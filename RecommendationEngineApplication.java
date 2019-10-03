package org.utdallas.atos.training.recommendationengine;

import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
@SpringBootApplication
public class RecommendationEngineApplication extends SpringBootServletInitializer
{
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(RecommendationEngineApplication.class);
    }
	public static void main(String[] args) {
		SpringApplication.run(RecommendationEngineApplication.class, args);
	}
}
