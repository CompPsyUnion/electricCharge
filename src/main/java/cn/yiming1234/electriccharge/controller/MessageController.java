package cn.yiming1234.electriccharge.controller;

import cn.yiming1234.electriccharge.service.ElectricService;
import cn.yiming1234.electriccharge.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Controller
@Slf4j
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private ElectricService electricService;

    /**
     * 获取用户手机号
     */
    @PostMapping("/submitPhone")
    public ResponseEntity<String> submitPhone(@RequestParam String phone) {
        try {
            // 调用服务层处理逻辑，返回适当的响应
            String result = messageService.submitPhone(phone);
            return ResponseEntity.ok(result);

        } catch (ExecutionException | InterruptedException e) {
            log.error("发送验证码失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("发送验证码失败");
        }
    }

    /**
     * 获取表单信息
     */
    @PostMapping("/submitForm")
    public ResponseEntity<Map<String, Object>> submitForm(@RequestParam String phone,
                                                          @RequestParam String verificationCode,
                                                          @RequestParam String roomNumber) {
        String result = messageService.submitForm(phone, verificationCode, roomNumber);
        Map<String, Object> response = new HashMap<>();
        if ("验证码错误".equals(result)) {
            response.put("success", false);
            response.put("message", "验证码错误");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else {
            response.put("success", true);
            response.put("url", result);
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 开启定时任务
     */
    @Scheduled(fixedRate = 3600000) // 每隔1小时执行一次
    public void sendSms() {
        List<String> phones = messageService.getAllPhones();
        for (String phone : phones) {
            LocalDateTime creationDate = messageService.getPhoneCreationDate(phone);
            if (creationDate != null) {
                long diffInMillies = Math.abs(new Date().getTime() - creationDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                if (diffInDays <= 30) {
                    String content = String.valueOf(messageService.getRoom(phone));
                    double balance = 0;
                    try {
                        balance = Double.parseDouble(electricService.getCharge(content));
                        electricService.saveChargeByPhone(phone,String.valueOf(balance));
                        log.info("balance:{}", balance);
                    } catch (IllegalArgumentException e) {
                        log.error("房间号错误：{}", content);
                        continue;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    if (balance < 10) {
                        log.info("余额不足需要续费");
                        messageService.SendSms2(phone, String.valueOf(balance));
                    }else{
                        log.info("余额充足");
                    }
                } else {
                    log.info("订阅已过期");
                }
            }
        }
    }
}
