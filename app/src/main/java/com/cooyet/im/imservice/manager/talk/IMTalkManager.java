package com.cooyet.im.imservice.manager.talk;


import android.util.Log;

import com.cooyet.im.DB.entity.MessageEntity;
import com.cooyet.im.imservice.entity.AudioMessage;
import com.cooyet.im.imservice.event.MessageTalkEvent;
import com.cooyet.im.imservice.manager.IMManager;
import com.cooyet.im.imservice.manager.IMSocketManager;
import com.cooyet.im.protobuf.IMBaseDefine;
import com.cooyet.im.protobuf.IMTalk;
import com.cooyet.im.utils.ByteUtils;
import com.google.protobuf.ByteString;

import java.io.IOException;

import de.greenrobot.event.EventBus;

/**
 * Created by user on 2018/6/13.
 */

public class IMTalkManager extends IMManager {

    private boolean isFromPc = false;
    private int seq = 0;
    private int msgId = 2018;

    /**
     * 单例模式
     */
    private static IMTalkManager inst = new IMTalkManager();

    public static IMTalkManager instance() {
        return inst;
    }

    public IMTalkManager() {
    }


    @Override
    public void doOnStart() {

    }

    @Override
    public void reset() {

    }

//    public static AudioMessage analyzeAudio(IMTalk.IMTalkVoiceReq msgInfo) {
//        AudioMessage audioMessage = new AudioMessage();
//        audioMessage.setFromId(msgInfo.getFromUserId());
//        audioMessage.setMsgId(msgInfo.getMsgId());
//        audioMessage.setToId(msgInfo.getToSessionId());
//        audioMessage.setSequence(msgInfo.getSequence());
//        audioMessage.setSerializedSize(msgInfo.getSerializedSize());
//
//        ByteString bytes = msgInfo.getVoiceData();
//        byte[] audioStream = bytes.toByteArray();
//        Log.i("cimtalk", "msgInfo.getSerializedSize:" + msgInfo.getSerializedSize());
//        Log.i("cimtalk", "audioStream.length:" + audioStream.length);
//        String audioSavePath = FileUtil.saveAudioResourceToFile(audioStream, audioMessage.getFromId());
//        audioMessage.setAudiolength(audioStream.length);
//        audioMessage.setAudioPath(audioSavePath);
//        return audioMessage;
//    }


    public void onTalkCallReq(IMTalk.IMTalkCallReq msgData, Boolean isFromPc) {
        Log.i("cimtalk", "onTalkCallReq");
        Log.i("cimtalk", "onTalkCallReq isFromPc:" + isFromPc);
        this.isFromPc = isFromPc;
        msgId = msgData.getMsgId();

        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setFromId(msgData.getFromUserId());
        messageEntity.setToId(msgData.getToSessionId());
        triggerEvent(new MessageTalkEvent(MessageTalkEvent.Event.MSG_TALK_CALL_REQ, messageEntity));
    }

    public void onTalkCallRes(IMTalk.IMTalkCallRes msgData, Boolean isFromPc) {
        Log.i("cimtalk", "onTalkCallRes");
        this.isFromPc = isFromPc;
        IMBaseDefine.TalkResultType code = msgData.getResultCode();
        switch (code) {
            case FAILED:
                triggerEvent(new MessageTalkEvent(MessageTalkEvent.Event.MSG_TALK_CALL_RES_FAILED));
                break;
            case SUCCESS:
                triggerEvent(new MessageTalkEvent(MessageTalkEvent.Event.MSG_TALK_CALL_RES_SUCCESS));
                break;
            case SERVICE_UNAVAILABLE:
                triggerEvent(new MessageTalkEvent(MessageTalkEvent.Event.MSG_TALK_CALL_RES_SERVICE_UNAVAILABLE));
                break;
        }
    }

    public void onTalkAllocReq(IMTalk.IMTalkAllocReq msgData) {
        Log.i("cimtalk", "onTalkAllocReq");
        triggerEvent(new MessageTalkEvent(MessageTalkEvent.Event.MSG_TALK_ALLOC_REQ));
    }

    public void onTalkAllocRes(IMTalk.IMTalkAllocRes msgData) {

        IMBaseDefine.TalkResultType code = msgData.getResultCode();
        Log.i("cimtalk", "onTalkAllocRes code:" + code);
        switch (code) {
            case FAILED:
                triggerEvent(new MessageTalkEvent(MessageTalkEvent.Event.MSG_TALK_ALLOC_RES_FAILED));
                break;
            case SUCCESS:
                MessageEntity messageEntity = new MessageEntity();
                messageEntity.setFromId(msgData.getFromUserId());
                messageEntity.setMsgId(msgData.getMsgId());
                messageEntity.setToId(msgData.getToSessionId());
                triggerEvent(new MessageTalkEvent(MessageTalkEvent.Event.MSG_TALK_ALLOC_RES_SUCCESS, messageEntity));
                break;
            case SERVICE_UNAVAILABLE:
                triggerEvent(new MessageTalkEvent(MessageTalkEvent.Event.MSG_TALK_ALLOC_RES_SERVICE_UNAVAILABLE));
                break;
            default:
                triggerEvent(new MessageTalkEvent(MessageTalkEvent.Event.MSG_TALK_ALLOC_RES_FAILED));

        }
    }

    public void onTalkVoiceReq(IMTalk.IMTalkVoiceReq msgData) {
        Log.i("cimtalk", "onTalkVoiceReq");
//        AudioMessage msg = analyzeAudio(msgData);//解析语音

        ByteString bytes = msgData.getVoiceData();
        byte[] audioStream = bytes.toByteArray();

        Log.i("cimtalk", "isFromPc:" + isFromPc);
        if (isFromPc) {
            try {
                audioStream = ByteUtils.bigtolittle(audioStream);//大端转小端
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        triggerEvent(new MessageTalkEvent(MessageTalkEvent.Event.MSG_TALK_VOICE_REQ, audioStream));
    }

    public void onTalkVoiceRes(IMTalk.IMTalkVoiceRes msgData) {
        Log.i("cimtalk", "onTalkVoiceRes");
        IMBaseDefine.TalkResultType code = msgData.getResultCode();
        switch (code) {
            case FAILED:
                triggerEvent(new MessageTalkEvent(MessageTalkEvent.Event.MSG_TALK_VOICE_RES_FAILED));
                break;
            case SUCCESS:
                MessageEntity messageEntity = new MessageEntity();
                messageEntity.setFromId(msgData.getFromUserId());
                messageEntity.setMsgId(msgData.getMsgId());
                messageEntity.setToId(msgData.getToSessionId());
                triggerEvent(new MessageTalkEvent(MessageTalkEvent.Event.MSG_TALK_VOICE_RES_SUCCESS, messageEntity));
                break;
            case SERVICE_UNAVAILABLE:
                triggerEvent(new MessageTalkEvent(MessageTalkEvent.Event.MSG_TALK_VOICE_RES_SERVICE_UNAVAILABLE));
                break;
        }
    }

    public void onTalkReleaseReq(IMTalk.IMTalkReleaseReq msgData) {
        Log.i("cimtalk", "onTalkReleaseReq");
        triggerEvent(new MessageTalkEvent(MessageTalkEvent.Event.MSG_TALK_RELEASE_REQ));
    }

    public void onTalkReleaseRes(IMTalk.IMTalkReleaseRes msgData) {
        Log.i("cimtalk", "onTalkReleaseRes");
        IMBaseDefine.TalkResultType code = msgData.getResultCode();
        switch (code) {
            case FAILED:
                triggerEvent(new MessageTalkEvent(MessageTalkEvent.Event.MSG_TALK_RELEASE_RES_FAILED));
                break;
            case SUCCESS:
                triggerEvent(new MessageTalkEvent(MessageTalkEvent.Event.MSG_TALK_RELEASE_RES_SUCCESS));
                break;
            case SERVICE_UNAVAILABLE:
                triggerEvent(new MessageTalkEvent(MessageTalkEvent.Event.MSG_TALK_RELEASE_RES_SERVICE_UNAVAILABLE));
                break;
        }
    }

    public void onTalkHungUpReq(IMTalk.IMTalkHangUpReq msgData) {
        Log.i("cimtalk", "onTalkHungUpReq");
        //收到挂断电话请求
        triggerEvent(new MessageTalkEvent(MessageTalkEvent.Event.MSG_TALK_HUNG_UP_REQ));
    }

    public void onTalkHungUpRes(IMTalk.IMTalkHangUpRes msgData) {
        Log.i("cimtalk", "onTalkHungUpRes");
        IMBaseDefine.TalkResultType code = msgData.getResultCode();
        switch (code) {
            case FAILED:
                triggerEvent(new MessageTalkEvent(MessageTalkEvent.Event.MSG_TALK_HUNG_UP_RES_FAILED));
                break;
            case SUCCESS:
                triggerEvent(new MessageTalkEvent(MessageTalkEvent.Event.MSG_TALK_HUNG_UP_RES_SUCCESS));
                break;
            case SERVICE_UNAVAILABLE:
                triggerEvent(new MessageTalkEvent(MessageTalkEvent.Event.MSG_TALK_HUNG_UP_RES_SERVICE_UNAVAILABLE));
                break;
        }
    }
//=======================================以上是收到响应==============================================
    //初始化连接：
    //TalkStatus:TALK_INIT
    //Android1发起请求：sendTalkCallReq->onTalkCallRes
    //TalkStatus:TALK_CALLING
    //Android2接收请求：onTalkCallReq->sendTalkCallRes

    //发送语音：
    //Android1发起语音请求：TalkStatus:TALK_READY:sendTalkAllocReq->onTalkAllocRes ; TalkStatus:TALK_SPEAK:开始说话sendTalkVoiceReq -> onTalkVoiceRes(是否重发) ; TalkStatus:TALK_READY结束语音：sendTalkReleaseReq->onTalkReleaseRes
    //Android2:接受语音请求：onTalkAllocReq->sendTalkAllocRes；                   ->//TalkStatus:TALK_LISTEN onTalkVoiceReq(实时播放) -> sendTalkVoiceRes        TalkStatus:TALK_READY:onTalkReleaseReq->sendTalkReleaseRes
    //Android2发起语音请求同理：READY状态下可以发起allocReq

    //挂断：
    //TalkStatus:TALK_END
    //Android1发起挂断请求：sendTalkHungUpReq->onTalkHungUpRes
    //Android2接受挂断请求：onTalkHungUpReq->sendTalkHungUpRes

//=======================================以下是发送请求==============================================

    public void sendTalkCallReq(MessageEntity msgEntity) {
        Log.i("cimtalk", "sendTalkCallReq");
        IMTalk.IMTalkCallReq msgData = IMTalk.IMTalkCallReq.newBuilder().setFromUserId(msgEntity.getFromId())
                .setMsgId(msgId)
                .setToSessionId(msgEntity.getToId())
                .setCreateTime(msgEntity.getCreated()).build();

        int sid = IMBaseDefine.ServiceID.SID_TALK_VALUE;
        int cid = IMBaseDefine.TalkCmdID.CID_TALK_CALL_REQ_VALUE;

        IMSocketManager.instance().sendRequest(msgData, sid, cid, true);
    }

    public void sendTalkCallRes(MessageEntity msgEntity, boolean isAccpted) {
        Log.i("cimtalk", "sendTalkCallRes isAccpted：" + isAccpted);
        IMTalk.IMTalkCallRes.Builder builder = IMTalk.IMTalkCallRes.newBuilder();
        if (isAccpted) {
            builder.setResultCode(IMBaseDefine.TalkResultType.SUCCESS);
        } else {
            builder.setResultCode(IMBaseDefine.TalkResultType.FAILED);
        }
        builder.setFromUserId(msgEntity.getFromId());
        builder.setMsgId(msgId);
        builder.setToSessionId(msgEntity.getToId());

        IMTalk.IMTalkCallRes msgData = builder.build();

        int sid = IMBaseDefine.ServiceID.SID_TALK_VALUE;
        int cid = IMBaseDefine.TalkCmdID.CID_TALK_CALL_RSP_VALUE;
        IMSocketManager.instance().sendRequest(msgData, sid, cid, true);

    }

    public void sendTalkAllocReq(MessageEntity msgEntity) {
        Log.i("cimtalk", "sendTalkAllocReq");
        IMTalk.IMTalkAllocReq msgData = IMTalk.IMTalkAllocReq.newBuilder().setFromUserId(msgEntity.getFromId())
                .setMsgId(msgId)
                .setToSessionId(msgEntity.getToId())
                .build();
        int sid = IMBaseDefine.ServiceID.SID_TALK_VALUE;
        int cid = IMBaseDefine.TalkCmdID.CID_TALK_ALLOC_REQ_VALUE;
        IMSocketManager.instance().sendRequest(msgData, sid, cid, true);
    }

    public void sendTalkAllocRes(MessageEntity msgEntity, Boolean isCanSpeak) {
        Log.i("cimtalk", "sendTalkAllocRes isCanSpeak:" + isCanSpeak);
        IMTalk.IMTalkAllocRes.Builder builder = IMTalk.IMTalkAllocRes.newBuilder();
        if (isCanSpeak) {
            builder.setResultCode(IMBaseDefine.TalkResultType.SUCCESS);
        } else {
            builder.setResultCode(IMBaseDefine.TalkResultType.FAILED);
        }
        builder.setFromUserId(msgEntity.getFromId());
        builder.setMsgId(msgId);
        builder.setToSessionId(msgEntity.getToId());
        IMTalk.IMTalkAllocRes msgData = builder.build();

        int sid = IMBaseDefine.ServiceID.SID_TALK_VALUE;
        int cid = IMBaseDefine.TalkCmdID.CID_TALK_ALLOC_RSP_VALUE;
        IMSocketManager.instance().sendRequest(msgData, sid, cid, true);
    }


    public void sendTalkVoiceReq(AudioMessage msgEntity, byte[] content) {
        Log.i("cimtalk", "sendTalkVoiceReq");
        Log.i("cimtalk", "isFromPc:" + isFromPc);
//        if (isFromPc) {
//            try {
//                content = ByteUtils.littletobig(content);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        IMTalk.IMTalkVoiceReq msgData = IMTalk.IMTalkVoiceReq.newBuilder().setFromUserId(msgEntity.getFromId())
                .setSequence(++seq)
                .setVoiceData(ByteString.copyFrom(content))
                .setMsgId(msgId)
                .setToSessionId(msgEntity.getToId())
                .build();

        int sid = IMBaseDefine.ServiceID.SID_TALK_VALUE;
        int cid = IMBaseDefine.TalkCmdID.CID_TALK_VOICE_REQ_VALUE;
        IMSocketManager.instance().sendRequest(msgData, sid, cid, true);
    }

    public void sendTalkVoiceRes(MessageEntity msgEntity, Boolean isNeedReSend) {
        Log.i("cimtalk", "sendTalkVoiceRes");
        IMTalk.IMTalkVoiceRes.Builder builder = IMTalk.IMTalkVoiceRes.newBuilder();
        if (isNeedReSend) {
            builder.setResultCode(IMBaseDefine.TalkResultType.FAILED);
        } else {
            builder.setResultCode(IMBaseDefine.TalkResultType.SUCCESS);
        }
        builder.setFromUserId(msgEntity.getFromId());
        builder.setMsgId(msgId);
        builder.setToSessionId(msgEntity.getToId());

        IMTalk.IMTalkVoiceRes msgData = builder.build();
        int sid = IMBaseDefine.ServiceID.SID_TALK_VALUE;
        int cid = IMBaseDefine.TalkCmdID.CID_TALK_VOICE_RSP_VALUE;
        IMSocketManager.instance().sendRequest(msgData, sid, cid, true);
    }

    public void sendTalkReleaseReq(MessageEntity msgEntity) {
        Log.i("cimtalk", "sendTalkReleaseReq");
        IMTalk.IMTalkReleaseReq msgData = IMTalk.IMTalkReleaseReq.newBuilder().setFromUserId(msgEntity.getFromId())
                .setMsgId(msgId)
                .setToSessionId(msgEntity.getToId())
                .build();
        int sid = IMBaseDefine.ServiceID.SID_TALK_VALUE;
        int cid = IMBaseDefine.TalkCmdID.CID_TALK_RELEASE_REQ_VALUE;
        IMSocketManager.instance().sendRequest(msgData, sid, cid, true);
    }

    public void sendTalkReleaseRes(MessageEntity msgEntity) {
        Log.i("cimtalk", "sendTalkReleaseRes");
        IMTalk.IMTalkReleaseRes.Builder builder = IMTalk.IMTalkReleaseRes.newBuilder();
        builder.setResultCode(IMBaseDefine.TalkResultType.SUCCESS);
        builder.setFromUserId(msgEntity.getFromId());
        builder.setMsgId(msgId);
        builder.setToSessionId(msgEntity.getToId());
        IMTalk.IMTalkReleaseRes msgData = builder.build();
        int sid = IMBaseDefine.ServiceID.SID_TALK_VALUE;
        int cid = IMBaseDefine.TalkCmdID.CID_TALK_RELEASE_RSP_VALUE;
        IMSocketManager.instance().sendRequest(msgData, sid, cid, true);
    }


    public void sendTalkHungUpReq(MessageEntity msgEntity) {
        Log.i("cimtalk", "sendTalkHungUpReq");
        IMTalk.IMTalkHangUpReq msgData = IMTalk.IMTalkHangUpReq.newBuilder().setFromUserId(msgEntity.getFromId())
                .setMsgId(msgId)
                .setToSessionId(msgEntity.getToId())
                .build();
        int sid = IMBaseDefine.ServiceID.SID_TALK_VALUE;
        int cid = IMBaseDefine.TalkCmdID.CID_TALK_HANGUP_REQ_VALUE;
        IMSocketManager.instance().sendRequest(msgData, sid, cid, true);
    }

    public void sendTalkHungUpRes(MessageEntity msgEntity) {
        Log.i("cimtalk", "sendTalkHungUpRes");
        IMTalk.IMTalkCallRes.Builder builder = IMTalk.IMTalkCallRes.newBuilder();
        builder.setResultCode(IMBaseDefine.TalkResultType.SUCCESS);
        builder.setFromUserId(msgEntity.getFromId());
        builder.setMsgId(msgId);
        builder.setToSessionId(msgEntity.getToId());

        IMTalk.IMTalkCallRes msgData = builder.build();

        int sid = IMBaseDefine.ServiceID.SID_TALK_VALUE;
        int cid = IMBaseDefine.TalkCmdID.CID_TALK_HANGUP_RSP_VALUE;
        IMSocketManager.instance().sendRequest(msgData, sid, cid, true);
    }


    /**
     * 自身的事件驱动
     *
     * @param event
     */
    public void triggerEvent(Object event) {
        EventBus.getDefault().post(event);
    }


}
