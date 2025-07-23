package common;

import com.google.gson.Gson;

/**
 * 共用通訊協定，目前主要用在需要發送行為讓server辨識處理流程
 */
public class Message {
    private static final Gson gson = new Gson();

    private String type; // 命令類型
    private String target; // 目標學員代號（可選）
    private String content; // 傳送內容

    public Message() {
    }

    public Message(String type, String target, String content) {
        this.type = type;
        this.target = target;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String toJson() {
        return gson.toJson(this);
    }

    public static Message fromJson(String json) {
        return gson.fromJson(json, Message.class);
    }

    @Override
    public String toString() {
        return String.format("Message{type=%s, target=%s, content=%s}", type, target, content);
    }
}