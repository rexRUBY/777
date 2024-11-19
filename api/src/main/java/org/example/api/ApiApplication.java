package org.example.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = {"org.example"})
@EntityScan("org.example.common")
@EnableJpaAuditing
@EnableJpaRepositories(
        basePackages = {
                "org.example.common.wallet.repository",
                "org.example.common.crypto.repository",
                "org.example.common.user.repository",
                "org.example.common.ranking.repository",
                "org.example.common.subscriptions.repository",
                "org.example.common.trade.repository"
        }
)
@EnableMongoRepositories(
        basePackages = "org.example.common.user.mongo" // MongoDB 리포지토리 패키지
)
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}