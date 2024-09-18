package cn.yiming1234.electriccharge.service;

import cn.yiming1234.electriccharge.properties.H5LoginProperties;
import org.springframework.stereotype.Service;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpUtil;
import cn.yiming1234.electriccharge.properties.PayProperties;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Service
@Slf4j
public class PayService {

    @Autowired
    private PayProperties payProperties;

    @Autowired
    private H5LoginProperties h5LoginProperties;

    /**
     * 获取精确到秒的时间戳
     */
    public int getSecondTimestamp(Date date){
        if (null == date) {
            return 0;
        }
        String timestamp = String.valueOf(date.getTime());
        int length = timestamp.length();
        if (length > 3) {
            return Integer.valueOf(timestamp.substring(0,length-3));
        } else {
            return 0;
        }
    }

    /**
     * 发送支付请求
     *
     * @return
     */
    public String pay(String phone,String roomNumber) {
        Map<String,Object> options = new HashMap<>();
        options.put("version","1.1");
        options.put("appid",payProperties.getAppid());
        options.put("appsecret",payProperties.getAppsecret());
        options.put("trade_order_id", UUID.randomUUID().toString().replace("-", ""));
        options.put("total_fee","5.00");
        options.put("title","开通短信提醒服务");
        options.put("time", getSecondTimestamp(new Date()));
        options.put("notify_url", h5LoginProperties.getHost() + "/paycallback?phone=" + phone + "&roomNumber=" + roomNumber);
        options.put("nonce_str","987412365");
        StringBuilder sb = new StringBuilder();
        options.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(a ->{
            sb.append(a).append("&");});
        sb.deleteCharAt(sb.length()-1);
        sb.append(payProperties.getAppsecret());
        String s = SecureUtil.md5(sb.toString());
        options.put("hash", s);
        log.info("sign:{}",s);
        log.info("time:{}",options.get("time"));
        log.info("传递的参数有：{}", options);
        log.info("开始调用支付接口");
        String url = "https://api.xunhupay.com/payment/do.html"; //支付网关
        String post = HttpUtil.post(url, options);
        log.info("结束调用支付接口");
        log.info("接口响应的结果是：{}",post);
        try{
            Map map = (Map) JSON.parse(post);
            map.keySet().stream().forEach(k -> {
                if (k == "url") {
                    log.info("url二维码链接是: "+map.get(k));
                }
            });
        }catch (Exception e){
            e.printStackTrace();
            log.info("调用接口时出现了问题");
        }
        Map map = (Map) JSON.parse(post);
        return map.get("url").toString();
    }
}