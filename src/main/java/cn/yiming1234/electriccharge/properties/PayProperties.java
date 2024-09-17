package cn.yiming1234.electriccharge.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "yiming1234.pay")
@Data
public class PayProperties {

    private String appid;
    private String appsecret;

}
