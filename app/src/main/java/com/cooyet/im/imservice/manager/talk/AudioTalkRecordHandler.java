
package com.cooyet.im.imservice.manager.talk;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.cooyet.im.imservice.event.MessageTalkEvent;
import com.cooyet.im.utils.CommonUtil;
import com.cooyet.im.utils.Logger;

import de.greenrobot.event.EventBus;

import static com.cooyet.im.imservice.event.MessageTalkEvent.Event.MSG_TALK_SEND_VOICE_BYTES;

public class AudioTalkRecordHandler {

    private Logger logger = Logger.getLogger(AudioTalkRecordHandler.class);
    private boolean isRecording;
    private static final int frequency = 8000;
    private static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private static AudioRecord recordInstance = null;

    private static final int byteSize = 8 * 1024;
    private static final int sendShortSize = byteSize / 2;
    private Handler mRecordHandler;
    private HandlerThread mRecordThread;

    private static AudioTalkRecordHandler instance = null;

    public static AudioTalkRecordHandler getInstance() {
        if (null == instance) {
            synchronized (AudioTalkRecordHandler.class) {
                instance = new AudioTalkRecordHandler();
            }
        }
        return instance;
    }

    public AudioTalkRecordHandler() {
        initThread();
    }

    public void initThread() {

        if (null == recordInstance) {
            int bufferSize = AudioRecord.getMinBufferSize(frequency,
                    AudioFormat.CHANNEL_IN_MONO, audioEncoding);
            recordInstance = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    frequency, AudioFormat.CHANNEL_IN_MONO, audioEncoding,
                    bufferSize);
        }

        mRecordThread = new HandlerThread("record-thread");
        mRecordThread.start();
        mRecordHandler = new Handler(mRecordThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                logger.i("handleMessage");
                if (msg.what == 0) {
                    try {
                        logger.d("chat#audio#in audio thread");

                        android.os.Process
                                .setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                        int bufferSize = AudioRecord.getMinBufferSize(frequency,
                                AudioFormat.CHANNEL_IN_MONO, audioEncoding);
                        short[] buffer = new short[bufferSize];

                        short[] result = new short[sendShortSize];
                        int index = 0;

                        try {

//                            String filepath = CommonUtil.getAudioSavePath(1021);
//                            File file = new File(filepath);
//                            OutputStream os = new FileOutputStream(file);
//                            BufferedOutputStream bos = new BufferedOutputStream(os);
//                            DataOutputStream dos = new DataOutputStream(bos);

                            while (isRecording) {

                                int bufferRead = recordInstance.read(buffer, 0, bufferSize);

//                                for (int i = 0; i < bufferRead; i++) {
//                                    dos.writeShort(buffer[i]);
//                                }

                                //把读到的short数组赋值给result处理
                                for (int i = 0; i < bufferRead; i++) {

                                    if (index < sendShortSize - 1) {
                                        result[index] = buffer[i];
                                        index++;
                                    }
                                    //到达result最大长度，直接发送，然后初始化。
                                    else if (index >= sendShortSize - 1) {
                                        byte[] content = CommonUtil.toByteArray(result);
                                        triggerEvent(new MessageTalkEvent(MSG_TALK_SEND_VOICE_BYTES, content));//发送语音字节

//                                        result = null;
//                                        System.gc();

                                        result = new short[sendShortSize];
                                        index = 0;
                                    }
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            }
        };
    }

    public void starRecording() {
        if (recordInstance != null) {
            recordInstance.startRecording();
            isRecording = true;
            mRecordHandler.sendEmptyMessage(0);

        }
    }

    public void stopRecording() {
        if (recordInstance != null) {
            recordInstance.stop();
            isRecording = false;
            mRecordHandler.removeMessages(0);
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    /**
     * 自身的事件驱动
     *
     * @param event
     */
    public void triggerEvent(Object event) {
        EventBus.getDefault().post(event);
    }
}
