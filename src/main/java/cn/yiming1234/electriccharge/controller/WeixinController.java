package cn.yiming1234.electriccharge.controller;

import cn.yiming1234.electriccharge.service.H5LoginService;
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

    @Autowired
    private H5LoginService h5LoginService;

    /**
     * 发送电费不足通知
     */
    @PostMapping("/sendBalance")
    public String sendBalance() {
        String code = h5LoginService.getCode();
        String accessToken = weixinService.getAccessToken();
        String openId = h5LoginService.getOpenId(code);
        double balance = weixinService.setBalance();
        weixinService.sendBalance(accessToken, openId, balance);
        return "success";
    }

    /**
     * 每隔一个小时根据数据表中的用户查询一次电费
     * 当电费小于10时发送通知
     */
    @PostMapping("/sendBalanceByUser")
    public String sendBalanceByUser() {
        // TODO
        return "success";
    }
}
