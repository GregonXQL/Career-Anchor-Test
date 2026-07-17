package com.careeranchor.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.careeranchor.server.mapper")
@SpringBootApplication
public class AnchorApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnchorApplication.class, args);
    }
}
