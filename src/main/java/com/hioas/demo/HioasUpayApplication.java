package com.hioas.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HioasUpayApplication {
    public static void main(String[] args) {
        SpringApplication.run(HioasUpayApplication.class, args);
    }
}
