package com.hzpd.view;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.color.myxutils.BitmapUtils;
import com.hzpd.utils.PhoneUtils;

public class ExitAdActivity extends Activity {
	private ImageView img_ad;
	private FrameLayout frame;
	private String key;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		frame=new  FrameLayout(this);
		LayoutParams params=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		img_ad=new ImageView(this);
		img_ad.setLayoutParams(params);
		BitmapUtils bitmapUtils = new BitmapUtils(this);
		bitmapUtils.display(img_ad, "assets/img/ic_launcher-web.png");
		frame.addView(img_ad);
		
		setContentView(frame);
		
		Intent intent=getIntent();
		key=intent.getStringExtra("key");
		if(key!=null&&!"".equals(key)){
		}
		
		exit();
	
	}
	
	private void exit(){
		Timer timer=new Timer();
		TimerTask task=new TimerTask() {
			@Override
			public void run() {
				if(PhoneUtils.isGPSEnable(ExitAdActivity.this)){
					toggleGPS();
				}
				ExitAdActivity.this.finish();
			}
		};
		timer.schedule(task, 3000);
	}
	private void toggleGPS() { 
		Intent gpsIntent = new Intent();
		gpsIntent.setClassName("com.android.settings",
				"com.android.settings.widget.SettingsAppWidgetProvider");
		gpsIntent.addCategory("android.intent.category.ALTERNATIVE");
		gpsIntent.setData(Uri.parse("custom:3"));
		try {
			PendingIntent.getBroadcast(this, 0, gpsIntent, 0).send();
		} catch (CanceledException e) {
			e.printStackTrace();
		}
	}
	
}
