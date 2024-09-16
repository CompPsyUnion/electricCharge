package cn.yiming1234.electriccharge.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "yiming1234.electric")
@Data
public class ElectricProperties {

    private String areaId;
    private String buildingCode;
    private String floorCode;
    private String roomCode;
    private String cookie;
}
