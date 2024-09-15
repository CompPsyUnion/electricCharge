package cn.yiming1234.electriccharge.util;

import cn.yiming1234.electriccharge.properties.message.BaseMessage;
import cn.yiming1234.electriccharge.properties.message.TextMessage;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class XmlHandleUtil {

    public static <T extends BaseMessage> String XmlHandleUtil(T object) {
        Document dou = null;
        try {

            dou = DocumentHelper.createDocument();
            Element root = dou.addElement("xml");
            root.addElement("ToUserName").addText("<![CDATA[" + object.getToUserName() + "]]>");
            root.addElement("FromUserName").addText("<![CDATA[" + object.getFromUserName() + "]]>");
            root.addElement("CreateTime").addText(String.valueOf(object.getCreateTime()));
            root.addElement("MsgType").addText("<![CDATA[" + object.getMsgType() + "]]>");
            if (object instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) object;
                root.addElement("Content").addText("<![CDATA[" + textMessage.getContent() + "]]>");
            }
        } catch (Exception e) {
            log.error("出现错误！XmlHandleFun");
            e.printStackTrace();
        }
        int count = "encoding=\"UTF-8\"?".length();
        String result = dou.asXML();
        result = result.substring(result.indexOf("encoding=\"UTF-8\"?") + count + 1);
        return result.trim();

    }
}