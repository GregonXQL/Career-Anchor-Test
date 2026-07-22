package com.careeranchor.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan(
        value = "com.careeranchor.server.mapper",
        lazyInitialization = "${mybatis-plus.lazy-initialization:false}"
)
@SpringBootApplication
public class AnchorApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnchorApplication.class, args);
    }
}
