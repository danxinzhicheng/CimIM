package com.cooyet.im.imservice.callback;

/**
 * @author : ronghua.xie on 15-1-7.
 * @email : ronghua.xie@cooyet.com.
 */
public interface IMListener<T> {
    public abstract void onSuccess(T response);

    public abstract void onFaild();

    public abstract void onTimeout();
}
