package com.example.racetobuy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class RaceToBuyApplication {

    public static void main(String[] args) {
        SpringApplication.run(RaceToBuyApplication.class, args);
    }

}
