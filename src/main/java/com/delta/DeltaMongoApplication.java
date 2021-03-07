package com.delta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.web.WebAppConfiguration;

@SpringBootApplication(scanBasePackages = "com")
@WebAppConfiguration
public class DeltaMongoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeltaMongoApplication.class, args);
    }

}
