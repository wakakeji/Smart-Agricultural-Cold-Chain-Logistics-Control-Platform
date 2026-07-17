package com.coldchain;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 智慧农业冷链物流管控平台启动类
 */
@SpringBootApplication
@MapperScan("com.coldchain.modules")
public class ColdChainApplication {

    public static void main(String[] args) {
        SpringApplication.run(ColdChainApplication.class, args);
    }
}
//测试代码合并1111
