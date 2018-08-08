package com.cooyet.im.imservice.manager;

import android.util.Log;

import com.cooyet.im.imservice.manager.talk.IMTalkManager;
import com.cooyet.im.protobuf.IMBaseDefine;
import com.cooyet.im.protobuf.IMBuddy;
import com.cooyet.im.protobuf.IMGroup;
import com.cooyet.im.protobuf.IMLogin;
import com.cooyet.im.protobuf.IMMessage;
import com.cooyet.im.protobuf.IMTalk;
import com.cooyet.im.utils.Logger;
import com.google.protobuf.CodedInputStream;

import java.io.IOException;

/**
 * ronghua
 * 消息分发中心，处理消息服务器返回的数据包
 * 1. decode  header与body的解析
 * 2. 分发
 */
public class IMPacketDispatcher {
    private static Logger logger = Logger.getLogger(IMPacketDispatcher.class);

    /**
     * @param commandId
     * @param buffer    有没有更加优雅的方式
     */
    public static void loginPacketDispatcher(int commandId, CodedInputStream buffer) {
        try {
            switch (commandId) {
                case IMBaseDefine.LoginCmdID.CID_LOGIN_RES_USERLOGIN_VALUE:
                    IMLogin.IMLoginRes imLoginRes = IMLogin.IMLoginRes.parseFrom(buffer);
                    IMLoginManager.instance().onRepMsgServerLogin(imLoginRes);
                    return;

                case IMBaseDefine.LoginCmdID.CID_LOGIN_RES_LOGINOUT_VALUE:
                    IMLogin.IMLogoutRsp imLogoutRsp = IMLogin.IMLogoutRsp.parseFrom(buffer);
                    IMLoginManager.instance().onRepLoginOut(imLogoutRsp);
                    return;

                case IMBaseDefine.LoginCmdID.CID_LOGIN_KICK_USER_VALUE:
                    IMLogin.IMKickUser imKickUser = IMLogin.IMKickUser.parseFrom(buffer);
                    IMLoginManager.instance().onKickout(imKickUser);
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.e("loginPacketDispatcher# error,cid:%d", commandId);
        }
    }

    public static void buddyPacketDispatcher(int commandId, CodedInputStream buffer) {
        try {
            switch (commandId) {
                case IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_ALL_USER_RESPONSE_VALUE:
                    Log.i("kkkk", "buddyPacketDispatcher1");
                    IMBuddy.IMAllUserRsp imAllUserRsp = IMBuddy.IMAllUserRsp.parseFrom(buffer);
                    IMContactManager.instance().onRepAllUsers(imAllUserRsp);
                    return;

                case IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_USER_INFO_RESPONSE_VALUE:
                    Log.i("kkkk", "buddyPacketDispatcher2");
                    IMBuddy.IMUsersInfoRsp imUsersInfoRsp = IMBuddy.IMUsersInfoRsp.parseFrom(buffer);
                    IMContactManager.instance().onRepDetailUsers(imUsersInfoRsp);
                    return;

                case IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_RECENT_CONTACT_SESSION_RESPONSE_VALUE:
                    Log.i("kkkk", "buddyPacketDispatcher3");
                    IMBuddy.IMRecentContactSessionRsp recentContactSessionRsp = IMBuddy.IMRecentContactSessionRsp.parseFrom(buffer);
                    IMSessionManager.instance().onRepRecentContacts(recentContactSessionRsp);
                    return;

                case IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_REMOVE_SESSION_RES_VALUE:
                    Log.i("kkkk", "buddyPacketDispatcher4");
                    IMBuddy.IMRemoveSessionRsp removeSessionRsp = IMBuddy.IMRemoveSessionRsp.parseFrom(buffer);
                    IMSessionManager.instance().onRepRemoveSession(removeSessionRsp);
                    return;

                case IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_PC_LOGIN_STATUS_NOTIFY_VALUE:
                    Log.i("kkkk", "buddyPacketDispatcher5");
                    IMBuddy.IMPCLoginStatusNotify statusNotify = IMBuddy.IMPCLoginStatusNotify.parseFrom(buffer);
                    IMLoginManager.instance().onLoginStatusNotify(statusNotify);
                    return;

                case IMBaseDefine.BuddyListCmdID.CID_BUDDY_LIST_DEPARTMENT_RESPONSE_VALUE:
                    Log.i("kkkk", "buddyPacketDispatcher6");
                    IMBuddy.IMDepartmentRsp departmentRsp = IMBuddy.IMDepartmentRsp.parseFrom(buffer);
                    IMContactManager.instance().onRepDepartment(departmentRsp);
                    return;

            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.e("buddyPacketDispatcher# error,cid:%d", commandId);
        }
    }

    public static void msgPacketDispatcher(int commandId, CodedInputStream buffer) {
        try {
            switch (commandId) {
                case IMBaseDefine.MessageCmdID.CID_MSG_DATA_ACK_VALUE:
                    // have some problem  todo
                    return;

                case IMBaseDefine.MessageCmdID.CID_MSG_LIST_RESPONSE_VALUE:
                    IMMessage.IMGetMsgListRsp rsp = IMMessage.IMGetMsgListRsp.parseFrom(buffer);
                    IMMessageManager.instance().onReqHistoryMsg(rsp);
                    return;

                case IMBaseDefine.MessageCmdID.CID_MSG_DATA_VALUE:
                    IMMessage.IMMsgData imMsgData = IMMessage.IMMsgData.parseFrom(buffer);
                    IMMessageManager.instance().onRecvMessage(imMsgData);
                    return;

                case IMBaseDefine.MessageCmdID.CID_MSG_READ_NOTIFY_VALUE:
                    IMMessage.IMMsgDataReadNotify readNotify = IMMessage.IMMsgDataReadNotify.parseFrom(buffer);
                    IMUnreadMsgManager.instance().onNotifyRead(readNotify);
                    return;
                case IMBaseDefine.MessageCmdID.CID_MSG_UNREAD_CNT_RESPONSE_VALUE:
                    IMMessage.IMUnreadMsgCntRsp unreadMsgCntRsp = IMMessage.IMUnreadMsgCntRsp.parseFrom(buffer);
                    IMUnreadMsgManager.instance().onRepUnreadMsgContactList(unreadMsgCntRsp);
                    return;

                case IMBaseDefine.MessageCmdID.CID_MSG_GET_BY_MSG_ID_RES_VALUE:
                    IMMessage.IMGetMsgByIdRsp getMsgByIdRsp = IMMessage.IMGetMsgByIdRsp.parseFrom(buffer);
                    IMMessageManager.instance().onReqMsgById(getMsgByIdRsp);
                    break;

            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.e("msgPacketDispatcher# error,cid:%d", commandId);
        }
    }

    public static void groupPacketDispatcher(int commandId, CodedInputStream buffer) {
        try {
            switch (commandId) {
                case IMBaseDefine.GroupCmdID.CID_GROUP_CREATE_RESPONSE_VALUE:
                    IMGroup.IMGroupCreateRsp groupCreateRsp = IMGroup.IMGroupCreateRsp.parseFrom(buffer);
                    IMGroupManager.instance().onReqCreateTempGroup(groupCreateRsp);
                    return;

                case IMBaseDefine.GroupCmdID.CID_GROUP_NORMAL_LIST_RESPONSE_VALUE:
                    IMGroup.IMNormalGroupListRsp normalGroupListRsp = IMGroup.IMNormalGroupListRsp.parseFrom(buffer);
                    IMGroupManager.instance().onRepNormalGroupList(normalGroupListRsp);
                    return;

                case IMBaseDefine.GroupCmdID.CID_GROUP_INFO_RESPONSE_VALUE:
                    IMGroup.IMGroupInfoListRsp groupInfoListRsp = IMGroup.IMGroupInfoListRsp.parseFrom(buffer);
                    IMGroupManager.instance().onRepGroupDetailInfo(groupInfoListRsp);
                    return;

                case IMBaseDefine.GroupCmdID.CID_GROUP_CHANGE_MEMBER_RESPONSE_VALUE:
                    IMGroup.IMGroupChangeMemberRsp groupChangeMemberRsp = IMGroup.IMGroupChangeMemberRsp.parseFrom(buffer);
                    IMGroupManager.instance().onReqChangeGroupMember(groupChangeMemberRsp);
                    return;

                case IMBaseDefine.GroupCmdID.CID_GROUP_CHANGE_MEMBER_NOTIFY_VALUE:
                    IMGroup.IMGroupChangeMemberNotify notify = IMGroup.IMGroupChangeMemberNotify.parseFrom(buffer);
                    IMGroupManager.instance().receiveGroupChangeMemberNotify(notify);
                case IMBaseDefine.GroupCmdID.CID_GROUP_SHIELD_GROUP_RESPONSE_VALUE:
                    //todo
                    return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.e("groupPacketDispatcher# error,cid:%d", commandId);
        }
    }

    public static void talkPacketDispatcher(int commandId, CodedInputStream buffer, Boolean isFromPc) {
        try {
            switch (commandId) {
                case IMBaseDefine.TalkCmdID.CID_TALK_CALL_REQ_VALUE:
                    IMTalk.IMTalkCallReq callReq = IMTalk.IMTalkCallReq.parseFrom(buffer);
                    IMTalkManager.instance().onTalkCallReq(callReq, isFromPc);
                    return;
                case IMBaseDefine.TalkCmdID.CID_TALK_CALL_RSP_VALUE:
                    IMTalk.IMTalkCallRes callRes = IMTalk.IMTalkCallRes.parseFrom(buffer);
                    IMTalkManager.instance().onTalkCallRes(callRes, isFromPc);
                    return;
                case IMBaseDefine.TalkCmdID.CID_TALK_ALLOC_REQ_VALUE:
                    IMTalk.IMTalkAllocReq allocReq = IMTalk.IMTalkAllocReq.parseFrom(buffer);
                    IMTalkManager.instance().onTalkAllocReq(allocReq);
                    return;

                case IMBaseDefine.TalkCmdID.CID_TALK_ALLOC_RSP_VALUE:
                    IMTalk.IMTalkAllocRes allocRes = IMTalk.IMTalkAllocRes.parseFrom(buffer);
                    IMTalkManager.instance().onTalkAllocRes(allocRes);
                    return;
                case IMBaseDefine.TalkCmdID.CID_TALK_VOICE_REQ_VALUE:
                    IMTalk.IMTalkVoiceReq voiceReq = IMTalk.IMTalkVoiceReq.parseFrom(buffer);
                    IMTalkManager.instance().onTalkVoiceReq(voiceReq);
                    return;
                case IMBaseDefine.TalkCmdID.CID_TALK_VOICE_RSP_VALUE:
                    IMTalk.IMTalkVoiceRes voiceRes = IMTalk.IMTalkVoiceRes.parseFrom(buffer);
                    IMTalkManager.instance().onTalkVoiceRes(voiceRes);
                    return;

                case IMBaseDefine.TalkCmdID.CID_TALK_RELEASE_REQ_VALUE:
                    IMTalk.IMTalkReleaseReq releaseReq = IMTalk.IMTalkReleaseReq.parseFrom(buffer);
                    IMTalkManager.instance().onTalkReleaseReq(releaseReq);
                    return;
                case IMBaseDefine.TalkCmdID.CID_TALK_RELEASE_RSP_VALUE:
                    IMTalk.IMTalkReleaseRes releaseRes = IMTalk.IMTalkReleaseRes.parseFrom(buffer);
                    IMTalkManager.instance().onTalkReleaseRes(releaseRes);
                    return;
                case IMBaseDefine.TalkCmdID.CID_TALK_HANGUP_REQ_VALUE:
                    IMTalk.IMTalkHangUpReq huangUpReq = IMTalk.IMTalkHangUpReq.parseFrom(buffer);
                    IMTalkManager.instance().onTalkHungUpReq(huangUpReq);
                    return;
                case IMBaseDefine.TalkCmdID.CID_TALK_HANGUP_RSP_VALUE:
                    IMTalk.IMTalkHangUpRes huangUpRes = IMTalk.IMTalkHangUpRes.parseFrom(buffer);
                    IMTalkManager.instance().onTalkHungUpRes(huangUpRes);
                    return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.e("talkPacketDispatcher# error,cid:%d", commandId);
        }
    }
}
