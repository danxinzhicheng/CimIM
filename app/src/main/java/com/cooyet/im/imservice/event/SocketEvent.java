package com.cooyet.im.imservice.event;

/**
 * @author : ronghua.xie on 14-12-30.
 * @email : ronghua.xie@cooyet.com.
 *
 */
public enum SocketEvent {
    /**登陆之前的动作*/
    NONE,
    REQING_MSG_SERVER_ADDRS,
    REQ_MSG_SERVER_ADDRS_FAILED,
    REQ_MSG_SERVER_ADDRS_SUCCESS,

    /**请求登陆的过程*/
    CONNECTING_MSG_SERVER,
    CONNECT_MSG_SERVER_SUCCESS,
    CONNECT_MSG_SERVER_FAILED,
    MSG_SERVER_DISCONNECTED    //channel disconnect 会触发，再应用开启内，要重连【可能是服务端、客户端断掉】
}
