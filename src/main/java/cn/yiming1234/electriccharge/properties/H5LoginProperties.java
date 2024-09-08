package cn.yiming1234.electriccharge.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "yiming1234.server")
@Data
public class H5LoginProperties {
    private String host;
}
