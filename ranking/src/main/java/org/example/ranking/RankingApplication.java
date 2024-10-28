package org.example.ranking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan(basePackages = {"org.example.common", "org.example.ranking"})
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class RankingApplication {

    public static void main(String[] args) {
        SpringApplication.run(RankingApplication.class, args);
    }

}
