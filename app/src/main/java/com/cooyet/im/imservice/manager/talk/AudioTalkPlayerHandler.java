
package com.cooyet.im.imservice.manager.talk;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.cooyet.im.utils.CommonUtil;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class AudioTalkPlayerHandler {
    private AudioTrack audioTrack;
    private static final int frequency = 8000;
    private static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    private Handler mPlayHandler;
    private HandlerThread mPlayThread;
    private static final int byteSize = 8 * 1024;
    private BlockingQueue<byte[]> mCacheData = new LinkedBlockingDeque<>();

    public void clearVoiceData() {
        mCacheData.clear();
    }

    public void addVoiceData(byte[] bytes) throws InterruptedException {
        try {
            mCacheData.put(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] getVoiceData() throws InterruptedException {
        return mCacheData.take();
    }

    public void destoryThread() {
        mPlayThread.quit();
        mPlayHandler.removeCallbacksAndMessages(null);
        mPlayHandler = null;
        audioTrack = null;
    }

    public void initThread() {

//        int bufsize = AudioTrack.getMinBufferSize(frequency, AudioFormat.CHANNEL_CONFIGURATION_MONO,
//                audioEncoding);
//        playSize = buffsize * 4;

        mPlayThread = new HandlerThread("player-thread");
        mPlayThread.start();
        mPlayHandler = new Handler(mPlayThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {
                    try {
                        byte[] content = getVoiceData();
                        Log.i("danmo", "handleMessage:" + content.length);
                        short[] music = CommonUtil.toShortArray(content);
                        if (music != null) {
                            if (audioTrack != null) {
                                audioTrack.write(music, 0, content.length / 2);
                                mPlayHandler.sendEmptyMessage(0);
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                frequency, AudioFormat.CHANNEL_OUT_MONO,
                audioEncoding,
                byteSize,
                AudioTrack.MODE_STREAM);
        audioTrack.setStereoVolume(AudioTrack.getMaxVolume(),
                AudioTrack.getMaxVolume());
    }


    private static AudioTalkPlayerHandler instance = null;

    public static AudioTalkPlayerHandler getInstance() {
        if (null == instance) {
            synchronized (AudioTalkPlayerHandler.class) {
                instance = new AudioTalkPlayerHandler();
            }
        }
        return instance;
    }


    //语音播放的模式
//    public void setAudioMode(Context ctx) {
//        AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
//        } else {
//            audioManager.setMode(AudioManager.MODE_IN_CALL);
//        }
//        audioManager.setSpeakerphoneOn(true); //默认为扬声器播放
//    }


    private AudioTalkPlayerHandler() {
    }

    public void stopPlayer() {
        mPlayHandler.removeMessages(0);
        if (audioTrack != null) {
            audioTrack.stop();
        }
    }

    public void readyPlay() {
        audioTrack.play();
        mPlayHandler.sendEmptyMessage(0);
    }

    public void silentSwitchOn(Context ctx) {
        AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            Log.d("Silent", "RINGING 已被静音");
        }
    }

    public void silentSwitchOff(Context ctx) {
        AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            Log.d("Silent", "RINGING 取消静音");
        }
    }
}
