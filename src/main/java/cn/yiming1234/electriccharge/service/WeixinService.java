package cn.yiming1234.electriccharge.service;

import cn.yiming1234.electriccharge.controller.ElectricController;
import cn.yiming1234.electriccharge.pojo.Balance;
import cn.yiming1234.electriccharge.properties.ElectricProperties;
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
import java.util.Map;

import cn.yiming1234.electriccharge.properties.WeChatProperties;

@Service
@Slf4j
public class WeixinService {

    private final ElectricProperties electricProperties;
    private final WeChatProperties weChatProperties;
    private final ObjectMapper jacksonObjectMapper;
    private final ElectricService electricService;

    public WeixinService(WeChatProperties weChatProperties, ElectricController electricController, ObjectMapper jacksonObjectMapper, ElectricProperties electricProperties, H5LoginService h5LoginService, ElectricService electricService) {
        this.weChatProperties = weChatProperties;
        this.jacksonObjectMapper = jacksonObjectMapper;
        this.electricProperties = electricProperties;
        this.electricService = electricService;
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

    // TODO 添加公众号自定义菜单

    /**
     * 生成回复微信公众号的消息
     */
    public void autoReply(BaseMessage baseMessage, HttpServletResponse response) {
        log.info("-----------------开始回复消息-----------------");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/xml; charset=UTF-8");
        if (baseMessage instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) baseMessage;
            String content = getContent(MessageUtil.xmlToMap(MessageUtil.textMessageToXml(textMessage)));
            textMessage.setContent("当前寝室电费余额：" + getCharge(content) + "元");
            textMessage.setCreateTime(System.currentTimeMillis());
            textMessage.setFromUserName(baseMessage.getFromUserName());
            textMessage.setToUserName(baseMessage.getToUserName());
            String xml = MessageUtil.textMessageToXml(textMessage);
            log.info("回复消息内容：" + xml);
            try {
                response.getWriter().write(xml);
                response.getWriter().flush(); // 确保消息被发送
                log.info("消息已发送到微信服务器");
            } catch (Exception e) {
                log.error("发送消息失败", e);
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