package org.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Random;

@SpringBootApplication
public class PlaygroundApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlaygroundApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigures() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry
                        .addMapping("/api/**")
                        .allowedOrigins("*")
                        .allowedMethods("*");
            }
        };
    }

    @Bean
    public Random random() {
        return  new Random();
    }

}

