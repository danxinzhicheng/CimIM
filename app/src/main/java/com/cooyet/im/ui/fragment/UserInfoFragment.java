package com.cooyet.im.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cooyet.im.DB.entity.DepartmentEntity;
import com.cooyet.im.DB.entity.UserEntity;
import com.cooyet.im.R;
import com.cooyet.im.config.DBConstant;
import com.cooyet.im.config.IntentConstant;
import com.cooyet.im.imservice.event.UserInfoEvent;
import com.cooyet.im.imservice.manager.IMLoginManager;
import com.cooyet.im.imservice.manager.talk.SpeechCallViewManager;
import com.cooyet.im.imservice.service.IMService;
import com.cooyet.im.imservice.support.IMServiceConnector;
import com.cooyet.im.ui.widget.IMBaseImageView;
import com.cooyet.im.utils.IMUIHelper;

import java.util.ArrayList;

/**
 * 1.18 添加currentUser变量
 */
public class UserInfoFragment extends MainFragment {

    private View curView = null;
    private IMService imService;
    private UserEntity currentUser;
    private int currentUserId;
    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            logger.d("detail#onIMServiceConnected");

            imService = imServiceConnector.getIMService();
            if (imService == null) {
                logger.e("detail#imService is null");
                return;
            }

            currentUserId = getActivity().getIntent().getIntExtra(IntentConstant.KEY_PEERID, 0);
            Log.i("kkkk", "currentUserId:" + currentUserId);
            if (currentUserId == 0) {
                logger.e("detail#intent params error!!");
                return;
            }
            currentUser = imService.getContactManager().findContact(currentUserId);
            if (currentUser != null) {
                initBaseProfile();
                initDetailProfile();
            }
            ArrayList<Integer> userIds = new ArrayList<>(1);
            //just single type
            userIds.add(currentUserId);
            imService.getContactManager().reqGetDetaillUsers(userIds);
        }

        @Override
        public void onServiceDisconnected() {
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        imServiceConnector.disconnect(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        imServiceConnector.connect(getActivity());
        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.tt_fragment_user_detail, topContentView);
        super.init(curView);
        showProgressBar();
        initRes();
        return curView;
    }

    @Override
    public void onResume() {
        Intent intent = getActivity().getIntent();
        if (null != intent) {
            String fromPage = intent.getStringExtra(IntentConstant.USER_DETAIL_PARAM);
            setTopLeftText(fromPage);
        }
        super.onResume();
    }

    /**
     * @Description 初始化资源
     */
    private void initRes() {
        // 设置标题栏
        setTopTitle(getActivity().getString(R.string.page_user_detail));
        setTopLeftButton(R.drawable.tt_top_back);
        topLeftContainerLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                getActivity().finish();
            }
        });
        setTopLeftText(getResources().getString(R.string.top_left_back));
    }

    @Override
    protected void initHandler() {
    }

    public void onEventMainThread(UserInfoEvent event) {
        switch (event) {
            case USER_INFO_UPDATE:
                UserEntity entity = imService.getContactManager().findContact(currentUserId);
                if (entity != null && currentUser.equals(entity)) {
                    initBaseProfile();
                    initDetailProfile();
                }
                break;
        }
    }


    private void initBaseProfile() {

        logger.d("detail#initBaseProfile");
        IMBaseImageView portraitImageView = (IMBaseImageView) curView.findViewById(R.id.user_portrait);
        IMBaseImageView portraitOtherImageView = (IMBaseImageView) curView.findViewById(R.id.iv_other_nick_pic);

        setTextViewContent(R.id.nickName, currentUser.getMainName());
        setTextViewContent(R.id.userName, currentUser.getRealName());

        setTextViewContent(R.id.tv_other_nick_name, currentUser.getMainName());

        //头像设置
        portraitImageView.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
        portraitImageView.setCorner(60);
        portraitImageView.setImageResource(R.drawable.tt_default_user_portrait_corner);
        portraitImageView.setImageUrl(currentUser.getAvatar());

        //头像设置
        portraitOtherImageView.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
        portraitOtherImageView.setCorner(60);
        portraitOtherImageView.setImageResource(R.drawable.tt_default_user_portrait_corner);
        portraitOtherImageView.setImageUrl(currentUser.getAvatar());

//		portraitImageView.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				Intent intent = new Intent(getActivity(), DetailPortraitActivity.class);
//				intent.putExtra(IntentConstant.KEY_AVATAR_URL, currentUser.getAvatar());
//				intent.putExtra(IntentConstant.KEY_IS_IMAGE_CONTACT_AVATAR, true);
//
//				startActivity(intent);
//			}
//		});

        UserEntity loginUser = imService.getLoginManager().getLoginInfo();
        SpeechCallViewManager.getInstance(getContext()).initData(loginUser, currentUser);


        // 设置界面信息
        RelativeLayout rlOtherTopHead = (RelativeLayout) curView.findViewById(R.id.rl_other_top_head);
        RelativeLayout rlOtherBz = (RelativeLayout) curView.findViewById(R.id.rl_other_set_bz);

        RelativeLayout rlMeNickName = (RelativeLayout) curView.findViewById(R.id.rl_me_nickname);
        RelativeLayout rlMeAvatar = (RelativeLayout) curView.findViewById(R.id.rl_me_avatar);

        LinearLayout llSend = (LinearLayout) curView.findViewById(R.id.ll_send);

        if (currentUserId == imService.getLoginManager().getLoginId()) {
            rlOtherTopHead.setVisibility(View.GONE);
            rlOtherBz.setVisibility(View.GONE);
            rlMeAvatar.setVisibility(View.VISIBLE);
            rlMeNickName.setVisibility(View.VISIBLE);
            llSend.setVisibility(View.GONE);
//            chatBtn.setVisibility(View.GONE);
        } else {
            rlOtherTopHead.setVisibility(View.VISIBLE);
            rlOtherBz.setVisibility(View.VISIBLE);
            rlMeAvatar.setVisibility(View.GONE);
            rlMeNickName.setVisibility(View.GONE);
            llSend.setVisibility(View.VISIBLE);
//            chatBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View arg0) {
//                    IMUIHelper.openChatActivity(getActivity(), currentUser.getSessionKey());
//                    getActivity().finish();
//                }
//            });

            LinearLayout llSendMsg = (LinearLayout) llSend.findViewById(R.id.ll_send_msg);
            LinearLayout llSendSpeech = (LinearLayout) llSend.findViewById(R.id.ll_send_speech);
            LinearLayout llSendState = (LinearLayout) llSend.findViewById(R.id.ll_send_state);
            llSendMsg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IMUIHelper.openChatActivity(getActivity(), currentUser.getSessionKey());
//                    getActivity().finish();
                }
            });
            llSendSpeech.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SpeechCallViewManager.getInstance(getContext()).showWindow(SpeechCallViewManager.STATE_SEND_REQUEST);
                }
            });
            llSendState.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IMUIHelper.openStateFragment(getActivity());
                }
            });

        }
    }

    private void initDetailProfile() {
        logger.d("detail#initDetailProfile");
        hideProgressBar();
        DepartmentEntity deptEntity = imService.getContactManager().findDepartment(currentUser.getDepartmentId());
        setTextViewContent(R.id.department, deptEntity.getDepartName());
        setTextViewContent(R.id.telno, currentUser.getPhone());
        setTextViewContent(R.id.email, currentUser.getEmail());

        View phoneView = curView.findViewById(R.id.phoneArea);
        View emailView = curView.findViewById(R.id.emailArea);
        IMUIHelper.setViewTouchHightlighted(phoneView);
        IMUIHelper.setViewTouchHightlighted(emailView);

        emailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentUserId == IMLoginManager.instance().getLoginId())
                    return;
                IMUIHelper.showCustomDialog(getActivity(), View.GONE, String.format(getString(R.string.confirm_send_email), currentUser.getEmail()), new IMUIHelper.dialogCallback() {
                    @Override
                    public void callback() {
                        Intent data = new Intent(Intent.ACTION_SENDTO);
                        data.setData(Uri.parse("mailto:" + currentUser.getEmail()));
                        data.putExtra(Intent.EXTRA_SUBJECT, "");
                        data.putExtra(Intent.EXTRA_TEXT, "");
                        startActivity(data);
                    }
                });
            }
        });

        phoneView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUserId == IMLoginManager.instance().getLoginId())
                    return;
                IMUIHelper.showCustomDialog(getActivity(), View.GONE, String.format(getString(R.string.confirm_dial), currentUser.getPhone()), new IMUIHelper.dialogCallback() {
                    @Override
                    public void callback() {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                IMUIHelper.callPhone(getActivity(), currentUser.getPhone());
                            }
                        }, 0);
                    }
                });
            }
        });

        setSex(currentUser.getGender());
    }

    private void setTextViewContent(int id, String content) {
        TextView textView = (TextView) curView.findViewById(id);
        if (textView == null) {
            return;
        }

        textView.setText(content);
    }

    private void setSex(int sex) {
        if (curView == null) {
            return;
        }

        TextView sexTextView = (TextView) curView.findViewById(R.id.sex);
        if (sexTextView == null) {
            return;
        }

        int textColor = Color.rgb(255, 138, 168); //xiaoxian
        String text = getString(R.string.sex_female_name);

        if (sex == DBConstant.SEX_MAILE) {
            textColor = Color.rgb(144, 203, 1);
            text = getString(R.string.sex_male_name);
        }

        sexTextView.setVisibility(View.VISIBLE);
        sexTextView.setText(text);
        sexTextView.setTextColor(textColor);
    }

}
