package cn.yiming1234.electriccharge.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@RestController
@Slf4j
public class ConfigController {

    public static final String TOKEN = "token";

    /**
     * 微信公众号服务器重定向授权域名验证
     */
    @GetMapping("/MP_verify_bgXfuICvzc098DQ6.txt")
    public String showTxtFile() {
        try {
            ClassPathResource resource = new ClassPathResource("MP_verify_bgXfuICvzc098DQ6.txt");
            Path path = Paths.get(resource.getURI());
            log.info("path: " + path);
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read the file", e);
        }
    }

    /**
     * 微信公众号服务器基本服务器配置
     */
    @GetMapping("/signature")
    public String signature(HttpServletRequest req, HttpServletResponse resp) {
        // 1.获取微信传入的4个参数
        String signature = req.getParameter("signature");
        String timestamp = req.getParameter("timestamp");
        String nonce = req.getParameter("nonce");
        String echostr = req.getParameter("echostr");
        // 2.用timestamp, nonce, signature进行校验
        boolean result = check(timestamp, nonce, signature);
        if (result) {
            // 3.校验成功返回echostr
            return echostr;
        }
        return "error!";
    }

    public static boolean check(String timestamp, String nonce, String signature) {
        // 1.按字典序对TOKEN, timestamp和nonce排序
        String[] arr = new String[]{TOKEN,timestamp,nonce};
        Arrays.sort(arr);
        // 2.将3个参数拼成一个字符串进行sha1加密
        String str = arr[0]+arr[1]+arr[2];
        // 3.用commons-codec包中的工具类进行sha1加密
        str = DigestUtils.sha1Hex(str);
        // 4.将加密后的字符串和signature比较
        System.out.println(signature);
        return str.equalsIgnoreCase(signature);
    }

}
