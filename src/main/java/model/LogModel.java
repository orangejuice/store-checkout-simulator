package model;

import java.util.Date;

public class LogModel {
    private String desc;
    private Date time;

    public LogModel(String desc, Date time) {
        this.desc = desc;
        this.time = time;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
