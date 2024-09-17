package cn.yiming1234.electriccharge.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "yiming1234.aliyun")
@Data
public class SmsProperties {

    private String accessKeyId;
    private String accessKeySecret;

}
