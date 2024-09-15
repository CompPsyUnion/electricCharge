package cn.yiming1234.electriccharge.controller;

import cn.yiming1234.electriccharge.properties.message.BaseMessage;
import cn.yiming1234.electriccharge.service.H5LoginService;
import cn.yiming1234.electriccharge.service.WeixinService;
import cn.yiming1234.electriccharge.util.MessageUtil;
import cn.yiming1234.electriccharge.util.TypeUtil;
import com.alibaba.fastjson.JSON;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@Slf4j
public class WeixinController {

    @Autowired
    private WeixinService weixinService;

    @Autowired
    private H5LoginService h5LoginService;

    /**
     * 发送电费不足模板消息
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
        String code = h5LoginService.getCode();
        String accessToken = weixinService.getAccessToken();
        String openId = h5LoginService.getOpenId(code);
        double balance = weixinService.setBalance(); // 获取用户电费余额
        if (balance < 10) {
            log.info("用户电费余额不足，发送通知。");
            weixinService.sendBalance(accessToken, openId, balance); // 发送模板消息通知
        }
        return "success";
    }

    /**
     * 自动回复微信公众号消息
     */
    @PostMapping("/signature")
    public void message(HttpServletRequest request, HttpServletResponse response) {
        try {
            log.info("接收到微信公众号消息");
            Map<String, String> paramMap = MessageUtil.parseXml(request);
            log.info(JSON.toJSONString(paramMap));
            String type = paramMap.get("MsgType");
            if (TypeUtil.REQ_MESSAGE_TYPE_TEXT.equals(type)) {
                log.info("-----------------进入消息处理-----------------");
                BaseMessage baseMessage = weixinService.processMessage(paramMap);
                weixinService.autoReply(baseMessage, response);
            } else {
                log.info("不是文本消息");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
