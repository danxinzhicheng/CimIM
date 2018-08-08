package com.cooyet.im.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cooyet.im.R;
import com.cooyet.im.config.IntentConstant;
import com.cooyet.im.ui.activity.WebViewFragmentActivity;
import com.cooyet.im.ui.adapter.InternalAdapter;
import com.cooyet.im.ui.base.TTBaseFragment;

public class InternalFragment extends TTBaseFragment {
    private View curView = null;
    private ListView internalListView;
    private InternalAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.tt_fragment_internal,
                topContentView);

        initRes();
        mAdapter = new InternalAdapter(this.getActivity());
        internalListView.setAdapter(mAdapter);
        mAdapter.update();
        return curView;
    }

    private void initRes() {
        // 设置顶部标题栏
        setTopTitle(getActivity().getString(R.string.main_innernet));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void initHandler() {
    }

}
