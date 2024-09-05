package cn.yiming1234.electriccharge.service;

import cn.yiming1234.electriccharge.controller.ElectricController;
import cn.yiming1234.electriccharge.pojo.Balance;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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

    private final WeChatProperties weChatProperties;
    private final ElectricController electricController;
    private final ObjectMapper jacksonObjectMapper;

    public WeixinService(WeChatProperties weChatProperties, ElectricController electricController, ObjectMapper jacksonObjectMapper) {
        this.weChatProperties = weChatProperties;
        this.electricController = electricController;
        this.jacksonObjectMapper = jacksonObjectMapper;
    }

    /**
     * 获取微信公众号的access_token
     * @return
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
     * 获取微信公众号的openId
     * @return
     */
    // TODO
    public String getOpenId() {
//        try (HttpClient client = HttpClient.newHttpClient()) {
//
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("https://example.com"))
//                    .GET()
//                    .build();
//
//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//            return response.body();
//        } catch (IOException | InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        return "oxk7M6DHey3QKzhiWYcdqYzcqLqA";
    }

    /**
     * 设置余额
     *
     * @return
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
     * @param accessToken
     * @param openId
     * @param balance
     * @return
     */
    public String sendBalance(String accessToken, String openId, Double balance) {
        try (HttpClient client = HttpClient.newHttpClient()) {

            String jsonPayload = new StringBuilder()
                    .append("{")
                    .append("\"touser\":\"").append(openId).append("\",")
                    .append("\"template_id\":\"").append("NKVWY4dM6VxQdfB2DbnyZV3QXJwn0kHKPkp4I9sDv1g").append("\",")
                    .append("\"data\":{")
                    .append("\"balance\":{")
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
