package cn.yiming1234.electriccharge.service;

import cn.yiming1234.electriccharge.controller.PayController;
import cn.yiming1234.electriccharge.entity.User;
import cn.yiming1234.electriccharge.mapper.UserMapper;
import cn.yiming1234.electriccharge.properties.SmsProperties;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dysmsapi20170525.models.*;
import com.aliyun.sdk.service.dysmsapi20170525.*;
import com.google.gson.Gson;
import darabonba.core.client.ClientOverrideConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MessageService {

    @Autowired
    private SmsProperties smsProperties;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private PayService payService;

    /**
     * 获取所有订阅的手机号
     */
    public List<String> getAllPhones() {
        return userMapper.getAllPhones();
    }

    /**
     * 获取手机号的创建日期
     */
    public LocalDateTime getPhoneCreationDate(String phone) {
        String createTime = userMapper.getCreateTime(phone);
        return LocalDateTime.parse(createTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 获取所在房间号
     */
    public User getRoom(String phone) {
         return userMapper.getRoomByPhone(phone);
    }

    /**
     * 提交手机号
     */
    public String submitPhone(String phone) throws ExecutionException, InterruptedException {
        // 校验手机号是否存在于数据库
        User user = userMapper.getByPhone(phone);
        String code = generateVerificationCode(phone);

        if (user == null) {
            // 用户不存在，发送验证码
            log.info("用户不存在，发送验证码");
            SendSms1(phone, code);
            return "验证码已发送到手机！";
        }

        // 检查订阅是否过期
        LocalDate createTime = LocalDate.parse(userMapper.getCreateTime(phone), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (createTime.plusMonths(1).isBefore(LocalDate.now())) {
            // 订阅已过期，发送验证码
            log.info("订阅已过期，发送验证码");
            SendSms1(phone, code);
            return "订阅已过期，请续费，验证码已发送到手机！";
        }

        // 查询是否绑定寝室号
        if (userMapper.getRoomByPhone(phone) == null) {
            // 未绑定寝室，发送验证码
            log.info("未绑定寝室，发送验证码");
            SendSms1(phone, code);
            return "您尚未绑定寝室号，请尽快绑定，验证码已发送到手机！";
        }

        // 已绑定寝室且订阅未过期
        log.info("订阅未过期且已绑定寝室号，无需发送验证码");
        return "订阅未过期且已绑定寝室号，无需发送验证码";
    }

    /**
     * 生成验证码
     */
    public String generateVerificationCode(String phone) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }
        String code = sb.toString();
        redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES); // 存储验证码并设置过期时间为5分钟
        return code;
    }

    /**
     * 校验验证码
     */
    public boolean checkVerificationCode(String phone, String verificationCode) {
        //校验验证码是否正确
        String code = redisTemplate.opsForValue().get(phone);
        if (code == null || !code.equals(verificationCode)) {
            return false;
        }else{
            return true;
        }
    }

    /**
     * 提交表单
     */
    public String submitForm(String phone, String verificationCode, String roomNumber) {
        //校验验证码
        if (!checkVerificationCode(phone, verificationCode)) {
            return "验证码错误";
        }else{
            //校验是否在数据库中存在
            User user = userMapper.getByPhone(phone);
            if (user == null) {
                log.info("用户不存在");
            } else {
                // 更新数据库中创建日期
                user.setCreateTime(LocalDateTime.now());
                userMapper.updateCreateTime(user);
            }
            //返回支付链接
            String response = payService.pay(phone,roomNumber);
            return response;
        }
    }

    /**
     * 发送验证码
     */
    public String SendSms1(String phone, String code) {
        try {
            // Create a credential provider
            StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
                    .accessKeyId(smsProperties.getAccessKeyId())
                    .accessKeySecret(smsProperties.getAccessKeySecret())
                    .build());

            // Create a client
            AsyncClient client = AsyncClient.builder()
                    .region("cn-hangzhou") // Region ID
                    .credentialsProvider(provider)
                    .overrideConfiguration(
                            ClientOverrideConfiguration.create()
                                    .setEndpointOverride("dysmsapi.aliyuncs.com")
                    )
                    .build();

            // Create a request
            SendSmsRequest sendSmsRequest = SendSmsRequest.builder()
                    .phoneNumbers(phone)
                    .signName("一鸣的小站")
                    .templateCode("SMS_292395470")
                    .templateParam("{\"code\":\"" + code + "\"}")
                    .build();

            // Send the request
            CompletableFuture<SendSmsResponse> response = client.sendSms(sendSmsRequest);
            SendSmsResponse resp = response.get();
            System.out.println(new Gson().toJson(resp));

            client.close();
            return "验证码发送成功";
        } catch (ExecutionException | InterruptedException e) {
            log.error("发送验证码失败", e);
            return "发送验证码失败，请稍后重试";
        }
    }
    /**
     * 发送电费短信提醒
     */
    public String SendSms2(String phone, String balance) {
        try {
            // Create a credential provider
            StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
                    .accessKeyId(smsProperties.getAccessKeyId())
                    .accessKeySecret(smsProperties.getAccessKeySecret())
                    .build());

            // Create a client
            AsyncClient client = AsyncClient.builder()
                    .region("cn-hangzhou") // Region ID
                    .credentialsProvider(provider)
                    .overrideConfiguration(
                            ClientOverrideConfiguration.create()
                                    .setEndpointOverride("dysmsapi.aliyuncs.com")
                    )
                    .build();

            // Create a request
            SendSmsRequest sendSmsRequest = SendSmsRequest.builder()
                    .phoneNumbers(phone)
                    .signName("一鸣的小站")
                    .templateCode("SMS_473500188")
                    .templateParam("{\"money\":\"" + balance + "\"}")
                    .build();

            // Send the request
            CompletableFuture<SendSmsResponse> response = client.sendSms(sendSmsRequest);
            SendSmsResponse resp = response.get();
            System.out.println(new Gson().toJson(resp));

            client.close();
            return "电费短信提醒发送成功";
        } catch (ExecutionException | InterruptedException e) {
            log.error("发送电费短信提醒失败", e);
            return "发送电费短信提醒失败，请稍后重试";
        }
    }
}
