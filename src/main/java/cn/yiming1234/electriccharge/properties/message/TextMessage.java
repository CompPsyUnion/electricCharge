package cn.yiming1234.electriccharge.properties.message;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class TextMessage extends BaseMessage {

    @JacksonXmlProperty(localName = "Content")
    private String Content;

    //由于打印本类toString时只会打印本类有的属性，不会打印父类的，所以我们需要重写类的toString，加上本类属性和父类属性
    public String toString(){
        return super.toString() + "[TextMessage]：" + "  Content：" + this.Content;
    }
}
