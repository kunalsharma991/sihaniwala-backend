package com.sihaniwala.foundationbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FoundationBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(FoundationBackendApplication.class, args);
    }
}
