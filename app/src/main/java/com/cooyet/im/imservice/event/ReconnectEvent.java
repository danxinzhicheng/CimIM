package com.cooyet.im.imservice.event;

/**
 * @author : ronghua.xie on 15-1-5.
 * @email : ronghua.xie@cooyet.com.
 *
 * 用户是否的登陆: 依赖loginManager的状态
 *   没有: 底层socket重连
 *   有: 底层socket重连，relogin
 */
public enum ReconnectEvent {
    NONE,

    SUCCESS,
    DISABLE
}
