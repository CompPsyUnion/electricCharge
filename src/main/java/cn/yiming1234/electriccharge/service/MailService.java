package cn.yiming1234.electriccharge.service;

import cn.yiming1234.electriccharge.properties.MailProperties;
import cn.yiming1234.electriccharge.util.MailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Slf4j
public class MailService {

    @Autowired
    private MailUtil mailUtil;

    @Autowired
    private MailProperties mailProperties;

    /**
     * 邮件地址正则表达式
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 验证邮件地址
     */
    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).find();
    }

    /**
     * 发送邮件
     */
    public void sendMail(String user, double balance) {
        user = user.replaceAll("[\\[\\]]", ""); // 去掉方括号
        if (!isValidEmail(user)) {
            log.error("无效的邮件地址: {}", user);
            throw new IllegalArgumentException("无效的邮件地址: " + user);
        }
        mailUtil.sendMail(user, mailProperties.getSubject(), balance);
        log.info("电费余额不足，当前余额：{}", balance);
    }

    /**
     * 获取用户
     */
    public List<String> getUsers() {
        // TODO 从数据库中获取用户
        return Arrays.asList("scyyw24@nottingham.edu.cn");
    }
}