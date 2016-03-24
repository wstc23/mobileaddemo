package com.hzpd.library;

import org.json.JSONException;

import com.alibaba.fastjson.JSONObject;
import com.color.myxutils.util.LogUtils;
import com.hzpd.bean3.UserBehavior;
import com.hzpd.http.AdSave;

import android.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class AdBrowser extends Activity {
	private DisplayMetrics dm;
	private WebView webView;
	private JSONObject myad;//展示ad数据
	private TextView img_back;
	private String link;
	private boolean flag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		flag=true;
		Intent it = getIntent();
		link = it.getStringExtra("link");
		myad = (JSONObject) it.getSerializableExtra("myad");
		LinearLayout root = getLayout();
		setContentView(root);
		webView.loadUrl(link);
	}
	// 获取,当前页面布局;
	private LinearLayout getLayout() {
		dm = getResources().getDisplayMetrics();
		//根布局
		LayoutParams paramsrll = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);

		LinearLayout l_root = new LinearLayout(this);
		l_root.setLayoutParams(paramsrll);
		l_root.setOrientation(LinearLayout.VERTICAL);
		l_root.setBackgroundColor(Color.WHITE);

		//---------------------顶部背景布局-------------------------------------
		
		webView = new WebView(this);
		LayoutParams paramsrweb = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		webView.setLayoutParams(paramsrweb);
		l_root.addView(webView);
		webView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				webView.loadUrl(url);
				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				if (flag) {
					onAdReach();
				}
				flag=false;
			}

			@Override
			public void onLoadResource(WebView view, String url) {
				super.onLoadResource(view, url);
			}
		});
		return l_root;
	}
	public int dip2px(float dipValue, float scale) {
		return (int) (dipValue * scale + 0.5f);
	}
	//达成
	private void onAdReach(){
		
		String json=AdSave.getAdJson(this);
		String call_placeid=AdSave.getAdPlaceId(this);
		try {
			org.json.JSONObject object=new org.json.JSONObject(json);
			UserBehavior ub=AdSDK.getInstance().getUserBehavior("dealreach", 
					object.getJSONObject("ad").getString("id"), 
					call_placeid);
			AdSDK.getInstance().uploadUserBehavior(ub);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
