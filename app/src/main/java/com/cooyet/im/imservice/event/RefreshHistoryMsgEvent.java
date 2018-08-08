package com.cooyet.im.imservice.event;

import com.cooyet.im.DB.entity.MessageEntity;

import java.util.List;

/**
 * @author : ronghua.xie on 15-3-26.
 * @email : ronghua.xie@cooyet.com.
 *
 * 异步刷新历史消息
 */
public class RefreshHistoryMsgEvent {
   public int pullTimes;
   public int lastMsgId;
   public int count;
   public List<MessageEntity> listMsg;
   public int peerId;
   public int peerType;
   public String sessionKey;

   public RefreshHistoryMsgEvent(){}

}
