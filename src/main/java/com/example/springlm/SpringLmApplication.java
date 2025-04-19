package com.example.springlm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SpringLmApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringLmApplication.class, args);
    }

}
