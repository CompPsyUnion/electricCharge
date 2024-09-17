package cn.yiming1234.electriccharge.service;

import cn.hutool.http.HttpUtil;
import cn.yiming1234.electriccharge.controller.ElectricController;
import cn.yiming1234.electriccharge.pojo.Balance;
import cn.yiming1234.electriccharge.properties.ElectricProperties;
import cn.yiming1234.electriccharge.properties.H5LoginProperties;
import cn.yiming1234.electriccharge.properties.MessageProperties;
import cn.yiming1234.electriccharge.properties.message.BaseMessage;
import cn.yiming1234.electriccharge.properties.message.TextMessage;
import cn.yiming1234.electriccharge.util.MessageUtil;
import cn.yiming1234.electriccharge.util.MsgHelpClass;
import cn.yiming1234.electriccharge.util.TypeUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import cn.yiming1234.electriccharge.properties.WeChatProperties;

@Service
@Slf4j
public class WeixinService {

    private final ElectricProperties electricProperties;
    private final WeChatProperties weChatProperties;
    private final H5LoginProperties h5LoginProperties;
    private final ObjectMapper jacksonObjectMapper;
    private final ElectricService electricService;
    private final MoneyService moneyService;

    public WeixinService(WeChatProperties weChatProperties, H5LoginProperties h5LoginProperties, ObjectMapper jacksonObjectMapper, ElectricProperties electricProperties, MoneyService moneyService, ElectricService electricService) {
        this.weChatProperties = weChatProperties;
        this.h5LoginProperties = h5LoginProperties;
        this.jacksonObjectMapper = jacksonObjectMapper;
        this.electricProperties = electricProperties;
        this.electricService = electricService;
        this.moneyService = moneyService;
    }

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
            log.info("access_token:{}", jsonNode.get("access_token").asText());
            return jsonNode.get("access_token").asText();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

//    /**
//     * 添加自定义菜单
//     */
//    public String createMenu(String accessToken) {
//        try (HttpClient client = HttpClient.newHttpClient()) {
//            String jsonPayload = """
//        {
//            "button":[
//            {
//                "name":"我的博客",
//                "sub_button":[
//                {
//                    "type":"view",
//                    "name":"网页版博客",
//                    "url":"http://blog.yiming1234.cn"
//                },
//                {
//                    "type":"view",
//                    "name":"CSDN博客",
//                    "url":"https://yiming1234.blog.csdn.net"
//                },
//                {
//                    "type":"view",
//                    "name":"Github主页",
//                    "url":"https://github.com/Pleasurecruise"
//                }]
//            },
//            {
//                "name":"公众号功能",
//                "sub_button":[
//                {
//                    "type":"view",
//                    "name":"查询寝室电费",
//                    "url":"http://www.soso.com/"
//                },
//                {
//                    "type":"article_id",
//                    "name":"编程资源汇总",
//                    "article_id":"mid=2247483659"
//                }]
//            }]
//        }
//        """;
//
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("https://api.weixin.qq.com/cgi-bin/menu/create?access_token=" + accessToken))
//                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
//                    .setHeader("content-type", "application/json")
//                    .build();
//
//            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//            return response.body();
//        } catch (IOException | InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//    }

    /**
     * 设置余额
     */
    public double setBalance(){
        try {
            Balance balance = jacksonObjectMapper.readValue(electricService.getElectricCharge(), Balance.class);
            double amount = balance.getData().getAmount();
            log.info("balance:{}", amount);
            return amount;
        } catch (Exception e) {
            log.error("获取电费余额失败", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 发送余额模板消息
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

    /**
     * 处理来自微信公众号的消息
     */
    public BaseMessage processMessage(Map<String, String> map) throws IllegalAccessException, InstantiationException {
        String fromUserName = map.get("ToUserName");
        String toUserName = map.get("FromUserName");
        String msgType = map.get("MsgType");
        long createTime = Long.valueOf(map.get("CreateTime"));
        long msgId = Long.valueOf(map.get("MsgId"));
        BaseMessage baseMessage = null;
        if (msgType.equals(TypeUtil.REQ_MESSAGE_TYPE_TEXT)) {
            log.info("这是文本消息！");
            baseMessage = MsgHelpClass.setAttribute(new MessageProperties(fromUserName, toUserName, createTime, msgType, msgId), TextMessage.class);
            TextMessage textMessage = (TextMessage) baseMessage;
            textMessage.setContent(map.get("Content"));
        } else {
            log.info("暂不支持此消息类型！");
        }
        return baseMessage;
    }

    /**
     * 获取消息内容
     */
    public String getContent(Map<String, String> map){
        try{
            log.info("消息内容：" + map.get("Content"));
            return map.get("Content");
        } catch (Exception e) {
            log.error("获取消息内容失败", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取电费余额(自用)
     */
    public double getBalance() {
        try {
            Balance balance = jacksonObjectMapper.readValue(electricService.getElectricCharge(), Balance.class);
            return balance.getData().getAmount();
        } catch (Exception e) {
            log.error("获取电费余额失败", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取电费余额
     */
    public double getCharge(String content) {
        try {
            String response = electricService.getCharge(content);
            log.info("Server response: {}", response);
            Balance balance = jacksonObjectMapper.readValue(electricService.getCharge(content), Balance.class);
            return balance.getData().getAmount();
        } catch (Exception e) {
            log.error("获取电费余额失败", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 生成回复微信公众号的消息
     */
    public void autoReply(BaseMessage baseMessage, HttpServletResponse response) {
        log.info("-----------------开始回复消息-----------------");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/xml; charset=UTF-8");
        if (baseMessage instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) baseMessage;
            try {
                String content = getContent(MessageUtil.xmlToMap(MessageUtil.textMessageToXml(textMessage)));
                double balance = getCharge(content);

                String codeParams = electricService.getCode(content);
                String[] params = codeParams.split("&");

                String buildingCode = params[0].split("=")[1];
                String floorCode = params[1].split("=")[1];
                String roomCode = params[2].split("=")[1];

                String recharge10 = moneyService.getPaymentLink(buildingCode, floorCode, roomCode, "10");
                String recharge20 = moneyService.getPaymentLink(buildingCode, floorCode, roomCode, "20");
                String recharge30 = moneyService.getPaymentLink(buildingCode, floorCode, roomCode, "30");

                String url = h5LoginProperties.getHost();

                textMessage.setContent(String.format(
                        "当前寝室电费余额：%.2f元，\n充值10元：%s\n充值20元：%s\n充值30元：%s\n点击此链接开启短信提醒服务：%s",
                        balance, recharge10, recharge20, recharge30, url
                ));
                textMessage.setCreateTime(System.currentTimeMillis());
                textMessage.setFromUserName(baseMessage.getFromUserName());
                textMessage.setToUserName(baseMessage.getToUserName());
                String xml = MessageUtil.textMessageToXml(textMessage);
                log.info("回复消息内容：" + xml);
                response.getWriter().write(xml);
                response.getWriter().flush(); // 确保消息被发送
                log.info("消息已发送到微信服务器");
            } catch (Exception e) {
                log.error("处理消息失败", e);
            }
        } else {
            log.info("暂不支持此消息类型！");
            try {
                response.getWriter().write("success");
                response.getWriter().flush(); // 确保消息被发送
            } catch (Exception e) {
                log.error("发送消息失败", e);
            }
        }
    }
}