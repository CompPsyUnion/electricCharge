package cn.yiming1234.electriccharge;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring Boot 启动类
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
@MapperScan("cn.yiming1234.electriccharge.mapper")
@Slf4j
public class ElectricChargeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElectricChargeApplication.class, args);
    }

}
