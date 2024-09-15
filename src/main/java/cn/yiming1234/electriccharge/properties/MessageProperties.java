package cn.yiming1234.electriccharge.properties;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
/**
 * 消息类型处理工具类
 */
public class MessageProperties {

    private String fromUserName;
    private String toUserName;
    private long createTime;
    private String msgType;
    private long msgId;

    public MessageProperties(String fromUserName, String toUserName, long createTime, String msgType, long msgId) {
        this.fromUserName = fromUserName;
        this.toUserName = toUserName;
        this.createTime = createTime;
        this.msgType = msgType;
        this.msgId = msgId;
    }
}