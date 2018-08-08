package com.cooyet.im.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.cooyet.im.R;
import com.cooyet.im.config.IntentConstant;
import com.cooyet.im.ui.base.TTBaseFragmentActivity;
import com.cooyet.im.ui.fragment.WebviewFragment;

public class WebViewFragmentActivity extends TTBaseFragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent=getIntent();
		if (intent.hasExtra(IntentConstant.WEBVIEW_URL)) {
			WebviewFragment.setUrl(intent.getStringExtra(IntentConstant.WEBVIEW_URL));
		}
		setContentView(R.layout.tt_fragment_activity_webview);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
