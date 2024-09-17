package cn.yiming1234.electriccharge.controller;

import cn.yiming1234.electriccharge.properties.H5LoginProperties;
import cn.yiming1234.electriccharge.properties.WeChatProperties;
import cn.yiming1234.electriccharge.service.H5LoginService;
import cn.yiming1234.electriccharge.service.WeixinService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@Slf4j
public class H5LoginController {

    @Autowired
    private H5LoginService h5LoginService;

    @Autowired
    private WeixinService weixinService;

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private H5LoginProperties h5LoginProperties;

    /**
     * 重定向到微信授权页面
     */
    @GetMapping("/redirect")
    public void redirectToWeChat(HttpServletResponse response) throws IOException {
        String redirectUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + weChatProperties.getAppid()
                + "&redirect_uri=" + URLEncoder.encode(h5LoginProperties.getHost(), StandardCharsets.UTF_8.toString())
                + "&response_type=code&scope=snsapi_userinfo#wechat_redirect";
        response.sendRedirect(redirectUrl);
        log.info("redirectUrl: " + redirectUrl);
    }

    /**
     * 微信授权回调,同时发送模板消息
     */
    @GetMapping("/link")
    public void callback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
        String accessToken = weixinService.getAccessToken();
        String openId = h5LoginService.getOpenId(code);
        Double balance = weixinService.setBalance();
        weixinService.sendBalance(accessToken, openId, balance);
        h5LoginService.saveOpenId(openId);
        log.info("code: " + code);
        log.info("openid: " + openId);
        response.sendRedirect("https://blog.yiming1234.cn");
    }
}