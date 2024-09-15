package cn.yiming1234.electriccharge.util;

import cn.yiming1234.electriccharge.properties.message.TextMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class MessageUtil {

    /**
     * 将xml字符串转换为文本对象
     * （解析微信发来的请求）
     */
    public static Map<String, String> parseXml(HttpServletRequest request) throws Exception {
        // 将解析结果存储在HashMap中
        Map<String, String> map = new HashMap<String, String>();
        // 从request中取得输入流
        InputStream inputStream = request.getInputStream();
        // 读取输入流
        SAXReader reader = new SAXReader();
        Document document = reader.read(inputStream);
        // 得到xml根元素
        Element root = document.getRootElement();
        // 得到根元素的所有子节点
        List<Element> elementList = root.elements();
        // 遍历所有子节点
        for (Element e : elementList) {
            map.put(e.getName(), e.getText());
            log.info("name：" + e.getName() + "   value："+map.get(e.getName()));
        }
        // 释放资源
        inputStream.close();
        return map;
    }

    /**
     * 将文本对象转换为xml字符串
     */

    public static String textMessageToXml(TextMessage textMessage) {
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("xml");

        root.addElement("ToUserName").addCDATA(textMessage.getToUserName());
        root.addElement("FromUserName").addCDATA(textMessage.getFromUserName());
        root.addElement("CreateTime").addText(String.valueOf(textMessage.getCreateTime()));
        root.addElement("MsgType").addCDATA(textMessage.getMsgType());
        root.addElement("Content").addCDATA(textMessage.getContent());

        return document.asXML();
    }
}
