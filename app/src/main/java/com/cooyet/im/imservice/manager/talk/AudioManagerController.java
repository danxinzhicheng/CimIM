package com.cooyet.im.imservice.manager.talk;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

import static android.media.AudioManager.FLAG_SHOW_UI;

/**
 * Created by user on 2018/6/26.
 */

public class AudioManagerController {
    /**
     * 外放模式
     */
    public static final int MODE_SPEAKER = 0;
    /**
     * 耳机模式
     */
    public static final int MODE_HEADSET = 1;
    /**
     * 听筒模式
     */
    public static final int MODE_EARPIECE = 2;
    private static AudioManagerController playerManager;
    private AudioManager audioManager;
    private Context context;
    private int currentMode = MODE_SPEAKER;

    public static AudioManagerController getManager() {
        if (playerManager == null) {
            synchronized (AudioManagerController.class) {
                playerManager = new AudioManagerController();
            }
        }
        return playerManager;
    }

    public void init(Context context) {
        this.context = context;
        initAudioManager();
    }

    /**
     * 初始化音频管理器
     */
    private void initAudioManager() {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        } else {
            audioManager.setMode(AudioManager.MODE_IN_CALL);
        }
        audioManager.setSpeakerphoneOn(true); //默认为扬声器播放
    }

    /**
     * 获取当前播放模式
     *
     * @return
     */
    public int getCurrentMode() {
        return currentMode;
    }


    /**
     * 切换到听筒模式
     */
    public void changeToEarpieceMode() {
        currentMode = MODE_EARPIECE;
        audioManager.setSpeakerphoneOn(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.MODE_IN_COMMUNICATION), AudioManager.FX_KEY_CLICK);
        } else {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.MODE_IN_CALL), AudioManager.FX_KEY_CLICK);
        }
    }

    /**
     * 切换到耳机模式
     */
    public void changeToHeadsetMode() {
        currentMode = MODE_HEADSET;
        audioManager.setSpeakerphoneOn(false);
    }

    /**
     * 切换到外放模式
     */
    public void changeToSpeakerMode() {
        currentMode = MODE_SPEAKER;
        audioManager.setSpeakerphoneOn(true);
    }

    /**
     * 调大音量
     */
    public void raiseVolume() {
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume < audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FX_FOCUS_NAVIGATION_UP);
        }
    }

    /**
     * 调小音量
     */
    public void lowerVolume() {
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume > 0) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FX_FOCUS_NAVIGATION_UP);
        }
    }

    public void raiseVolume(Context context) {
        setVolume(context, true);

    }

    public void lowerVolume(Context context) {
        setVolume(context, false);
    }

    private void setVolume(Context context, boolean upVolume) {
        int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (upVolume) {
            volume += 1;
        } else {
            volume -= 1;
        }
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (volume >= 0 && volume <= maxVolume) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, FLAG_SHOW_UI);
        }
    }
}
