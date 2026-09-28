package com.fruitmall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 基于推荐算法的水果商城 —— 后端启动类
 */
@SpringBootApplication
@MapperScan("com.fruitmall.**.mapper")
public class FruitMallApplication {

    public static void main(String[] args) {
        SpringApplication.run(FruitMallApplication.class, args);
    }
}
