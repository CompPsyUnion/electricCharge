package cn.yiming1234.electriccharge.controller;

import cn.yiming1234.electriccharge.service.MailService;
import cn.yiming1234.electriccharge.service.WeixinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@Slf4j
public class MailController {

    @Autowired
    private MailService mailService;

    @Autowired
    private WeixinService weixinService;

    /**
     * 发送电费不足邮件（自用）
     */
    @PostMapping("/sendMail")
    public String sendMail() {
        List<String> user = mailService.getUsers();
        double balance = weixinService.setBalance();
        mailService.sendMail(String.valueOf(user), balance);
        return "success";
    }

    /**
     * 每隔一个小时根据数据表中的用户查询一次电费
     * 当电费小于10时发送邮件通知
     */
    @Scheduled(fixedRate = 3600000) // 每隔1小时执行一次
    public String sendMailByUser() {
        for (String user : mailService.getUsers()) {
            double balance = weixinService.setBalance(); // 获取用户电费余额
            if (balance < 10) {
                log.info("用户电费余额不足，发送通知邮件。");
                mailService.sendMail(user, balance); // 发送邮件通知
            }
        }
        return "success";
    }

}
