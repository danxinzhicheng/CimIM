package com.cooyet.im.imservice.manager;

/**
 * Created by user on 2018/5/31.
 */

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.cooyet.im.config.SysConstant;
import com.cooyet.im.protobuf.IMBaseDefine;
import com.cooyet.im.protobuf.base.DataBuffer;
import com.cooyet.im.protobuf.base.Header;
import com.cooyet.im.utils.CommonUtil;
import com.cooyet.im.utils.Logger;
import com.google.protobuf.GeneratedMessageLite;

import org.jboss.netty.buffer.ChannelBuffers;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * UDT通信
 */
public class IMUdtManager {
    private static final String TAG = "wb_cim";
    private Logger logger = Logger.getLogger(IMUdtManager.class);

    public static int handle = 0;

    public native int startup();

    public native int cleanup();

    public native int socket(String port);

    public native int connect(int socket, String ip, int port);

    public native int close(int socket);

    public native int send(int socket, byte[] buffer, int size, int flags);

    public native byte[] recv(int socket);

    private byte[] result;
    private Handler mSendHandler;
    private HandlerThread mSendThread;
    private Handler mRecvHandler;
    private HandlerThread mRecvThread;

    private Handler mSendMsgHandler;
    private HandlerThread mSendMsgThread;
    private DataBuffer buffer = new DataBuffer();


    /**
     * 单例模式
     */
    private static IMUdtManager inst = new IMUdtManager();

    public static IMUdtManager instance() {
        return inst;
    }

    public IMUdtManager() {
        System.loadLibrary("udt");
    }


    public void releaseUdt() {
        close(handle);
        cleanup();
    }

    /**
     * 初始化发送除发送文字语音之外的获取内容 登录等的线程
     */
    public void initSendThread() {
        mSendThread = new HandlerThread("send-thread");
        mSendThread.start();
        mSendHandler = new Handler(mSendThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    byte[] data = (byte[]) msg.obj;
                    logger.i("send-thread content data length:%d", data.length);
                    int send_count = send(handle, data, data.length, 0);
                    recvFunc();
                }
            }
        };

        //初始化文字语音线程
        initSendMsgThread();
    }

    /**
     * 发送文字，语音消息的线程
     */
    public void initSendMsgThread() {
        mSendMsgThread = new HandlerThread("send-msg-thread");
        mSendMsgThread.start();
        mSendMsgHandler = new Handler(mSendMsgThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                logger.i("handleMessage");
                if (msg.what == 0) {
                    byte[] data = (byte[]) msg.obj;
                    logger.i("send-msg-thread content data length:%d", data.length);
                    int send_count = send(handle, data, data.length, 0);
                }
            }
        };
    }

    //考虑到udp接收包大的时候会自动分包，将收到的包保存到队列中,到达头部指定长度再decode，分发到业务层
    private ArrayList<Byte> bufferList = new ArrayList<>();
    private byte[] resultLast;
    private int length;

    private synchronized void recvFunc() {
        Log.i("recvFunc", "recvFunc current thread name:" + Thread.currentThread().getName());
        Log.i("recvFunc", "bufferList current size:" + bufferList.size());

        result = recv(handle);
        if (result == null) {
            Log.i("recvFunc", "recv result is null...");
            return;
        }
//        Log.i("recvFunc", "recvFunc thread content:" + ByteUtils.bytesToHexString(result));
        Log.i("recvFunc", "recvFunc length:" + result.length);

        if (Arrays.equals(result, resultLast)) {
            Log.i("recvFunc", "recv result is same...");
            return;
        } else {
            resultLast = result;
        }
//        //=========判断最后一次语音时被分割，导致加到队列的 voiceReleaseReq解析失败
//        if (bufferList.size() > 1000 && isVoiceNormalPackage(result)) {
//            bufferList.clear();
//        }
//        //==========

        Boolean isHasHead = isHasHeadPackage(result);
        if (isHasHead) {
            length = getHeadLength(result);
            bufferList.clear();
        }

        //接收到的字节数组保存到字节队列中
        for (int i = 0; i < result.length; i++) {
            bufferList.add(result[i]);
            //循环过程中 达到头部指定长度则回调给上层业务
            if (bufferList.size() == length) {
                Log.i("recvFunc", "===get the real package success===");
                byte[] bytes = new byte[bufferList.size()];
                for (int m = 0; m < bufferList.size(); m++) {
                    bytes[m] = bufferList.get(m);
                }
                IMSocketManager.instance().packetDispatch(bytes);
                //发送完将队列清除
                bufferList.clear();
            }
        }
    }

    /**
     * 接收线程，阻塞线程，5秒之后循环，为了接收对方发来的消息，语音等
     */
    public void initRecvThread() {

        mRecvThread = new HandlerThread("recv-thread");
        mRecvThread.start();
        mRecvHandler = new Handler(mRecvThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    mSendHandler.removeMessages(0);
                    mRecvHandler.sendEmptyMessage(1);

                } else if (msg.what == 1) {
                    recvFunc();
                    mRecvHandler.sendEmptyMessage(1);
                }
            }
        };
        mRecvHandler.sendEmptyMessageDelayed(1, 5000);
    }

    /**
     * 业务层发送总入口
     *
     * @param requset
     * @param header
     * @return
     */
    public boolean sendRequest(final GeneratedMessageLite requset, final Header header, Boolean isSendMsg) {

        logger.i("sendRequest isSendMsg:%s", isSendMsg);
        DataBuffer headerBuffer = header.encode();
        DataBuffer bodyBuffer = new DataBuffer();
        int bodySize = requset.getSerializedSize();
        bodyBuffer.writeBytes(requset.toByteArray());

        DataBuffer buffer = new DataBuffer(SysConstant.PROTOCOL_HEADER_LENGTH + bodySize);
        buffer.writeDataBuffer(headerBuffer);
        buffer.writeDataBuffer(bodyBuffer);

        int data_size = SysConstant.PROTOCOL_HEADER_LENGTH + bodySize;
        byte[] data = buffer.readBytes(data_size);

        if (!isSendMsg) {
            Message msg = Message.obtain();
            msg.what = 0;
            msg.obj = data;
            mSendHandler.sendMessageDelayed(msg, 1000);
        } else {
            Message msg = Message.obtain();
            msg.what = 0;
            msg.obj = data;
            mSendMsgHandler.sendMessageDelayed(msg, 0);
        }
        return true;
    }

    /**
     * 判断包是否包含头部信息 version:暂定的头部协议标识
     *
     * @param content
     * @return
     */
    private Boolean isHasHeadPackage(byte[] content) {
        int length = getHeadLength(content);
        short version = getHeadVersion(content);
        //version:所有发送的头都带有version=2018的内容
        //暂时加个长度判断，被分包的length，理论上是在0-10000字节之内
        if (length > 0 && length < 10000 && (version == SysConstant.PROTOCOL_VERSION || version == SysConstant.PROTOCOL_VERSION_PC)) {
            return true;
        }
        return false;
    }

//    /**
//     * 判断包是否包含头部信息且是否 version:暂定的头部协议标识
//     *
//     * @param content
//     * @return
//     */
//    private Boolean isVoiceNormalPackage(byte[] content) {
//        int length = getHeadLength(content);
//        short version = getHeadVersion(content);
//        //version:所有发送的头都带有version=2018的内容
//        //暂时加个长度判断，被分包的length，理论上是在0-10000字节之内
//        if (length > 0 && length < 30 && (version == SysConstant.PROTOCOL_VERSION || version == SysConstant.PROTOCOL_VERSION_PC)) {
//            return true;
//        }
//        return false;
//    }


    public int getHeadLength(byte[] content) {

        byte[] length_byte = CommonUtil.subBytes(content, 0, 4);
        buffer.setOrignalBuffer(ChannelBuffers.copiedBuffer(length_byte));
        int length = buffer.readInt();
        logger.i("length:%d", length);
        return length;
    }


    public short getHeadVersion(byte[] content) {

        byte[] version_byte = CommonUtil.subBytes(content, 4, 6);
        buffer.setOrignalBuffer(ChannelBuffers.copiedBuffer(version_byte));
        short version = buffer.readShort();
        logger.i("version:%d", version);
        return version;
    }

    public short getHeadSeqNo(byte[] content) {
        byte[] seqNo_byte = CommonUtil.subBytes(content, 6, 8);
        buffer.setOrignalBuffer(ChannelBuffers.copiedBuffer(seqNo_byte));
        short seqNo = buffer.readShort();
        logger.i("seqNo:%d", seqNo);
        return seqNo;
    }

    public short getHeadServiceId(byte[] content) {
        byte[] serviceId_byte = CommonUtil.subBytes(content, 8, 10);
        buffer.setOrignalBuffer(ChannelBuffers.copiedBuffer(serviceId_byte));
        short serviceId = buffer.readShort();
        logger.i("serviceId:%d", serviceId);
        return serviceId;
    }

    public short getHeadCommandId(byte[] content) {
        byte[] commandId_byte = CommonUtil.subBytes(content, 10, 12);
        buffer.setOrignalBuffer(ChannelBuffers.copiedBuffer(commandId_byte));
        short commandId = buffer.readShort();
        logger.i("commandId:%d", commandId);
        return commandId;
    }

    public int getHeadChannel(byte[] content) {

        byte[] channel_byte = CommonUtil.subBytes(content, 12, 16);
        buffer.setOrignalBuffer(ChannelBuffers.copiedBuffer(channel_byte));
        int channel = buffer.readInt();
        logger.i("channel:%d", channel);
        return channel;
    }

    public int getHeadUserId(byte[] content) {
        byte[] userId_byte = CommonUtil.subBytes(content, 16, 20);
        buffer.setOrignalBuffer(ChannelBuffers.copiedBuffer(userId_byte));
        int userId = buffer.readInt();
        logger.i("userId:%d", userId);
        return userId;
    }

    public byte[] getContentReal(byte[] content) {
        byte[] contentReal = CommonUtil.subBytes(content, 20, content.length - 20);
        return contentReal;
    }

    public boolean isVoiceFromPc(byte[] content) {
        int sid = getHeadServiceId(content);
        if (sid == IMBaseDefine.ServiceID.SID_TALK_VALUE) {
            int version = getHeadVersion(content);
            if (version == SysConstant.PROTOCOL_VERSION_PC) {
                return true;
            }
        }
        return false;
    }

}
