package com.cooyet.im.imservice.event;

import com.cooyet.im.DB.entity.MessageEntity;

import java.util.ArrayList;

/**
 * @author : ronghua.xie on 14-12-30.
 * @email : ronghua.xie@cooyet.com.
 */
public class MessageTalkEvent {

    private ArrayList<MessageEntity> msgList;
    private Event event;
    private byte[] content;

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public MessageTalkEvent() {
    }

    public MessageTalkEvent(Event event) {
        //默认值 初始化使用
        this.event = event;
    }

    public MessageTalkEvent(Event event, MessageEntity entity) {
        //默认值 初始化使用
        this.event = event;
        msgList = new ArrayList<>(1);
        msgList.add(entity);
    }

    public MessageTalkEvent(Event event, byte[] entity) {
        //默认值 初始化使用
        this.event = event;
        this.content = entity;
    }

    public enum Event {
        NONE,

        MSG_TALK_SEND_VOICE_BYTES,

        MSG_TALK_CALL_REQ,

        MSG_TALK_CALL_RES_FAILED,
        MSG_TALK_CALL_RES_SERVICE_UNAVAILABLE,
        MSG_TALK_CALL_RES_SUCCESS,

        MSG_TALK_ALLOC_REQ,
        MSG_TALK_ALLOC_RES_FAILED,
        MSG_TALK_ALLOC_RES_SERVICE_UNAVAILABLE,
        MSG_TALK_ALLOC_RES_SUCCESS,

        MSG_TALK_VOICE_REQ,
        MSG_TALK_VOICE_RES_FAILED,
        MSG_TALK_VOICE_RES_SUCCESS,
        MSG_TALK_VOICE_RES_SERVICE_UNAVAILABLE,

        MSG_TALK_RELEASE_REQ,
        MSG_TALK_RELEASE_RES_FAILED,
        MSG_TALK_RELEASE_RES_SUCCESS,
        MSG_TALK_RELEASE_RES_SERVICE_UNAVAILABLE,

        MSG_TALK_HUNG_UP_RES_FAILED,
        MSG_TALK_HUNG_UP_RES_SUCCESS,
        MSG_TALK_HUNG_UP_RES_SERVICE_UNAVAILABLE,
        MSG_TALK_HUNG_UP_REQ,

    }

    public MessageEntity getMessageEntity() {
        if (msgList == null || msgList.size() <= 0) {
            return null;
        }
        return msgList.get(0);
    }

    public void setMessageEntity(MessageEntity messageEntity) {
        if (msgList == null) {
            msgList = new ArrayList<>();
        }
        msgList.clear();
        msgList.add(messageEntity);
    }

    public ArrayList<MessageEntity> getMsgList() {
        return msgList;
    }

    public void setMsgList(ArrayList<MessageEntity> msgList) {
        this.msgList = msgList;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
