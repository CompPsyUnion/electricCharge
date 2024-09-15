package cn.yiming1234.electriccharge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot 启动类
 */
@SpringBootApplication
@EnableScheduling
public class ElectricChargeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElectricChargeApplication.class, args);
    }

}
