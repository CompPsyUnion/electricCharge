package cn.yiming1234.electriccharge.service;

import cn.yiming1234.electriccharge.properties.ElectricProperties;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@Slf4j
public class MoneyService {

    @Autowired
    private ElectricProperties electricProperties;

    /**
     * 获取 token
     */
    public String getToken() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String requestBody = "platform=WECHAT_H5";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://application.xiaofubao.com/center/common/token/get"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 NetType/WIFI MicroMessenger/7.0.20.1781(0x6700143B) WindowsWechat(0x63090c0f) XWEB/11275 Flue")
                .setHeader("x-requested-with", "XMLHttpRequest")
                .setHeader("content-type", "application/x-www-form-urlencoded;charset=UTF-8")
                .setHeader("origin", "https://application.xiaofubao.com")
                .setHeader("sec-fetch-site", "same-origin")
                .setHeader("sec-fetch-mode", "cors")
                .setHeader("sec-fetch-dest", "empty")
                .setHeader("referer", "https://application.xiaofubao.com/")
                .setHeader("accept-language", "zh-CN,zh;q=0.9")
                .setHeader("Cookie", electricProperties.getCookie())  // 可替换成动态获取的 Cookie
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("Response from token API: {}", response.body());
        return response.body();
    }

    /**
     * 获取付款链接
     */
    public String getPaymentLink(String buildingCode, String floorCode, String roomCode, String money) throws Exception {
        String submitToken = getToken();
        HttpClient client = HttpClient.newHttpClient();
        String requestBody = String.format(
                "areaId=2307499265384382465&buildingCode=%s&floorCode=%s&roomCode=%s&money=%s&submitToken=%s&platform=WECHAT_H5&extJson={\"serialNO\":\"\"}&ymId=2309636064844165132",
                buildingCode, floorCode, roomCode, money, submitToken
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://application.xiaofubao.com/app/electric/recharge"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 NetType/WIFI MicroMessenger/7.0.20.1781(0x6700143B) WindowsWechat(0x63090c0f) XWEB/11275 Flue")
                .setHeader("x-requested-with", "XMLHttpRequest")
                .setHeader("content-type", "application/x-www-form-urlencoded;charset=UTF-8")
                .setHeader("origin", "https://application.xiaofubao.com")
                .setHeader("sec-fetch-site", "same-origin")
                .setHeader("sec-fetch-mode", "cors")
                .setHeader("sec-fetch-dest", "empty")
                .setHeader("referer", "https://application.xiaofubao.com/")
                .setHeader("accept-language", "zh-CN,zh;q=0.9")
                .setHeader("Cookie", "shiroJID=5c7159f5-0621-41b1-84fe-d4faf83ad209")  // 动态 Cookie 可在此处替换
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("Response from payment API: {}", response.body());
        return new JSONObject(response.body()).getString("data");
    }
}
