package com.cooyet.im.imservice.manager.talk;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

/**
 * Created by user on 2018/6/14.
 */

public class PlayRingHelper {

    private static PlayRingHelper inst = new PlayRingHelper();

    public static PlayRingHelper instance() {
        return inst;
    }

    public PlayRingHelper() {
    }

    private Context mContext;
    private String filename;
    private MediaPlayer mediaPlayer;
    private int position;

    public void play(String filename) {
        this.filename = filename;
        try {
            AssetManager assetManager = mContext.getAssets();   ////获得该应用的AssetManager
            AssetFileDescriptor afd = assetManager.openFd(filename);   //根据文件名找到文件
            //对mediaPlayer进行实例化

            if (mediaPlayer.isPlaying()) {
                mediaPlayer.reset();    //如果正在播放，则重置为初始状态
            }
            mediaPlayer.setDataSource(afd.getFileDescriptor(),
                    afd.getStartOffset(), afd.getLength());     //设置资源目录
            mediaPlayer.prepare();//缓冲
            mediaPlayer.setLooping(true);//循环播放
            mediaPlayer.start();//开始或恢复播放
        } catch (IOException e) {
            Log.e("cimtalk", "没有找到assets这个文件");
            e.printStackTrace();
        }
    }

    public PlayRingHelper init(Context context) {
        this.mContext = context;
        if (mediaPlayer == null) {
            this.mediaPlayer = new MediaPlayer();
        }
        return this;
    }

    protected void stop() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                //保存当前播放点
                position = mediaPlayer.getCurrentPosition();
                mediaPlayer.stop();
            }
        }
    }

    //继续播放音乐
    protected void resume() {
        if (mediaPlayer != null) {
            if (position > 0 && filename != null) {
                try {
                    play(filename);
                    mediaPlayer.seekTo(position);
                    position = 0;
                } catch (Exception e) {
                    Log.e("mmtalk", e.toString());
                }
            }
        }
    }

    //退出时，对mediaPlayer进行回收
    protected void destroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
