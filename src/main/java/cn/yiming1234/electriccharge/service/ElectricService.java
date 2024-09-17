package cn.yiming1234.electriccharge.service;

import cn.yiming1234.electriccharge.mapper.UserMapper;
import cn.yiming1234.electriccharge.properties.ElectricProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ElectricService {

    @Autowired
    private ElectricProperties electricProperties;

    @Autowired
    private UserMapper userMapper;

    /**
     * 通过手机号储存电费
     */
    public String saveChargeByPhone(String phone, String balance) {
        log.info("phone:{}, balance:{}", phone, balance);
        userMapper.updateChargeByPhone(phone, balance);
        return "success";
    }

    /**
     * 通过房间号储存电费
     */
    public String saveChargeByRoom(String room, String balance) {
        log.info("room:{}, balance:{}", room, balance);
        userMapper.updateChargeByRoom(room, balance);
        return "success";
    }

    /**
     * 第三方接口获取电费余额(自用)
     */
    public String getElectricCharge() throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {

            String requestBody = String.format(
                    "areaId=2307499265384382465&buildingCode=%s&floorCode=%s&roomCode=%s&ymId=2309636064844165132&platform=WECHAT_H5",
                    electricProperties.getBuildingCode(),
                    electricProperties.getFloorCode(),
                    electricProperties.getRoomCode()
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://application.xiaofubao.com/app/electric/queryRoomSurplus"))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 NetType/WIFI MicroMessenger/7.0.20.1781(0x6700143B) WindowsWechat(0x63090b19) XWEB/11253 Flue")
                    .setHeader("x-requested-with", "XMLHttpRequest")
                    .setHeader("content-type", "application/x-www-form-urlencoded;charset=UTF-8")
                    .setHeader("origin", "https://application.xiaofubao.com")
                    .setHeader("sec-fetch-site", "same-origin")
                    .setHeader("sec-fetch-mode", "cors")
                    .setHeader("sec-fetch-dest", "empty")
                    .setHeader("referer", "https://application.xiaofubao.com/")
                    .setHeader("accept-language", "zh-CN,zh;q=0.9")
                    .setHeader("Cookie", electricProperties.getCookie())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        }
    }

    /**
     * 根据消息内容输出对应的楼号，层号，房号
     */
    public String getCode(String content) {

        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("房间号错误: " + content);
        }

        // 楼栋号映射表
        Map<String, String> buildingCodeMap = new HashMap<>();
        buildingCodeMap.put("19", "009");
        buildingCodeMap.put("15", "007");
        buildingCodeMap.put("23", "013");

        // 将输入的content用"-"进行分割
        String[] split = content.split("-");
        if (split.length < 2) {
            throw new IllegalArgumentException("Invalid content format. Expected format: <building>-<floor><room>");
        }
        // 获取楼栋号并映射为对应的代码
        String buildingCode = buildingCodeMap.get(split[0]);
        if (buildingCode == null) {
            throw new IllegalArgumentException("Invalid building number: " + split[0]);
        }

        // 获取房间号
        int roomNumber = Integer.parseInt(split[1]);
        int floorNumber = roomNumber / 100; // 提取楼层号

        String floorCode = "";
        switch (split[0]) {
            case "15":
                floorCode = String.format("%s%03d", buildingCode, floorNumber);
                break;
            case "19":
                floorCode = String.format("%s%03d", buildingCode, floorNumber - 2);
                break;
            case "23":
                floorCode = String.format("%s%03d", buildingCode, floorNumber - 1);
                break;
            default:
                throw new IllegalArgumentException("Unsupported building number: " + split[0]);
        }

        String roomCode = content;
        log.info("buildingCode:{}, floorCode:{}, roomCode:{}", buildingCode, floorCode, roomCode);
        return String.format("buildingCode=%s&floorCode=%s&roomCode=%s", buildingCode, floorCode, roomCode);
    }


    /**
     * 第三方接口获取电费余额
     */
    public String getCharge(String content) throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {

            String codeParams = getCode(content);

            String requestBody = String.format(
                    "areaId=%s&platform=WECHAT_H5&%s",
                    electricProperties.getAreaId(),
                    codeParams
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://application.xiaofubao.com/app/electric/queryRoomSurplus"))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 NetType/WIFI MicroMessenger/7.0.20.1781(0x6700143B) WindowsWechat(0x63090b19) XWEB/11253 Flue")
                    .setHeader("x-requested-with", "XMLHttpRequest")
                    .setHeader("content-type", "application/x-www-form-urlencoded;charset=UTF-8")
                    .setHeader("origin", "https://application.xiaofubao.com")
                    .setHeader("sec-fetch-site", "same-origin")
                    .setHeader("sec-fetch-mode", "cors")
                    .setHeader("sec-fetch-dest", "empty")
                    .setHeader("referer", "https://application.xiaofubao.com/")
                    .setHeader("accept-language", "zh-CN,zh;q=0.9")
                    .setHeader("Cookie", electricProperties.getCookie())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        }
    }
}