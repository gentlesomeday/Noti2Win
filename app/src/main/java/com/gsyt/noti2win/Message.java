package com.gsyt.noti2win;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author: 付仁秀
 * @Description：
 **/
public class Message {
    private String title;
    private String content;
    private int type;
    private int uuid;
    private String time;

    public Message(int type,String title, String content) {
        this.title = title;
        this.content = content;
        this.type = type;
        this.uuid=0;
        this.time = getCurrentTime();
    }

    public Message() {
        this.title = "";
        this.content = "";
        this.type = -1;
        this.uuid=0;
        this.time = getCurrentTime();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getUuid() {
        return uuid;
    }

    public void setUuid(int uuid) {
        this.uuid = uuid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }
}
