package com.cooyet.im.imservice.entity;

import com.cooyet.im.config.DBConstant;
import com.cooyet.im.DB.entity.GroupEntity;
import com.cooyet.im.DB.entity.SessionEntity;
import com.cooyet.im.DB.entity.UserEntity;
import com.cooyet.im.imservice.manager.IMContactManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : ronghua.xie on 15-1-8.
 * @email : ronghua.xie@cooyet.com.
 */
public class RecentInfo {
    /**sessionEntity*/
    private String sessionKey;
    private int peerId;
    private int sessionType;
    private int latestMsgType;
    private int latestMsgId;
    private String latestMsgData;
    private int updateTime;

    /**unreadEntity*/
    private int unReadCnt;

    /**group/userEntity*/
    private String name;
    private List<String> avatar;

    /**是否置顶*/
    private boolean isTop = false;
    /**是否屏蔽信息*/
    private boolean isForbidden = false;

    @Override
    public String toString() {
        return "RecentInfo{" +
                "sessionKey='" + sessionKey + '\'' +
                ", peerId=" + peerId +
                ", sessionType=" + sessionType +
                ", latestMsgType=" + latestMsgType +
                ", latestMsgId=" + latestMsgId +
                ", latestMsgData='" + latestMsgData + '\'' +
                ", updateTime=" + updateTime +
                ", unReadCnt=" + unReadCnt +
                ", name='" + name + '\'' +
                ", avatar=" + avatar +
                ", isTop=" + isTop +
                ", isForbidden=" + isForbidden +
                '}';
    }

    public RecentInfo(){}
    public RecentInfo(SessionEntity sessionEntity,UserEntity entity,UnreadEntity unreadEntity){
        sessionKey = sessionEntity.getSessionKey();
        peerId = sessionEntity.getPeerId();
        sessionType = DBConstant.SESSION_TYPE_SINGLE;
        latestMsgType = sessionEntity.getLatestMsgType();
        latestMsgId = sessionEntity.getLatestMsgId();
        latestMsgData = sessionEntity.getLatestMsgData();
        updateTime = sessionEntity.getUpdated();

        if(unreadEntity !=null)
        unReadCnt = unreadEntity.getUnReadCnt();

        if(entity != null){
            name = entity.getMainName();
            ArrayList<String> avatarList = new ArrayList<>();
            avatarList.add(entity.getAvatar());
            avatar = avatarList;
        }
    }


    public RecentInfo(SessionEntity sessionEntity,GroupEntity groupEntity,UnreadEntity unreadEntity){
        sessionKey =  sessionEntity.getSessionKey();
        peerId = sessionEntity.getPeerId();
        sessionType = DBConstant.SESSION_TYPE_GROUP;
        latestMsgType = sessionEntity.getLatestMsgType();
        latestMsgId = sessionEntity.getLatestMsgId();
        latestMsgData = sessionEntity.getLatestMsgData();
        updateTime = sessionEntity.getUpdated();

        if(unreadEntity !=null)
        unReadCnt = unreadEntity.getUnReadCnt();

        if(groupEntity !=null) {
            ArrayList<String>  avatarList = new ArrayList<>();
            name = groupEntity.getMainName();

            // 免打扰的设定
            int status = groupEntity.getStatus();
            if (status == DBConstant.GROUP_STATUS_SHIELD){
                isForbidden = true;
            }

            ArrayList<Integer> list =  new ArrayList<>();
            list.addAll(groupEntity.getlistGroupMemberIds());

            for(Integer userId:list){
                UserEntity entity = IMContactManager.instance().findContact(userId);
                if(entity!=null){
                    avatarList.add(entity.getAvatar());
                }
                if(avatarList.size()>=4){
                    break;
                }
            }
            avatar = avatarList;
        }
        //avatar
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public int getPeerId() {
        return peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public int getSessionType() {
        return sessionType;
    }

    public void setSessionType(int sessionType) {
        this.sessionType = sessionType;
    }

    public int getLatestMsgType() {
        return latestMsgType;
    }

    public void setLatestMsgType(int latestMsgType) {
        this.latestMsgType = latestMsgType;
    }

    public int getLatestMsgId() {
        return latestMsgId;
    }

    public void setLatestMsgId(int latestMsgId) {
        this.latestMsgId = latestMsgId;
    }

    public String getLatestMsgData() {
        return latestMsgData;
    }

    public void setLatestMsgData(String latestMsgData) {
        this.latestMsgData = latestMsgData;
    }

    public int getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(int updateTime) {
        this.updateTime = updateTime;
    }

    public int getUnReadCnt() {
        return unReadCnt;
    }

    public void setUnReadCnt(int unReadCnt) {
        this.unReadCnt = unReadCnt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getAvatar() {
        return avatar;
    }

    public void setAvatar(List<String> avatar) {
        this.avatar = avatar;
    }

    public boolean isTop() {
        return isTop;
    }
    public boolean isForbidden()
    {
        return isForbidden;
    }

    public void setTop(boolean isTop) {
        this.isTop = isTop;
    }

    public void setForbidden(boolean isForbidden)
    {
        this.isForbidden = isForbidden;
    }
}
