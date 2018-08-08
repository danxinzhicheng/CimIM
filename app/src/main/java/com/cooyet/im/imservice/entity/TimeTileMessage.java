package com.cooyet.im.imservice.entity;

/**
 * @author : ronghua.xie on 15-1-8.
 * @email : ronghua.xie@cooyet.com.
 */
public class TimeTileMessage {
    private int time;
    public TimeTileMessage(int mTime){
        time= mTime;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
