package com.cooyet.im.imservice.event;

import com.cooyet.im.imservice.entity.UnreadEntity;

/**
 * @author : ronghua.xie on 15-1-6.
 * @email : ronghua.xie@cooyet.com.
 */
public class UnreadEvent {

    public UnreadEntity entity;
    public Event event;

    public UnreadEvent(){}
    public UnreadEvent(Event e){
        this.event = e;
    }

    public enum Event {
        UNREAD_MSG_LIST_OK,
        UNREAD_MSG_RECEIVED,

        SESSION_READED_UNREAD_MSG
    }
}
