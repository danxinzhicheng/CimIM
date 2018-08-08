package com.cooyet.im.imservice.manager.talk;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cooyet.im.DB.entity.MessageEntity;
import com.cooyet.im.DB.entity.PeerEntity;
import com.cooyet.im.DB.entity.UserEntity;
import com.cooyet.im.R;
import com.cooyet.im.imservice.entity.AudioMessage;
import com.cooyet.im.imservice.event.MessageTalkEvent;
import com.cooyet.im.imservice.manager.IMContactManager;
import com.cooyet.im.ui.helper.AudioPlayerHandler;
import com.cooyet.im.ui.widget.FloatingWindowView;
import com.cooyet.im.ui.widget.IMBaseImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.greenrobot.event.EventBus;


public class SpeechCallViewManager implements View.OnClickListener, SensorEventListener {

    private static String TAG = "cimtalk";
    private String ringName = "ring_call.mp3";
    private View mView = null;
    private WindowManager mWindowManager = null;
    private Context mContext = null;
    public Boolean isShown = false;
    private int mCurrentState = -1;

    public static int STATE_SEND_REQUEST = 0;
    public static int STATE_RECV_REQUEST = 1;
    public static int STATE_ME_SAY = 2;
    public static int STATE_OTHER_SAY = 3;

    private LinearLayout mStateSend, mStateRecv, mStateMeSay, mStateOtherSay;
    private ImageView ivSendCancle, ivRecvRefuse, ivRecvAceept, ivSayMuteVol, ivSayEnd, ivSaySpeechState;
    private IMBaseImageView ivAvatar;
    private TextView tvName, tvSateTip, tvDurion;
    private TextView tvSay, tvMute;
    private LayoutParams params;
    private PeerEntity otherUser;
    private UserEntity meUser;
    private AudioMessage msgEntity;

    private SensorManager sensorManager = null;
    private Sensor sensor = null;
    private static SpeechCallViewManager mInstance;

    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm:ss");

//    private Boolean isMute = false;

    public static SpeechCallViewManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SpeechCallViewManager(context);
        }
        return mInstance;
    }

    private SpeechCallViewManager(Context context) {
        initWm(context.getApplicationContext());
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            calendar.add(Calendar.SECOND, 1);
            String time = formatTime.format(calendar.getTime());
            tvDurion.setText(time);
            mHandler.sendEmptyMessageDelayed(0, 1000);
        }
    };

    public void initData(UserEntity meUser) {
        this.meUser = meUser;
        this.msgEntity = new AudioMessage();
        int nowTime = (int) (System.currentTimeMillis() / 1000);
        msgEntity.setFromId(meUser.getPeerId());
        msgEntity.setCreated(nowTime);
    }

    public void initData(UserEntity meUser, PeerEntity otherUser) {
        this.otherUser = otherUser;
        this.meUser = meUser;
        this.msgEntity = new AudioMessage();
        int nowTime = (int) (System.currentTimeMillis() / 1000);
        Log.i(TAG, "meUser.getPeerId:" + meUser.getPeerId());
        Log.i(TAG, "otherUser.getPeerId:" + otherUser.getPeerId());
        msgEntity.setFromId(meUser.getPeerId());
        msgEntity.setToId(otherUser.getPeerId());
        msgEntity.setCreated(nowTime);
    }

    private void initAudioSensor() {
        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void initWm(Context context) {
        // 获取应用的Context
        mContext = context.getApplicationContext();
        // 获取WindowManager
        mWindowManager = (WindowManager) mContext
                .getSystemService(Context.WINDOW_SERVICE);
        params = new LayoutParams();
        // 类型
        params.type = LayoutParams.TYPE_SYSTEM_ALERT;

        int flags = LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | LayoutParams.FLAG_FULLSCREEN;

        params.flags = flags;
        // 不设置这个弹出框的透明遮罩显示为黑色
        params.format = PixelFormat.TRANSLUCENT;
        params.width = LayoutParams.MATCH_PARENT;
        params.height = LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.TOP;

        mView = initView();
        PlayRingHelper.instance().init(mContext);
        initAudioSensor();
        EventBus.getDefault().register(this);
    }


    private View initView() {
        FloatingWindowView view = (FloatingWindowView) LayoutInflater.from(mContext).inflate(R.layout.tt_wm_speech_call_layout, null, false);
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                        || event.getKeyCode() == KeyEvent.KEYCODE_SETTINGS) {
                    hideWindow();
                    IMTalkManager.instance().sendTalkHungUpReq(msgEntity);
                }
                return false;
            }
        });

        ivAvatar = (IMBaseImageView) view.findViewById(R.id.call_avatar);
        tvName = (TextView) view.findViewById(R.id.call_name);

        tvSateTip = (TextView) view.findViewById(R.id.call_state_tip);
        tvDurion = (TextView) view.findViewById(R.id.call_durion);

        mStateSend = (LinearLayout) view.findViewById(R.id.ll_state_send);
        mStateRecv = (LinearLayout) view.findViewById(R.id.ll_state_recv);
        mStateMeSay = (LinearLayout) view.findViewById(R.id.ll_say_state);

        ivSendCancle = (ImageView) mStateSend.findViewById(R.id.call_send_cancel);
        ivSendCancle.setOnClickListener(this);

        ivRecvRefuse = (ImageView) mStateRecv.findViewById(R.id.call_recv_refuse);
        ivRecvAceept = (ImageView) mStateRecv.findViewById(R.id.call_recv_accept);
        ivRecvRefuse.setOnClickListener(this);
        ivRecvAceept.setOnClickListener(this);

        ivSayMuteVol = (ImageView) mStateMeSay.findViewById(R.id.call_say_mute);
        ivSaySpeechState = (ImageView) mStateMeSay.findViewById(R.id.call_say);
        ivSayEnd = (ImageView) mStateMeSay.findViewById(R.id.call_say_end);
        ivSayMuteVol.setOnClickListener(this);
        ivSaySpeechState.setOnClickListener(this);
        ivSayEnd.setOnClickListener(this);

        tvSay = (TextView) mStateMeSay.findViewById(R.id.tv_call_say);
        tvMute = (TextView) mStateMeSay.findViewById(R.id.tv_call_mute);

        ivSaySpeechState.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.i(TAG, "ACTION_DOWN");
                    AudioTalkPlayerHandler.getInstance().stopPlayer();
                    IMTalkManager.instance().sendTalkAllocReq(msgEntity);

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.i(TAG, "ACTION_UP");

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            IMTalkManager.instance().sendTalkReleaseReq(msgEntity);
                            mHandler.removeCallbacks(this);
                        }
                    }, 1000);
                    AudioTalkRecordHandler.getInstance().stopRecording();

                }

                return true;
            }
        });

        return view;

    }


    public void updateStateView(int state) {

        if (mView == null) {
            return;
        }
        if (mCurrentState == state) {
            return;
        }
        mCurrentState = state;
        if (state == STATE_SEND_REQUEST) {
            mStateSend.setVisibility(View.VISIBLE);
            mStateRecv.setVisibility(View.GONE);
            mStateMeSay.setVisibility(View.GONE);
            tvDurion.setVisibility(View.GONE);
            tvSateTip.setText("正在呼叫中...");
            //发送请求
            IMTalkManager.instance().sendTalkCallReq(msgEntity);

        } else if (state == STATE_RECV_REQUEST) {
            mStateSend.setVisibility(View.GONE);
            mStateRecv.setVisibility(View.VISIBLE);
            mStateMeSay.setVisibility(View.GONE);
            tvDurion.setVisibility(View.GONE);
            tvSateTip.setText("邀请你进行语音对讲...");
            PlayRingHelper.instance().init(mContext).play(ringName);

        } else if (state == STATE_ME_SAY) {
            mStateSend.setVisibility(View.GONE);
            mStateRecv.setVisibility(View.GONE);
            mStateMeSay.setVisibility(View.VISIBLE);
            tvDurion.setVisibility(View.VISIBLE);
            ivSaySpeechState.setImageResource(R.drawable.speech_call_say_big);
            ivSaySpeechState.setEnabled(true);
            ivSaySpeechState.setFocusable(true);
            ivSaySpeechState.requestFocusFromTouch();
            tvSateTip.setText("正在对讲中");
            tvSay.setText("按住讲话");
            PlayRingHelper.instance().init(mContext).stop();
            PlayRingHelper.instance().init(mContext).destroy();
            startTime();

        } else if (state == STATE_OTHER_SAY) {
            mStateSend.setVisibility(View.GONE);
            mStateRecv.setVisibility(View.GONE);
            mStateMeSay.setVisibility(View.VISIBLE);
            tvDurion.setVisibility(View.VISIBLE);
            ivSaySpeechState.setImageResource(R.drawable.speech_call_vol);
            ivSaySpeechState.setEnabled(false);
            ivSaySpeechState.setFocusable(false);
            tvSateTip.setText("正在对讲中");
            tvSay.setText("对方正在讲话");
            PlayRingHelper.instance().init(mContext).stop();
            PlayRingHelper.instance().init(mContext).destroy();
        }
    }

    private boolean isFirst = true;

    private void startTime() {
        if (isFirst) {
            cancelTime();
            mHandler.sendEmptyMessageDelayed(0, 1000);
            isFirst = false;
        }
    }

    private void cancelTime() {
        tvDurion.setText("");
        calendar.clear();
        isFirst = true;
        mHandler.removeMessages(0);
    }

    /**
     * 显示
     */
    public void showWindow(int state) {
        if (isShown) {
            return;
        }
        if (otherUser == null) {
            return;
        }

        isShown = true;
        ivAvatar.setImageUrl(otherUser.getAvatar());
        tvName.setText(otherUser.getMainName());
        updateStateView(state);
        mWindowManager.addView(mView, params);

        AudioTalkPlayerHandler.getInstance().initThread();
    }

    /**
     * 隐藏
     */
    public void hideWindow() {
        if (isShown && null != mView) {
            mWindowManager.removeView(mView);
            isShown = false;
            mCurrentState = -1;

            if (ivSayMuteVol.isActivated()) {
                tvMute.setText("静音");
                ivSayMuteVol.setActivated(false);
                ivSayMuteVol.setImageResource(R.drawable.speech_call_mute_vol_off);
                AudioTalkPlayerHandler.getInstance().silentSwitchOff(mContext);
            }

            cancelTime();

            AudioTalkPlayerHandler.getInstance().clearVoiceData();
            AudioTalkPlayerHandler.getInstance().destoryThread();
//            AudioTalkRecordHandler.getInstance().stopRecording();

            sensorManager.unregisterListener(this, sensor);

            PlayRingHelper.instance().stop();
            PlayRingHelper.instance().destroy();

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.call_send_cancel:
                hideWindow();
                IMTalkManager.instance().sendTalkHungUpReq(msgEntity);
                break;
            case R.id.call_recv_refuse:
                hideWindow();
                IMTalkManager.instance().sendTalkCallRes(msgEntity, false);
                break;
            case R.id.call_recv_accept:
                updateStateView(STATE_ME_SAY);
                IMTalkManager.instance().sendTalkCallRes(msgEntity, true);
                break;
            case R.id.call_say_mute:
                if (!ivSayMuteVol.isActivated()) {
                    AudioTalkPlayerHandler.getInstance().silentSwitchOn(mContext);
                    tvMute.setText("已静音");
                    ivSayMuteVol.setImageResource(R.drawable.speech_call_mute_vol);
                    ivSayMuteVol.setActivated(true);
                } else {
                    AudioTalkPlayerHandler.getInstance().silentSwitchOff(mContext);
                    tvMute.setText("静音");
                    ivSayMuteVol.setActivated(false);
                    ivSayMuteVol.setImageResource(R.drawable.speech_call_mute_vol_off);
                }
                break;
            case R.id.call_say:
                break;
            case R.id.call_say_end:
                hideWindow();
                IMTalkManager.instance().sendTalkHungUpReq(msgEntity);
                break;
        }

    }

    public void onEventMainThread(MessageTalkEvent event) {
        switch (event.getEvent()) {
            case NONE:
                break;
            case MSG_TALK_CALL_REQ:
                Log.i(TAG, "MSG_TALK_CALL_REQ");
                MessageEntity entity = event.getMessageEntity();
                otherUser = IMContactManager.instance().findContact(entity.getFromId());
                initData(meUser, otherUser);
                showWindow(STATE_RECV_REQUEST);
                break;
            case MSG_TALK_CALL_RES_FAILED:
                Log.i(TAG, "MSG_TALK_CALL_RES_FAILED");
                hideWindow();
                break;
            case MSG_TALK_CALL_RES_SUCCESS:
                Log.i(TAG, "MSG_TALK_CALL_RES_SUCCESS");
                updateStateView(STATE_ME_SAY);
                break;
            case MSG_TALK_CALL_RES_SERVICE_UNAVAILABLE:
                Log.i(TAG, "MSG_TALK_CALL_RES_SERVICE_UNAVAILABLE");
                hideWindow();
                break;
            case MSG_TALK_ALLOC_REQ:
                //判断自己是否正在说话 ,让对方是否可以语音

                boolean isRecording = AudioTalkRecordHandler.getInstance().isRecording();
                Log.i(TAG, "MSG_TALK_ALLOC_REQ===>isRecording:" + isRecording);
                if (isRecording) {
                    updateStateView(STATE_ME_SAY);
                    IMTalkManager.instance().sendTalkAllocRes(msgEntity, false);
                } else {
                    updateStateView(STATE_OTHER_SAY);
                    IMTalkManager.instance().sendTalkAllocRes(msgEntity, true);

                    AudioTalkPlayerHandler.getInstance().readyPlay();
                }
                break;
            case MSG_TALK_ALLOC_RES_FAILED:
                Log.i(TAG, "MSG_TALK_ALLOC_RES_FAILED");
                updateStateView(STATE_OTHER_SAY);
                break;
            case MSG_TALK_ALLOC_RES_SUCCESS:
                //开始发送语音
                Log.i(TAG, "MSG_TALK_ALLOC_RES_SUCCESS");
                updateStateView(STATE_ME_SAY);
                AudioTalkRecordHandler.getInstance().starRecording();
                break;
            case MSG_TALK_ALLOC_RES_SERVICE_UNAVAILABLE:
                Log.i(TAG, "MSG_TALK_ALLOC_RES_SERVICE_UNAVAILABLE");
                updateStateView(STATE_ME_SAY);
                break;
            case MSG_TALK_SEND_VOICE_BYTES:
                //发送语音
                byte[] content = event.getContent();
                Log.i(TAG, "MSG_TALK_SEND_VOICE_BYTES length:" + content.length);
//                Log.i(TAG, "MSG_TALK_SEND_VOICE_BYTES bytesToHexString:" + ByteUtils.bytesToHexString(content));
                AudioMessage audioMessage = new AudioMessage();
                audioMessage.setToId(otherUser.getPeerId());
                audioMessage.setFromId(meUser.getPeerId());
                IMTalkManager.instance().sendTalkVoiceReq(audioMessage, content);
                break;

            case MSG_TALK_VOICE_REQ:
                //收到语音，播放

                try {
                    byte[] result = event.getContent();
                    Log.i(TAG, "MSG_TALK_VOICE_REQ length:" + result.length);
//                    Log.i(TAG, "MSG_TALK_VOICE_REQ bytesToHexString:" + ByteUtils.bytesToHexString(result));
                    AudioTalkPlayerHandler.getInstance().addVoiceData(result);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                break;
            case MSG_TALK_VOICE_RES_FAILED:
                //重发
                Log.i(TAG, "MSG_TALK_VOICE_RES_FAILED");
                break;
            case MSG_TALK_VOICE_RES_SUCCESS:
                Log.i(TAG, "MSG_TALK_VOICE_RES_SUCCESS");
                break;
            case MSG_TALK_VOICE_RES_SERVICE_UNAVAILABLE:
                Log.i(TAG, "MSG_TALK_VOICE_RES_SERVICE_UNAVAILABLE");
                break;
            case MSG_TALK_RELEASE_REQ:
                Log.i(TAG, "MSG_TALK_RELEASE_REQ");
                updateStateView(STATE_ME_SAY);

                IMTalkManager.instance().sendTalkReleaseRes(msgEntity);//直接发送relase成功的消息
                AudioTalkPlayerHandler.getInstance().stopPlayer();

                break;
            case MSG_TALK_RELEASE_RES_FAILED:
                Log.i(TAG, "MSG_TALK_RELEASE_RES_FAILED");
                break;
            case MSG_TALK_RELEASE_RES_SUCCESS:
                Log.i(TAG, "MSG_TALK_RELEASE_RES_SUCCESS");
//                AudioTalkRecordHandler.getInstance().stopRecording();
                break;
            case MSG_TALK_RELEASE_RES_SERVICE_UNAVAILABLE:
                Log.i(TAG, "MSG_TALK_RELEASE_RES_SERVICE_UNAVAILABLE");
                break;
            case MSG_TALK_HUNG_UP_RES_FAILED:
                Log.i(TAG, "MSG_TALK_HUNG_UP_RES_FAILED");
                break;
            case MSG_TALK_HUNG_UP_RES_SUCCESS:
                Log.i(TAG, "MSG_TALK_HUNG_UP_RES_SUCCESS");
                break;
            case MSG_TALK_HUNG_UP_RES_SERVICE_UNAVAILABLE:
                Log.i(TAG, "MSG_TALK_HUNG_UP_RES_SERVICE_UNAVAILABLE");
                break;
            case MSG_TALK_HUNG_UP_REQ:
                Log.i(TAG, "MSG_TALK_HUNG_UP_REQ");
                hideWindow();
                IMTalkManager.instance().sendTalkHungUpRes(msgEntity);
                break;
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            if (!AudioPlayerHandler.getInstance().isPlaying()) {
                return;
            }
            float range = event.values[0];
            if (null != sensor && range == sensor.getMaximumRange()) {
                // 屏幕恢复亮度
                AudioPlayerHandler.getInstance().setAudioMode(AudioManager.MODE_NORMAL, mContext);
            } else {
                // 屏幕变黑
                AudioPlayerHandler.getInstance().setAudioMode(AudioManager.MODE_IN_CALL, mContext);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

//    private static final int MIN_DELAY_TIME = 1500;  // 两次点击间隔不能少于1000ms
//    private static long lastClickTime;
//
//    public static boolean isFastClick() {
//        boolean flag = true;
//        long currentClickTime = System.currentTimeMillis();
//        if ((currentClickTime - lastClickTime) >= MIN_DELAY_TIME) {
//            flag = false;
//        }
//        lastClickTime = currentClickTime;
//        return flag;
//    }

}
