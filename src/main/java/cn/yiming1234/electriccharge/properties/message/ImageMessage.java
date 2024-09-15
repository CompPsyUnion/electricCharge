package cn.yiming1234.electriccharge.properties.message;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class ImageMessage extends BaseMessage {

    @JacksonXmlProperty(localName = "PicUrl")
    private String PicUrl;

    @JacksonXmlProperty(localName = "MediaId")
    private String MediaId;

    public String toString(){
        return super.toString() + "[ImageMessage]：" + "  PriUrl：" + this.PicUrl + "  MediaId：" + this.MediaId;
    }
}
