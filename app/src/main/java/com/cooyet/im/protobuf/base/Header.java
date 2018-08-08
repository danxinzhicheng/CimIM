package com.cooyet.im.protobuf.base;

import com.cooyet.im.config.SysConstant;
import com.cooyet.im.utils.Logger;

/**
 * TCP协议的头文件
 *
 * @author dolphinWang
 * @time 2014/04/30
 */
public class Header {

    private Logger logger = Logger.getLogger(Header.class);


    private int length; // 数据包长度，包括包头

    private short version = 2018; // 版本号

//    private short flag;

    private short serviceId; // SID

    private short commandId; // CID

    private short seqnum;

//    private short reserved; // 保留，可用于如序列号等

    private int userId;
    private int channel;


    public Header() {

        length = 0;

        version = 0;

        serviceId = 0;

        commandId = 0;

//        reserved = 0;

//        flag = 0;

        seqnum = 0;
        userId = 0;
        channel = 0;

    }

//    public short getFlag() {
//
//        return flag;
//
//    }
//
//
//    public void setFlag(short flag) {
//
//        this.flag = flag;
//
//    }


    public short getSeqnum() {

        return seqnum;

    }


    public void setSeqnum(short seq) {

        this.seqnum = seq;

    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }


    /**
     * 头文件的压包函数
     *
     * @return 数据包
     */

    public DataBuffer encode() {
        DataBuffer db = new DataBuffer(SysConstant.PROTOCOL_HEADER_LENGTH);
        db.writeInt(length);
        db.writeShort(version);
        db.writeShort(seqnum);
//        db.writeShort(flag);
        db.writeShort(serviceId);
        db.writeShort(commandId);

//        db.writeShort(reserved);
        db.writeInt(channel);
        db.writeInt(userId);
        return db;
    }


    /**
     * 头文件的解包函数
     *
     * @param buffer
     */

    public void decode(DataBuffer buffer) {

        if (null == buffer) {
            return;
        }
        try {
            length = buffer.readInt();
            version = buffer.readShort();
            seqnum = buffer.readShort();
//            flag = buffer.readShort();
            serviceId = buffer.readShort();
            commandId = buffer.readShort();
            channel = buffer.readInt();
//            reserved = buffer.readShort();

            userId = buffer.readInt();
            logger.d(

                    "decode header, length:%d, version:%d, flag:%d serviceId:%d, commandId:%d, reserved:%d,seq:%d",

                    length, version, serviceId, commandId,

                    seqnum, userId, channel);
        } catch (Exception e) {
            logger.e(e.getMessage());
        }
    }


    @Override
    public String toString() {
        return "Header{" +
                "length=" + length +
                ", version=" + version +
                ", serviceId=" + serviceId +
                ", commandId=" + commandId +
                ", seqnum=" + seqnum +
                ", userId=" + userId +
                ", channel=" + channel +
                '}';
    }

    public short getCommandId() {
        return commandId;
    }

    public void setCommandId(short commandID) {
        this.commandId = commandID;
    }

    public short getServiceId() {
        return serviceId;
    }

    public void setServiceId(short serviceID) {
        this.serviceId = serviceID;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public short getVersion() {
        return version;
    }

    public void setVersion(short version) {
        this.version = version;
    }

//    public int getReserved() {
//        return reserved;
//    }
//
//    public void setReserved(short reserved) {
//        this.reserved = reserved;
//    }
}
