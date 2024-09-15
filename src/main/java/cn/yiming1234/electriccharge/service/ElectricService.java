package cn.yiming1234.electriccharge.service;

import cn.yiming1234.electriccharge.properties.ElectricProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class ElectricService {

    @Autowired
    private ElectricProperties electricProperties;

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
}