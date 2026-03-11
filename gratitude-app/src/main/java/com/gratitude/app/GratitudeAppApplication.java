package com.gratitude.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.gratitude.app.mapper")
@ComponentScan(basePackages = {"com.gratitude"})
@EnableAsync
@EnableScheduling
public class GratitudeAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(GratitudeAppApplication.class, args);
    }
}
