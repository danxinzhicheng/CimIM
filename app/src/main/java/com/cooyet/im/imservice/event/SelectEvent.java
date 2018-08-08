package com.cooyet.im.imservice.event;

import com.cooyet.im.ui.adapter.album.ImageItem;

import java.util.List;

/**
 * @author : ronghua.xie on 15-1-16.
 * @email : ronghua.xie@cooyet.com.
 */
public class SelectEvent {
    private List<ImageItem> list;
    public SelectEvent(List<ImageItem> list){
        this.list = list;
    }

    public List<ImageItem> getList() {
        return list;
    }

    public void setList(List<ImageItem> list) {
        this.list = list;
    }
}
