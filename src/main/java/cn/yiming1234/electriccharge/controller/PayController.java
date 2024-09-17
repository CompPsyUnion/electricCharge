package cn.yiming1234.electriccharge.controller;

import cn.yiming1234.electriccharge.entity.User;
import cn.yiming1234.electriccharge.mapper.UserMapper;
import cn.yiming1234.electriccharge.service.ElectricService;
import cn.yiming1234.electriccharge.service.MessageService;
import cn.yiming1234.electriccharge.service.PayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@Slf4j
@Controller
public class PayController {

    @Autowired
    private PayService payService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ElectricService electricService;

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/pay")
    @ResponseBody
    public String pay(String phone, String roomNumber) {
        String response = payService.pay(phone,roomNumber);
        JSONObject jsonObject = new JSONObject(response);
        log.info("url:{}", jsonObject.getString("url"));
        return jsonObject.getString("url");
    }

    /**
     * 支付回调
     */
    @RequestMapping("/paycallback")
    @ResponseBody
    public String callback(HttpServletRequest request) throws Exception {

        Map<String, String[]> parameterMap = request.getParameterMap();
        log.info("展示回调的所有结果：");

        parameterMap.keySet().stream().forEach((k) ->{
            log.info(k+":"+parameterMap.get(k)[0]);
        });

        log.info("展示回调的所有结果完成");
        log.info("\n最终结果是:");

        String phone = request.getParameter("phone");
        String roomNumber = request.getParameter("roomNumber");
        String response = electricService.getCharge(roomNumber);

        JSONObject jsonResponse = new JSONObject(response);
        double balance = jsonResponse.getJSONObject("data").getDouble("surplus");

        if("OD".equals(parameterMap.get("status")[0])){
            log.info("用户支付成功了");

            User newUser = new User();
            newUser.setPhone(phone);
            newUser.setRoom(roomNumber);
            newUser.setCreateTime(LocalDateTime.now());
            userMapper.insert(newUser);

            electricService.saveChargeByPhone(phone, String.valueOf(balance));
            messageService.SendSms2(phone, String.valueOf(balance));
        }else {
            log.info("用户支付不成功");
        }
        return "ok";
    }
}
