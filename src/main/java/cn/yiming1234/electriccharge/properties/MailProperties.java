package cn.yiming1234.electriccharge.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;
import lombok.Data;

import java.util.Properties;

@Component
@ConfigurationProperties(prefix = "yiming1234.mail")
@Data
@Configuration
public class MailProperties {
    private String host;
    private String port;
    private String username;
    private String password;
    private String to;
    private String subject;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(this.host);
        mailSender.setPort(Integer.parseInt(this.port));
        mailSender.setUsername(this.username);
        mailSender.setPassword(this.password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.enable", "true"); // 启用 SSL
        props.put("mail.debug", "true");

        return mailSender;
    }
}