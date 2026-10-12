package com.taptales;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.taptales.mapper")
public class TapTalesApplication {
    public static void main(String[] args) {
        SpringApplication.run(TapTalesApplication.class, args);
    }
}
