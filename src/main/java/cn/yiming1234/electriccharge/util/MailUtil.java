package cn.yiming1234.electriccharge.util;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import cn.yiming1234.electriccharge.properties.MailProperties;

import java.io.File;

@Service
@Slf4j
public class MailUtil {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MailProperties mailProperties;

    /**
     * 发送文本邮件
     */
    public void sendMail(String to, String subject, double balance) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("pleasurecruise@qq.com");
        message.setTo(to); // 使用传入的参数 `to`
        message.setSubject(subject); // 使用传入的参数 `subject`
        message.setText("您的电费余额不足，当前余额为：" + balance + "元。"); // 使用传入的参数 `balance`
        mailSender.send(message);
    }

    /**
     * 发送HTML邮件
     */
    public void sendHtmlMail(String to, String subject, String content) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);
            message.setFrom("pleasurecruise@qq.com");
            messageHelper.setTo(InternetAddress.parse(to)); // 使用传入的参数 `to`
            messageHelper.setSubject(subject); // 使用传入的参数 `subject`
            messageHelper.setText(content, true); // 第二个参数 `true` 表示发送 HTML 格式的邮件
            mailSender.send(message);
            log.info("发送HTML邮件成功");
        } catch (Exception e) {
            log.error("发送HTML邮件失败", e);
        }
    }

    /**
     * 发送带附件的邮件
     */
    public void sendAttachmentsMail(String to, String subject, String content, String filePath) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);
            message.setFrom("pleasurecruise@qq.com");
            messageHelper.setTo(to); // 使用传入的参数 `to`
            messageHelper.setSubject(subject); // 使用传入的参数 `subject`
            messageHelper.setText(content); // 使用传入的参数 `content`

            // 添加附件
            FileSystemResource file = new FileSystemResource(new File(filePath));
            String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1); // 提取文件名
            messageHelper.addAttachment(fileName, file);

            mailSender.send(message);
            log.info("发送带附件的邮件成功");
        } catch (Exception e) {
            log.error("发送带附件的邮件失败", e);
        }
    }
}
