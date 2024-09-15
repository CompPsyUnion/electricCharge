package cn.yiming1234.electriccharge.util;

import java.util.Date;

import cn.yiming1234.electriccharge.properties.MessageProperties;
import cn.yiming1234.electriccharge.properties.message.BaseMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MsgHelpClass {

    /**
     * 设置消息属性
     */
    public static <E extends BaseMessage> E setAttribute(MessageProperties msgHandle, Class<E> eClass) throws IllegalAccessException, InstantiationException {
        E baseMessage = eClass.newInstance();
        baseMessage.setCreateTime(new Date().getTime());
        baseMessage.setFromUserName(msgHandle.getFromUserName());
        baseMessage.setMsgId(msgHandle.getMsgId());
        baseMessage.setToUserName(msgHandle.getToUserName());
        baseMessage.setMsgType(msgHandle.getMsgType());
        log.warn("MsgHelpClass-setAttribute方法返回值如下：\n" + baseMessage.toString());

        return baseMessage;
    }
}