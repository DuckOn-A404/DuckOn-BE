package com.a404.duckonback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DuckOnBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(DuckOnBackApplication.class, args);
    }

}
