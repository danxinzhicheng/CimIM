package com.cooyet.im.imservice.manager;

import android.os.Handler;

import com.cooyet.im.config.SysConstant;
import com.cooyet.im.config.UrlConstant;
import com.cooyet.im.imservice.callback.ListenerQueue;
import com.cooyet.im.imservice.callback.Packetlistener;
import com.cooyet.im.imservice.event.SocketEvent;
import com.cooyet.im.protobuf.IMBaseDefine;
import com.cooyet.im.protobuf.base.DataBuffer;
import com.cooyet.im.protobuf.base.DefaultHeader;
import com.cooyet.im.utils.Logger;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.GeneratedMessageLite;

import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.buffer.ChannelBuffers;

import de.greenrobot.event.EventBus;

/**
 * @author : ronghua.xie on 14-12-30.
 * @email : ronghua.xie@cooyet.com.
 * <p>
 * 业务层面:
 * 长连接建立成功之后，就要发送登陆信息，否则15s之内就会断开
 * 所以connMsg 与 login是强耦合的关系
 */
public class IMSocketManager extends IMManager {

    private Logger logger = Logger.getLogger(IMSocketManager.class);
    private static IMSocketManager inst = new IMSocketManager();

    public static IMSocketManager instance() {
        return inst;
    }

    public IMSocketManager() {
        logger.d("login#creating IMSocketManager");
    }

    private ListenerQueue listenerQueue = ListenerQueue.instance();

    @Override
    public void doOnStart() {
//        connectMsgServer();
    }

    //todo check
    @Override
    public void reset() {
        disconnectMsgServer();
    }

    /**
     * 实现自身的事件驱动
     *
     * @param event
     */
    public void triggerEvent(SocketEvent event) {
        setSocketStatus(event);
        EventBus.getDefault().postSticky(event);
    }

    /**
     * -------------------------------功能方法--------------------------------------
     */

    public void sendRequest(GeneratedMessageLite requset, int sid, int cid, Boolean isSendMsg) {
        sendRequest(requset, sid, cid, null, isSendMsg);
    }

    /**
     * todo check exception
     */
    public void sendRequest(GeneratedMessageLite requset, int sid, int cid, Packetlistener packetlistener, Boolean isSendMsg) {
        //组装包头 header
        com.cooyet.im.protobuf.base.Header header = new DefaultHeader(sid, cid);
        int bodySize = requset.getSerializedSize();

        header.setLength(SysConstant.PROTOCOL_HEADER_LENGTH + bodySize);
        int seqNo = header.getSeqnum();
        listenerQueue.push(seqNo, packetlistener);

        boolean sendRes = IMUdtManager.instance().sendRequest(requset, header, isSendMsg);

    }


    public synchronized void packetDispatch(byte[] content) {

        byte[] contentReal = IMUdtManager.instance().getContentReal(content);
        DataBuffer buffer = new DataBuffer();
        buffer.setOrignalBuffer(ChannelBuffers.copiedBuffer(contentReal));
        CodedInputStream codedInputStream = CodedInputStream.newInstance(new ChannelBufferInputStream(buffer.getOrignalBuffer()));
//        CodedInputStream codedInputStream = CodedInputStream.newInstance(new ByteArrayInputStream(contentReal));
        Packetlistener listener = listenerQueue.pop(IMUdtManager.instance().getHeadSeqNo(content));
        if (listener != null) {
            listener.onSuccess(codedInputStream);
            return;
        }

        short serviceId = IMUdtManager.instance().getHeadServiceId(content);
        short commandId = IMUdtManager.instance().getHeadCommandId(content);

        logger.i("packetDispatch#from server serviceId:%d, commandId:%d", serviceId,
                commandId);

        // 抽象 父类执行
        switch (serviceId) {
            case IMBaseDefine.ServiceID.SID_LOGIN_VALUE:
                IMPacketDispatcher.loginPacketDispatcher(commandId, codedInputStream);
                break;
            case IMBaseDefine.ServiceID.SID_BUDDY_LIST_VALUE:
                IMPacketDispatcher.buddyPacketDispatcher(commandId, codedInputStream);
                break;
            case IMBaseDefine.ServiceID.SID_MSG_VALUE:
                IMPacketDispatcher.msgPacketDispatcher(commandId, codedInputStream);
                break;
            case IMBaseDefine.ServiceID.SID_GROUP_VALUE:
                IMPacketDispatcher.groupPacketDispatcher(commandId, codedInputStream);
                break;
            case IMBaseDefine.ServiceID.SID_TALK_VALUE:
                Boolean isFromPc = IMUdtManager.instance().isVoiceFromPc(content);
                IMPacketDispatcher.talkPacketDispatcher(commandId, codedInputStream, isFromPc);
                break;
            default:
                logger.e("packet#unhandled serviceId:%d, commandId:%d", serviceId,
                        commandId);
                break;
        }
    }


    /**
     * 新版本流程如下
     * 1.客户端通过域名获得login_server的地址
     * 2.客户端通过login_server获得msg_serv的地址
     * 3.客户端带着用户名密码对msg_serv进行登录
     * 4.msg_serv转给db_proxy进行认证（do not care on client）
     * 5.将认证结果返回给客户端
     */

    public void reqMsgServerAddrs() {
        logger.d("socket#reqMsgServerAddrs.");

        triggerEvent(SocketEvent.REQ_MSG_SERVER_ADDRS_SUCCESS);
        listenerQueue.onStart();
        triggerEvent(SocketEvent.CONNECT_MSG_SERVER_SUCCESS);
        connectMsgServer();//登录先连接到UDT server
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                IMLoginManager.instance().reqLoginMsgServer();
            }
        }, 500);
    }

    /**
     * 与登陆login是强耦合的关系
     */
    public void connectMsgServer() {

        triggerEvent(SocketEvent.CONNECTING_MSG_SERVER);

        int result = IMUdtManager.instance().startup();
        IMUdtManager.handle = IMUdtManager.instance().socket(String.valueOf(UrlConstant.PORT));
        int result2 = IMUdtManager.instance().connect(IMUdtManager.handle, UrlConstant.IP, UrlConstant.PORT);

        IMUdtManager.instance().initRecvThread();
        IMUdtManager.instance().initSendThread();

    }

    public void reconnectMsg() {
        synchronized (IMSocketManager.class) {
            disconnectMsgServer();
            connectMsgServer();
        }
    }

    /**
     * 断开与msg的链接
     */
    public void disconnectMsgServer() {
        listenerQueue.onDestory();
        IMUdtManager.instance().releaseUdt();
    }


//    public void onMsgServerConnected() {
//        logger.i("login#onMsgServerConnected");
//        listenerQueue.onStart();
//        triggerEvent(SocketEvent.CONNECT_MSG_SERVER_SUCCESS);
//        IMLoginManager.instance().reqLoginMsgServer();
//    }

    /**
     * 1. kickout 被踢出会触发这个状态   -- 不需要重连
     * 2. 心跳包没有收到 会触发这个状态   -- 链接断开，重连
     * 3. 链接主动断开                 -- 重连
     * 之前的长连接状态 connected
     */
    // 先断开链接
    // only 2 threads(ui thread, network thread) would request sending  packet
    // let the ui thread to close the connection
    // so if the ui thread has a sending task, no synchronization issue
    public void onMsgServerDisconn() {
        logger.w("login#onMsgServerDisconn");
//        disconnectMsgServer();
        triggerEvent(SocketEvent.MSG_SERVER_DISCONNECTED);
    }

    /**
     * ------------get/set----------------------------
     */
    private SocketEvent socketStatus = SocketEvent.NONE;


    public SocketEvent getSocketStatus() {
        return socketStatus;
    }

    public void setSocketStatus(SocketEvent socketStatus) {
        this.socketStatus = socketStatus;
    }

}
