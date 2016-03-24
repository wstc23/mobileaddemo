package com.hzpd.view;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.VideoView;

import com.alibaba.fastjson.JSONObject;
import com.color.myxutils.BitmapUtils;
import com.color.myxutils.util.LogUtils;
import com.hzpd.bean3.UserBehavior;
import com.hzpd.http.AdSave;
import com.hzpd.library.AdSDK;
import com.hzpd.view.gif.GifView;

public class WelcomeAdLayout extends LinearLayout {
	private ImageView imgView;
	private GifView gifView;
	private WebView webView;
	private VideoView videoView;
	
	private Activity activity;
	
	private LayoutParams params;
	private DisplayMetrics dm;
	private TimerTask timerTask;
	
	public WelcomeAdLayout(Activity activity) {
		super(activity);
		activity.requestWindowFeature(Window.FEATURE_NO_TITLE);  //
		activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  
	              WindowManager.LayoutParams.FLAG_FULLSCREEN);  //
		params=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		this.setLayoutParams(params);
		this.activity=activity;
		dm=activity.getResources().getDisplayMetrics();
	
	}
	
	public ImageView getImgV(){
		return imgView;
	}
	
	
	//开屏图数据,传入;
	public void setKey(JSONObject ad,final String clazz) {
		int duration=3;
	
		if(AdSDK.getInstance().getStartPlaceId()==null||ad==null){
			addImageView();
			BitmapUtils bitmapUtils = new BitmapUtils(activity);
			bitmapUtils.display(imgView, "assets/img/ic_launcher-web.png");
		}else{
			String content=ad.getJSONObject("ad").getString("content");
			if("image".equals(content)){
				String img=ad.getJSONObject("ad").getString("image");
				String suffix[]=img.split("\\.");
				String suff=suffix[suffix.length-1];
				String fileName=AdSave.sdcardPath+AdSDK.getInstance().getStartPlaceId()+"."+suff;
				if("gif".equals(suff)){
					addGifView();
					AdSave.getGIF(activity, fileName, gifView);
				}else{//png jpg
					addImageView();
					AdSave.getPicture(activity, fileName, imgView);
				}
			}else if("html5".equals(content)){
				LogUtils.i("html5");
				addWebView();
				String html5=ad.getJSONObject("ad").getString("html5");
				
				String suffix[]=html5.split("\\.");
				String suff=suffix[suffix.length-1];
				String fileName=AdSave.sdcardPath+AdSDK.getInstance().getStartPlaceId()+"."+suff;
				AdSave.getHtml(activity, fileName, webView);
			}else if("video".equals(content)){
				LogUtils.i("video");
				addVideoView();
				
				String video=ad.getJSONObject("ad").getString("video");
				LogUtils.i("video-->"+video);
				Uri uri = Uri.parse(video);
				videoView.setVideoURI(uri);
				videoView.start();
				
//				String suffix[]=video.split("\\.");
//				String suff=suffix[suffix.length-1];
//				String fileName=AdSave.sdcardPath+AdSDK.getInstance().getStartPlaceId()+"."+suff;
//				AdSave.getVideo(activity, fileName, videoView);
			}else if("text".equals(content)){
				
			}
			
			duration=ad.getJSONObject("control").getIntValue("time_duration");
			//dshowed
			LogUtils.i("startad adshowed");
			UserBehavior ub=AdSDK.getInstance().getUserBehavior("adshowed", ad.getJSONObject("ad").getString("id"), AdSDK.getInstance().getStartPlaceId());
			AdSDK.getInstance().uploadUserBehavior(ub);
		}
		
		LogUtils.i("clazz-->"+clazz);
		if(null==timerTask){
		    timerTask=new TimerTask() {
				@Override
				public void run()  {
					Intent intent=new Intent();
					intent.setClassName(activity,clazz);
					activity.startActivity(intent);
					activity.finish();
				}
			};
			Timer timer=new Timer();
			timer.schedule(timerTask, duration*1000);
		}
		
	}
	
	private void addImageView(){
		imgView=new ImageView(activity);
		imgView.setLayoutParams(params);
		imgView.setScaleType(ScaleType.FIT_XY);
		this.addView(imgView);
//		imgView.setOnClickListener(new OnClickListener() {
//		@Override
//		public void onClick(View v) {
//		
//			onViewClick();
//		}
//	});
	}
	private void addGifView(){
		gifView=new GifView(activity);
		gifView.setMaxHeight(dm.heightPixels);
		gifView.setMaxWidth(dm.widthPixels);
		gifView.setMinimumHeight(dm.heightPixels);
		gifView.setMinimumWidth(dm.widthPixels);
		gifView.setLayoutParams(params);
		this.addView(gifView);
//		gifView.setOnClickListener(new OnClickListener() {
//		@Override
//		public void onClick(View v) {
//			onViewClick();
//		}
//		});
	}
	private void addWebView(){
		webView=new WebView(activity);
		webView.getSettings().setDefaultTextEncodingName("utf-8");
		webView.setLayoutParams(params);
		this.addView(webView);
	}
	private void addVideoView(){
		videoView=new VideoView(activity);
		videoView.setLayoutParams(params);
		this.addView(videoView);
	}
	
//	private void onViewClick(){
//		//
//		Ad ad=AdSave.getAd(getContext(), key);
//		UserBehavior ub=AdSDK.getInstance().getUserBehavior("adclick", ad.getAd().getId(), key);
//		AdSDK.getInstance().uploadUserBehavior(ub);
//	}
	
	
}
