package cn.yiming1234.electriccharge.service;

import cn.yiming1234.electriccharge.controller.ElectricController;
import cn.yiming1234.electriccharge.pojo.Balance;
import cn.yiming1234.electriccharge.properties.ElectricProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import cn.yiming1234.electriccharge.properties.WeChatProperties;

@Service
@Slf4j
public class WeixinService {

    private final ElectricProperties electricProperties;
    private final WeChatProperties weChatProperties;
    private final ElectricController electricController;
    private final ObjectMapper jacksonObjectMapper;

    public WeixinService(WeChatProperties weChatProperties, ElectricController electricController, ObjectMapper jacksonObjectMapper, ElectricProperties electricProperties) {
        this.weChatProperties = weChatProperties;
        this.electricController = electricController;
        this.jacksonObjectMapper = jacksonObjectMapper;
        this.electricProperties = electricProperties;
    }

    @Autowired
    private H5LoginService h5LoginService;

    /**
     * 获取微信公众号的access_token
     */
    public String getAccessToken() {
        try (HttpClient client = HttpClient.newHttpClient()) {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", weChatProperties.getAppid(), weChatProperties.getAppsecret())))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            // 解析JSON响应，提取access_token字段
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            log.info("access_token: " + jsonNode.get("access_token").asText());
            return jsonNode.get("access_token").asText();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试公众号显示的用户openId
     */
    public String getOpenId() {
        return "oxk7M6DHey3QKzhiWYcdqYzcqLqA";
    }

    /**
     * 设置余额
     */
    public double setBalance(){
        try {
            Balance balance = jacksonObjectMapper.readValue(electricController.getElectricCharge(), Balance.class);
            double amount = balance.getData().getAmount();
            log.info("balance:{}", amount);
            return amount;
        } catch (Exception e) {
            log.error("获取电费余额失败", e);
            throw new RuntimeException(e);
        }
    }
    /**
     * 发送余额通知
     */
    public String sendBalance(String accessToken, String openId, Double balance) {
        try (HttpClient client = HttpClient.newHttpClient()) {

            String roomCode = electricProperties.getRoomCode();
            String jsonPayload = new StringBuilder()
                    .append("{")
                    .append("\"touser\":\"").append(openId).append("\",")
                    .append("\"template_id\":\"").append("8M1mgGgJt5uX2I7BLhbJC4tDKmqtDvWMj-DT2_PMUDo").append("\",")
                    .append("\"data\":{")
                    .append("\"thing18\":{")
                    .append("\"value\":\"").append(roomCode).append("\"")
                    .append("},")
                    .append("\"character_string6\":{")
                    .append("\"value\":\"").append(balance).append("\"")
                    .append("}")
                    .append("}")
                    .append("}")
                    .toString();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + accessToken))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .setHeader("content-type", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
