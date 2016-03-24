package com.hzpd.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;

import android.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;
import android.widget.VideoView;

import com.alibaba.fastjson.JSONObject;
import com.color.myxutils.BitmapUtils;
import com.color.myxutils.HttpUtils;
import com.color.myxutils.exception.HttpException;
import com.color.myxutils.http.ResponseInfo;
import com.color.myxutils.http.callback.RequestCallBack;
import com.color.myxutils.util.LogUtils;
import com.hzpd.bean3.UserBehavior;
import com.hzpd.http.AdSave;
import com.hzpd.library.AdBrowser;
import com.hzpd.library.AdSDK;
import com.hzpd.push.AdDetailActivity;
import com.hzpd.push.PushService;
import com.hzpd.utils.Code;
import com.hzpd.utils.PhoneUtils;
import com.hzpd.utils.VoiceThread;
import com.hzpd.view.gif.GifView;



public class PopScreenAD {
	private Dialog dialog;
	private ImageView refresh;//png、jpg
	
	private JSONObject data;//当前数据
    
//	private RelativeLayout rlAll;	//中间广告
	private final RelativeLayout relativeLayout;	//中间广告
	private ImageView closeView;
	
//	private PopupAnimation popAnim;
	
	private Context context;
	
	private DisplayMetrics dm;
	private int newWidth;
	private int newHeight;
	
	private boolean exitFlag;
	private Timer timer;

	private JSONObject myad;
	private String place_id;
	private VoiceThread vt;	 //声音
	//
	private String calledNnumber;
	private TelephonyManager telManager;
	
	private Handler handler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what==Code.musicComplete){
				LogUtils.i("musicComplete");
				onAdReach();
			}
		}
	};
	
	
	public PopScreenAD(Context context){
//		LogUtils.i("不停调用");
		
		this.context=context;
		dm=context.getResources().getDisplayMetrics();
		
		if(dm.widthPixels>dm.heightPixels){
			newWidth=(int) (dm.heightPixels*0.6);
			newHeight=(int) (dm.widthPixels*0.8);
			
		}else{
			newHeight=(int) (dm.heightPixels*0.8);
			newWidth=(int) (dm.widthPixels*0.6);
		}
		
		relativeLayout=new RelativeLayout(context);
		RelativeLayout.LayoutParams para=new RelativeLayout.LayoutParams(newWidth,newHeight);

		relativeLayout.setLayoutParams(para);
		
		relativeLayout.setBackgroundColor(context.getResources().getColor(R.color.white));
		
		
		dialog=new AlertDialog.Builder(context)
				.setView(relativeLayout)
	            .create();
		
		dialog.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK){  
			           dismiss();
			    }  
				return false;
			}
		});

		
		//TODO
		MySync sync=new MySync();
//		LogUtils.i("pop--->测试");
		place_id=AdSDK.getInstance().getPopPlaceId();
		sync.execute(place_id);
		
	}
	
	public boolean isShowing(){
		if(exitFlag){
			return true;
		}
		return dialog.isShowing();
	}
	
	private void setCloseButton(boolean flag,View img_close){
		if(!flag){
			img_close.setVisibility(View.GONE);
		}else{
			img_close.setVisibility(View.VISIBLE);
		}
	}

	public void show() {
		if(exitFlag){
			return;
		}
		exitFlag=true;
//		dialog.show();
	}
	
	private void postImg(final JSONObject data){
		this.data=data;
		if(data.getJSONObject("ad").getString("effect")!=null){
			relativeLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {//
					LogUtils.i("click event");
					String effect=data.getJSONObject("ad").getString("effect");
					if(effect!=null&&!"".equals(effect)){
						onViewClick();//点击事件
						
						String effectUrl=data.getJSONObject("ad").getString(effect);
						LogUtils.i("effect-url->"+effectUrl);
						if("call".equals(effect)){
							call(effectUrl);
						}else if("voice".equals(effect)){
							voice(effectUrl);
						}else if("download".equals(effect)){
							download(data.getJSONObject("ad"));
						}else if("link".equals(effect)){
							link(effectUrl);
						}else if("map".equals(effect)){
							map(effectUrl);
						}else if("video".equals(effect)){
							video(effectUrl);
						}
					}
				}
			});
		}
		
		
		if("video".equals(data.getJSONObject("ad").getString("content"))){
			LogUtils.i("video");
			VideoView videoView=addVideoView();
			closeView=addCloseImageView();
			setCloseButton(data.getJSONObject("ad").getBoolean("close_button"),closeView);
			LogUtils.i("video-url->"+data.getJSONObject("ad").getString("video"));
			Uri uri = Uri.parse(data.getJSONObject("ad").getString("video"));
			videoView.setVideoURI(uri);
			videoView.start();
			myad=data;
			onAdShowed();
			LogUtils.i("这里不停调用-->1");
		}else if("html5".equals(data.getJSONObject("ad").getString("content"))){
			LogUtils.i("html5");
			WebView webview=addWebView();
			closeView=addCloseImageView();
			setCloseButton(data.getJSONObject("ad").getBoolean("close_button"),closeView);
			
			webview.loadUrl(data.getJSONObject("ad").getString("html5"));
			myad=data;
			onAdShowed();
			LogUtils.i("这里不停调用--->2");
		}else if("text".equals(data.getJSONObject("ad").getString("content"))){
			LogUtils.i("text");
			
		}else if("image".equals(data.getJSONObject("ad").getString("content"))){
			
			String img=data.getJSONObject("ad").getString("image");
			String suffix[]=img.split("\\.");
			String suff=suffix[suffix.length-1];
			
			LogUtils.i("image-->"+suff);
			
			HttpUtils http = new HttpUtils();
			
			if("gif".equals(suff)){
				final String fileName=AdSave.sdcardPath+AdSDK.getInstance().getPopPlaceId()+".gif";
				http.download(data.getJSONObject("ad").getString("image"),
					fileName,
				    false, 
				    false, 
				    new RequestCallBack<File>() {
						@Override
						public void onSuccess(ResponseInfo<File> responseInfo) {
							LogUtils.i("download success");
							//
							final GifView gifview=addGifView();
							closeView=addCloseImageView();
							setCloseButton(data.getJSONObject("ad").getBoolean("close_button"),closeView);
							
							if(responseInfo.result!=null){
								try {
									LogUtils.i("newwidth-->"+newWidth);
									int w=data.getJSONObject("ad").getIntValue("width");
									int h=data.getJSONObject("ad").getIntValue("height");
									
//									float scaleWidth = ((float) newWidth) /(w>h?w:h) ;
//									int nh=(int)(scaleWidth*h);
//									int nw=(int)(scaleWidth*w);
//									LogUtils.i("scaleWidth-->"+scaleWidth+"   gifview scaleWidth-->"+nw+"    gifview height-->"+nh);
//									
//									gifview.setMaxHeight(newHeight);
//									gifview.setMinimumWidth(newWidth);
//									gifview.setMaxWidth(newWidth);
//									gifview.setMinimumHeight(newHeight);
									
									gifview.setGifImage(new FileInputStream(responseInfo.result));
									
									myad=data;
									LogUtils.i("这里不停调用-->3");
									onAdShowed();
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								}
							}
							LogUtils.i("Time_interval-->"+data.getJSONObject("control").getIntValue("time_interval"));
						}
						@Override
						public void onFailure(HttpException error, String msg) {
						}
				});
			}else{//png jpg
				final String fileName=AdSave.sdcardPath+AdSDK.getInstance().getPopPlaceId()+"."+suff;
			
				http.download(data.getJSONObject("ad").getString("image"),
					fileName,
				    false, 
				    false, 
				    new RequestCallBack<File>() {
						@Override
						public void onSuccess(ResponseInfo<File> responseInfo) {
							LogUtils.i("download success");
							//
							final ImageView imageView=addImageView();
							closeView=addCloseImageView();
							setCloseButton(data.getJSONObject("ad").getBoolean("close_button"),closeView);
							
							if(responseInfo.result!=null){
								try {
									LogUtils.i("imageView width-->"+imageView.getWidth()+"    imageView height-->"+imageView.getHeight());
									Bitmap bm=BitmapFactory.decodeStream(new FileInputStream(responseInfo.result));
									if(bm!=null){
										
										imageView.setImageBitmap(bm);
										myad=data;
										LogUtils.i("这里不停调用-->4");
										onAdShowed();
									}
									
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								}
							}
							LogUtils.i("Time_interval-->"+data.getJSONObject("control").getIntValue("time_interval"));
							
						}
						@Override
						public void onFailure(HttpException error, String msg) {
						}
				});
			}
		}
		
	}
	//TODO
	
	//处理数据;
	private void task(int interval){
		LogUtils.i("不停调用-->123");
		TimerTask timerTask=new TimerTask() {
			@Override
			public void run()  {
				MySync sync=new MySync();
				place_id=AdSDK.getInstance().getPopPlaceId();
				sync.execute(place_id);
			}
		};
		if(timer!=null){
			timer.cancel();
		}
		timer=new Timer();
		if(exitFlag){
			timer.schedule(timerTask, interval*1000);
		}else{
			timer.cancel();
		}
	}

	
	public void dismiss() {
		LogUtils.i("dismiss");
		exitFlag=false;
		if(isShowing()){
			dialog.dismiss();
		}
	}
	
	
	//effect效果-->加到Reletivilayout
	private void onViewClick(){
		if(myad==null) return;
		UserBehavior ub=AdSDK.getInstance().getUserBehavior("adclick", myad.getJSONObject("ad").getString("id"), place_id);
		AdSDK.getInstance().uploadUserBehavior(ub);
	}
	private void onAdShowed(){
		LogUtils.i("不停调用--->1234");
		//如果,弹框存在;
		if(exitFlag){
			dialog.show();
//			
//			task(data.getJSONObject("control").getIntValue("time_interval"));
			ScaleAnimation sa=new ScaleAnimation(0.0f, 1.0f
					, 0.0f, 1.0f
					, Animation.RELATIVE_TO_SELF, 0.5f
					, Animation.RELATIVE_TO_SELF, 0.5f);
			
			sa.setDuration(1000);
			sa.setFillAfter(true);
			sa.setStartOffset(1000);
			closeView.startAnimation(sa);
			
		}
		
		UserBehavior ub=AdSDK.getInstance().getUserBehavior("adshowed", myad.getJSONObject("ad").getString("id"), place_id);
		AdSDK.getInstance().uploadUserBehavior(ub);
	}
	private void onAdClosebutton(){
		PopScreenAD.this.dismiss();
		if(vt!=null){
			vt.stopVoice();
		}
		if(myad!=null){
			UserBehavior ub=AdSDK.getInstance().getUserBehavior("closebutton", myad.getJSONObject("ad").getString("id"), place_id);
			AdSDK.getInstance().uploadUserBehavior(ub);
		}
	}
	//
	private ImageView addImageView(){
		relativeLayout.removeAllViews();
		LayoutParams params=new LayoutParams(LayoutParams.MATCH_PARENT ,newHeight);
		ImageView imageView=new ImageView(context);
//		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		imageView.setLayoutParams(params);
		imageView.setScaleType(ScaleType.FIT_XY);
		relativeLayout.addView(imageView);

		return imageView;
	}
	private GifView addGifView(){
		relativeLayout.removeAllViews();
		LayoutParams paramss=new LayoutParams(LayoutParams.MATCH_PARENT,newHeight);
		GifView gifview=new GifView(context);
//		paramss.addRule(RelativeLayout.CENTER_IN_PARENT);
		gifview.setLayoutParams(paramss);
		gifview.setScaleType(ScaleType.FIT_XY);
		relativeLayout.addView(gifview);
	
		return gifview;
	}
	private VideoView addVideoView(){
		relativeLayout.removeAllViews();
		LayoutParams pas=new LayoutParams(LayoutParams.MATCH_PARENT, newHeight);
		VideoView videoView=new VideoView(context);
//		pas.addRule(RelativeLayout.CENTER_IN_PARENT);
		videoView.setLayoutParams(pas);
		videoView.setZOrderOnTop(true);
		relativeLayout.addView(videoView);
	
		return videoView;
	}
	private WebView addWebView(){
		relativeLayout.removeAllViews();
		android.view.ViewGroup.LayoutParams pa=new LayoutParams(LayoutParams.MATCH_PARENT,newHeight);
		WebView webview=new WebView(context);
		webview.setLayoutParams(pa);
		webview.getSettings().setDefaultTextEncodingName("utf-8");
		relativeLayout.addView(webview);
		
		return webview;
	}
	
	
	private ImageView addCloseImageView(){
		
		final ImageView img_close=new ImageView(context);
		int wh=PhoneUtils.dip(context, 30);
		LayoutParams params_im_cl=new LayoutParams(wh,wh);
		params_im_cl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		params_im_cl.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		
		img_close.setLayoutParams(params_im_cl);
		
		BitmapUtils bitmapUtils = new BitmapUtils(context);
		bitmapUtils.display(img_close, "assets/img/close.png");
		
		img_close.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onAdClosebutton();
			}
		});
//		rlAll.addView(img_close);

		relativeLayout.addView(img_close);
//		closeView.setVisibility(View.INVISIBLE);
		return img_close;
	}
	
	private ImageView addRefreshView(){
		relativeLayout.removeAllViews();
		LayoutParams params=new LayoutParams(newWidth,newHeight);
		refresh=new ImageView(context);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		refresh.setLayoutParams(params);
		refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onViewClick();
			}
		});
		
		relativeLayout.addView(refresh);
	
		return refresh;
	}
	
	class MySync extends AsyncTask<String,Integer, String>{
		@Override
		protected String doInBackground(String... params) {
			//---------------------------------------
			place_id=params[0];
			LogUtils.i("pop-place_id->"+place_id);
			
			StringBuilder sb=new StringBuilder();
			sb.append("place_id=");
			sb.append(place_id);
			sb.append("&");
			sb.append("network=");
			sb.append(PhoneUtils.getNetTypeName(context));
			
			HttpURLConnection connection = null;
	        URL url;
			StringBuilder result =new StringBuilder();
			try {
			  	url = new URL(Code.url);
	            connection = (HttpURLConnection) url.openConnection();
	            connection.setRequestMethod("POST");
	            connection.setDoInput(true);
	            connection.setDoOutput(true);
	            connection.setUseCaches(false);
	            connection.setConnectTimeout(3000);

	            connection.setRequestProperty("Content-Type",
	                    "application/x-www-form-urlencoded");

	            OutputStreamWriter osw = new OutputStreamWriter(
	                    connection.getOutputStream(), "UTF-8");
	            osw.write(sb.toString());
	            osw.flush();
	            osw.close();

	            BufferedReader br = new BufferedReader(new InputStreamReader(
	                    connection.getInputStream(), "UTF-8"));
	            String temp;
	            while ((temp = br.readLine()) != null) {
	                result.append(temp);
	            }
			       
			}catch (Exception e){
				e.printStackTrace();
			}finally{
				if (connection != null) {
	                connection.disconnect();
	            }
			}
			return result.toString();
		}

		@Override
		protected void onPostExecute(String result) {
			LogUtils.i("PopAdServer-ad->"+result);
			if(result==null||"".equals(result)){
				LogUtils.i("没有数据");
				return;
			}
	    	JSONObject jo=JSONObject.parseObject(result);
	    	if(jo.getIntValue("error_code")!=0){
				dismiss();
				LogUtils.i("没有插屏广告");
	    		return;
	    	}
	    	JSONObject data=jo.getJSONObject("data");
		    try{
		    	LogUtils.i("不停调用-->postimage");
		    	if(exitFlag){
		    		postImg(data);//
		    	}
		    }catch(Exception e){
		    	LogUtils.i("pop-postImg->挂了");
		    	return;
		    }
		    
		    
		}
	}

	//效果
	private void call(String telNum){
		calledNnumber=telNum;
		AdSave.setAdPlaceId(context, place_id);
		AdSave.setAdJson(context, myad.toJSONString());
		try {
			telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			telManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
			
			Intent intent=new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+telNum));
			context.startActivity(intent);
		
		} catch (Exception e) {
			Toast.makeText(context, "拨打电话被系统拦截", Toast.LENGTH_LONG).show();	
		}
	}
	private void voice(String voiceUrl){
		vt=new VoiceThread(handler, voiceUrl);
		vt.start();
	}

	private void link(String linkUrl){
//		Uri url = Uri.parse(linkUrl);  
//		Intent it = new Intent(Intent.ACTION_VIEW, url);  
//		context.startActivity(it);
		AdSave.setAdPlaceId(context, place_id);
		AdSave.setAdJson(context, myad.toJSONString());
		Intent it = new Intent();
		it.putExtra("link", linkUrl);
		it.setClass(context,AdBrowser.class);
		context.startActivity(it);

	}
	private void map(String linkUrl){
		Uri url = Uri.parse(linkUrl);  
		Intent it = new Intent(Intent.ACTION_VIEW, url);  
		context.startActivity(it);
	}
	private void video(String linkUrl){
		Uri url = Uri.parse(linkUrl);  
		Intent it = new Intent(Intent.ACTION_VIEW, url);  
		context.startActivity(it);
	}
	private void download(JSONObject data){
		JSONObject download=data.getJSONObject("download");
	
		if("ios".equals(download.getString("platform"))){
			return;
		}
		
		if("indirect".equals(download.getString("download_mode"))){
        	Intent intent =new Intent(context, AdDetailActivity.class); // 
        	intent.putExtra("object", data.toJSONString());
        	intent.putExtra("place_id", place_id);
        	
        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	intent.setAction(PushService.DetailAction);
        	context.startActivity(intent);
        	
        }else{
        	Intent service =new Intent(context, PushService.class); // 
        	service.putExtra("id", data.getInteger("id"));
        	service.putExtra("obj",data.toJSONString());
        	service.putExtra("place_id", place_id);
        	service.setAction(PushService.downloadAction);
        	context.startService(service);
        	
        }
	}
	//
	private void onAdReach(){
		if(myad==null) return;
		String json=AdSave.getAdJson(context);
		String call_placeid=AdSave.getAdPlaceId(context);
		try {
			org.json.JSONObject object=new org.json.JSONObject(json);
			UserBehavior ub=AdSDK.getInstance().getUserBehavior("dealreach", 
					object.getJSONObject("ad").getString("id"), 
					call_placeid);
			AdSDK.getInstance().uploadUserBehavior(ub);
			Toast.makeText(context, "upload!", Toast.LENGTH_LONG).show();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		
	}

	
	PhoneStateListener listener = new PhoneStateListener(){ 
	    @Override  
	    public void onCallStateChanged(int state, String incomingNumber) {
          switch (state){
            case TelephonyManager.CALL_STATE_IDLE: /* 无任何状态时 */
            	
            	if(AdSave.getCalled(context)){
            		LogUtils.i("IDLE");
            		 new Handler().postDelayed(new Runnable(){  
            		     public void run() {  
            		    	 if(PhoneUtils.getNumber(context, calledNnumber)){
                     			//上传dealreach
                     			LogUtils.i("上传dealreach");
                     			onAdReach();
                     			if(telManager!=null){//停止监听
                     				telManager.listen(listener, PhoneStateListener.LISTEN_NONE);
                     				telManager=null;
                     			}
                     			calledNnumber=null;
                     		}else{
                     			LogUtils.i("没有通话记录");
                     		}
                     		AdSave.setCalled(context, false);
            		     }  
            		  }, 1000); 
            		
            	}
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK: /* 接起电话时 */
            	LogUtils.i("OFFHOOK");
            	AdSave.setCalled(context, true);
            	Toast.makeText(context, "OFFHOOK!", Toast.LENGTH_LONG).show();
                break;  
            case TelephonyManager.CALL_STATE_RINGING: /* 电话进来时 */
            	LogUtils.i("RINGING");
                break;
            default:
            	break;
          }
	    super.onCallStateChanged(state, incomingNumber);
	    }           
	};
}
