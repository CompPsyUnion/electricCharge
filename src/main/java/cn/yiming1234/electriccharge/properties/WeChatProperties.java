package cn.yiming1234.electriccharge.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "yiming1234.wechat")
@Data
public class WeChatProperties {
    private String id;
    private String appid;
    private String appsecret;
}