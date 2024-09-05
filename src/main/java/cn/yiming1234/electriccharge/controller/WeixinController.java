package cn.yiming1234.electriccharge.controller;

import cn.yiming1234.electriccharge.service.WeixinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class WeixinController {

    @Autowired
    private WeixinService weixinService;

    @PostMapping("/sendBalance")
    public String sendBalance() {
        String openId = weixinService.getOpenId();
        String accessToken = weixinService.getAccessToken();
        double balance = weixinService.setBalance();

        return weixinService.sendBalance(accessToken, openId, balance);
    }

}
