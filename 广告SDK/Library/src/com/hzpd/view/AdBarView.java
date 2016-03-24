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

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;
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

public class AdBarView extends RelativeLayout {
	private JSONObject myad;//展示ad数据
	private String place_id;//本次展示placeid
	
	private Context context;//
	private int width;		//屏幕宽
	private int height;		//屏幕长
	private int newWidth;	//内容宽
	private boolean exitFlag;//是否结束
	private Timer timer;
	private boolean isPortrait=true;//竖屏f   横屏t
	private VoiceThread vt;	 //声音
	
	private int myAdHeight;//广告高度
	
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
	
	public AdBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	public AdBarView(Context context) {
		super(context);
		init(context);
	
	}
	
	private void init(Context context){
	
		this.context=context;
		DisplayMetrics dis=context.getResources().getDisplayMetrics();
		width=dis.widthPixels;
		height=dis.heightPixels;
		
		if(context.getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT){
			isPortrait=true;
		}else{
			isPortrait=false;
		}
		newWidth=width>height?height:width;
//		myAdHeight=PhoneUtils.dip(context, 50);
		
		RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		this.setLayoutParams(params);
		//banner高度
		ViewTreeObserver vto = this.getViewTreeObserver();  
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {  
            public boolean onPreDraw() {  
                int height = AdBarView.this.getMeasuredHeight();  
                int width = AdBarView.this.getMeasuredWidth();  
                LogUtils.i("\n^^^^^^"+height+",%%%%"+width);  
                myAdHeight = height;
                return true;  
            }  
        });  
	
        //
        this.setVisibility(View.GONE);
	}

	private ImageView addImageView(){
		this.removeAllViews();
		RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(newWidth,RelativeLayout.LayoutParams.MATCH_PARENT);
		ImageView imageView=new ImageView(context);
		imageView.setLayoutParams(params);
		imageView.setScaleType(ScaleType.FIT_XY);
		this.addView(imageView);
		return imageView;
	}
	private GifView addGifView(){
		this.removeAllViews();
		
		RelativeLayout.LayoutParams paramss=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		GifView gifview=new GifView(context);
		paramss.addRule(RelativeLayout.CENTER_IN_PARENT);
		gifview.setLayoutParams(paramss);
		gifview.setMaxHeight(myAdHeight);
		gifview.setMaxWidth(newWidth);
		gifview.setMinimumHeight(myAdHeight);
		gifview.setMinimumWidth(newWidth);
		
		this.addView(gifview);
		return gifview;
	}
	private VideoView addVideoView(){
		this.removeAllViews();
		RelativeLayout.LayoutParams pas=new RelativeLayout.LayoutParams(newWidth,myAdHeight);
		VideoView videoView=new VideoView(context);
		videoView.setLayoutParams(pas);
		
		this.addView(videoView);
		return videoView;
	}
	private WebView addWebView(){
		this.removeAllViews();
		RelativeLayout.LayoutParams pa=new RelativeLayout.LayoutParams(newWidth,myAdHeight);
		WebView webview=new WebView(context);
		webview.getSettings().setDefaultTextEncodingName("utf-8");
		webview.setLayoutParams(pa);
		this.addView(webview);
		return webview;
	}
	
	private ImageView addCloseImageView(){
		final ImageView img_close=new ImageView(context);
		int wh=PhoneUtils.dip(context, 25);
		RelativeLayout.LayoutParams params_im_cl=new RelativeLayout.LayoutParams(wh,wh);
		params_im_cl.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		params_im_cl.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		img_close.setLayoutParams(params_im_cl);
		
		BitmapUtils bitmapUtils = new BitmapUtils(context);
		bitmapUtils.display(img_close, "assets/img/close.png");
		
		img_close.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onAdClosebutton();
				stop();
				AdBarView.this.setVisibility(View.GONE);
			}
		});
		this.addView(img_close);
		return img_close;
	}
	
	private ImageView addTextImageView(){
		this.removeAllViews();
		RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		ImageView imageView=new ImageView(context);
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		imageView.setLayoutParams(params);
		imageView.setId(1);
		
		this.addView(imageView);
		return imageView;
	}

	private GifView addTextGifView(){
		this.removeAllViews();
		RelativeLayout.LayoutParams paramss=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT
				,RelativeLayout.LayoutParams.WRAP_CONTENT);
		GifView gifview=new GifView(context);
		paramss.addRule(RelativeLayout.CENTER_VERTICAL);
		gifview.setLayoutParams(paramss);
		gifview.setId(1);
		gifview.setMaxHeight(myAdHeight);
		gifview.setMinimumHeight(myAdHeight);
		gifview.setMaxWidth(myAdHeight);
		gifview.setMinimumWidth(myAdHeight);
		
		this.addView(gifview);
		return gifview;
	}
	
	private TextView addTextView(){
		final TextView textView=new TextView(context);
		textView.setTextSize(PhoneUtils.dip(context, 9));
		if(isPortrait){
			LayoutParams pa=new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			pa.addRule(RelativeLayout.RIGHT_OF,1);
			pa.addRule(RelativeLayout.CENTER_VERTICAL);
			this.addView(textView,pa);
			
		}else{
			FrameLayout.LayoutParams param=new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, myAdHeight);
			this.setLayoutParams(param);
				
			LayoutParams pa=new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			pa.addRule(RelativeLayout.RIGHT_OF,1);
			this.addView(textView, pa);
		}
		return textView;
	}
	
	public void start(){
		MySync sync=new MySync();
		place_id=AdSDK.getInstance().getBannerPlaceId();
		exitFlag=true;
		if(place_id!=null&&!"".equals(place_id)){
			sync.execute(place_id);
		}
	}
	
	private void start(long delay){
		TimerTask timerTask=new TimerTask() {
			@Override
			public void run() {
				MySync sync=new MySync();
				place_id=AdSDK.getInstance().getBannerPlaceId();
				if(place_id!=null&&!"".equals(place_id)){
					sync.execute(place_id);
				}
			}
		};
		if(timer!=null){
			timer.cancel();
		}
		timer=new Timer();
		if(exitFlag){
			timer.schedule(timerTask, delay*1000);
		}else{
			timer.cancel();
		}
			
	}
	//停止广告
	public void stop(){
		exitFlag=false;
		if(timer!=null){
			timer.cancel();
		}
		if(vt!=null){
			vt.stopVoice();
		}
	}
	
	
	//
	private void postImg(final JSONObject data){
		this.setVisibility(View.VISIBLE);
		if(data.getJSONObject("ad").getString("effect")!=null){
			this.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {//
					LogUtils.i("hello world!");
					String effect=data.getJSONObject("ad").getString("effect");
					if(effect!=null&&!"".equals(effect)){
						onViewClick();//点击事件
						
						String effectUrl=data.getJSONObject("ad").getString(effect);
					//无聊信息,分类处理;
						//打电话
						if("call".equals(effect)){
							call(effectUrl);
							//声音
						}else if("voice".equals(effect)){
							voice(effectUrl);
							//下载
						}else if("download".equals(effect)){
							download(data.getJSONObject("ad"));
							//打开连接
						}else if("link".equals(effect)){
							link(effectUrl);
							
						}else if("map".equals(effect)){
							map(effectUrl);
							//视频
						}else if("video".equals(effect)){
							video(effectUrl);
						}
					}
				}
			});
		}else{
			this.setOnClickListener(null);
		}
		
		if("video".equals(data.getJSONObject("ad").getString("content"))){
			LogUtils.i("video");
			VideoView videoView=addVideoView();
			ImageView closeView=addCloseImageView();
			setCloseButton(data.getJSONObject("ad").getBoolean("close_button"),closeView);
			
			Uri uri = Uri.parse(data.getJSONObject("ad").getString("video"));
			videoView.setVideoURI(uri);
			videoView.start();
			myad=data;
			onAdShowed();
		}else if("html5".equals(data.getJSONObject("ad").getString("content"))){
			LogUtils.i("html5");
			WebView webview=addWebView();
			ImageView closeView=addCloseImageView();
			setCloseButton(data.getJSONObject("ad").getBoolean("close_button"),closeView);
			
			webview.loadUrl(data.getJSONObject("ad").getString("html5"));
			myad=data;
			onAdShowed();
		}else if("text".equals(data.getJSONObject("ad").getString("content"))){
			LogUtils.i("text");
			
			
			String img=data.getJSONObject("ad").getString("text");
			LogUtils.i("text-img->"+img);
			String suffix[]=img.split("\\.");
			String suff=suffix[suffix.length-1];
			
			LogUtils.i("image-->"+suff);
			
			HttpUtils http = new HttpUtils();
			
			if("gif".equals(suff)){
				final String fileName=AdSave.sdcardPath+AdSDK.getInstance().getPopPlaceId()+".gif";
				http.download(data.getJSONObject("ad").getString("text"),
					fileName,
				    false, 
				    false, 
				    new RequestCallBack<File>() {
						@Override
						public void onSuccess(ResponseInfo<File> responseInfo) {
							LogUtils.i("download success");
							//
							final GifView gifview=addTextGifView();
							ImageView closeView=addCloseImageView();
							TextView textView=addTextView();
							textView.setText(data.getJSONObject("ad").getString("name"));
							setCloseButton(data.getJSONObject("ad").getBoolean("close_button"),closeView);
							
							if(responseInfo.result!=null){
								try {
									LogUtils.i("gifview width-->"+gifview.getWidth()+"    gifview height-->"+gifview.getHeight());
									gifview.setGifImage(new FileInputStream(responseInfo.result));
									myad=data;
									onAdShowed();
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								}
							}
							LogUtils.i("Time_interval-->"+data.getJSONObject("control").getIntValue("time_interval"));
						}
						@Override
						public void onFailure(HttpException error, String msg) {
							LogUtils.i("banner--text-gif-failure");
						}
				});
			}else{//png jpg
				final String fileName=AdSave.sdcardPath+AdSDK.getInstance().getPopPlaceId()+"."+suff;
				http.download(data.getJSONObject("ad").getString("text"),
						fileName,
					    false, 
					    false, 
					    new RequestCallBack<File>() {
							@Override
							public void onSuccess(ResponseInfo<File> responseInfo) {
								LogUtils.i("download success");
								
								ImageView imageView=addTextImageView();
								ImageView closeView=addCloseImageView();
								TextView textView=addTextView();
								textView.setText(data.getJSONObject("ad").getString("name"));
								
								setCloseButton(data.getJSONObject("ad").getBoolean("close_button"),closeView);
								
								if(responseInfo.result!=null){
									try {
										LogUtils.i("imageView width-->"+imageView.getWidth()+"    imageView height-->"+imageView.getHeight());
										Bitmap bm=BitmapFactory.decodeStream(new FileInputStream(responseInfo.result));
										if(bm!=null){
											int width = bm.getWidth();
											int height = bm.getHeight();
											float scaleHeight = ((float) myAdHeight) / height;
											Matrix matrix = new Matrix();
											matrix.postScale(scaleHeight, scaleHeight);
											bm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
											
											imageView.setImageBitmap(bm);
											myad=data;
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
								LogUtils.i("banner--text-pngjpg-failure");
							}
					});
			}
			
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
							GifView gifview=addGifView();
							ImageView closeView=addCloseImageView();
							setCloseButton(data.getJSONObject("ad").getBoolean("close_button"),closeView);
							
							if(responseInfo.result!=null){
								try {
									LogUtils.i("gifview width-->"+gifview.getWidth()+"    gifview height-->"+gifview.getHeight());
									gifview.setGifImage(new FileInputStream(responseInfo.result));
									myad=data;
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
								ImageView closeView=addCloseImageView();
								setCloseButton(data.getJSONObject("ad").getBoolean("close_button"),closeView);
								
								if(responseInfo.result!=null){
									try {
										LogUtils.i("imageView width-->"+imageView.getWidth()+"    imageView height-->"+imageView.getHeight());
										Bitmap bm=BitmapFactory.decodeStream(new FileInputStream(responseInfo.result));
										if(bm!=null){
											
											imageView.setImageBitmap(bm);
											myad=data;
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
		start(data.getJSONObject("control").getIntValue("time_interval"));
		
	}
	
	private void setCloseButton(boolean flag,View img_close){
		if(!flag){
			img_close.setVisibility(View.GONE);
		}else{
			img_close.setVisibility(View.VISIBLE);
		}
	}
	//拨打电话
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
	//打开连接;
	private void link(String linkUrl){
		AdSave.setAdPlaceId(context, place_id);
		AdSave.setAdJson(context, myad.toJSONString());
		Intent it = new Intent();
		it.putExtra("link", linkUrl);
		it.setClass(context,AdBrowser.class);
		context.startActivity(it);
//		try {
//			Uri url = Uri.parse(linkUrl);  
//			Intent it = new Intent(Intent.ACTION_VIEW, url);  
//			context.startActivity(it);
//		} catch (Exception e) {
//			e.printStackTrace();
//			LogUtils.i("linkUrl-->"+linkUrl);
//		}
	}
	//map
	private void map(String linkUrl){
		try {
			Uri url = Uri.parse(linkUrl);  
			Intent it = new Intent(Intent.ACTION_VIEW, url);  
			context.startActivity(it);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void video(String linkUrl){
		try {
			Uri url = Uri.parse(linkUrl);  
			Intent it = new Intent(Intent.ACTION_VIEW, url);  
			context.startActivity(it);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	//effect效果 加到this上
	
	//点击事件;
	private void onViewClick(){
		LogUtils.i("广告测试-->adclick");
		if(myad==null) return;
		UserBehavior ub=AdSDK.getInstance().getUserBehavior("adclick", myad.getJSONObject("ad").getString("id"), place_id);
		AdSDK.getInstance().uploadUserBehavior(ub);
	}
	//展示;
	//展示
	private void onAdShowed(){
		LogUtils.i("广告测试-->adshowed");
		UserBehavior ub=AdSDK.getInstance().getUserBehavior("adshowed", myad.getJSONObject("ad").getString("id"), place_id);
		AdSDK.getInstance().uploadUserBehavior(ub);
	}
	
	//关闭;
	private void onAdClosebutton(){
		LogUtils.i("广告测试-->closebutton");
		if(myad==null) return;
		UserBehavior ub=AdSDK.getInstance().getUserBehavior("closebutton", myad.getJSONObject("ad").getString("id"), place_id);
		AdSDK.getInstance().uploadUserBehavior(ub);
		
	}
	//达成
	private void onAdReach(){
		LogUtils.i("广告测试-->dealreach");
		if(myad==null) return;
		String json=AdSave.getAdJson(context);
		String call_placeid=AdSave.getAdPlaceId(context);
		
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

	class MySync extends AsyncTask<String,Integer, String>{
		@Override
		protected String doInBackground(String... params) {
			//---------------------------------------
			StringBuilder sb=new StringBuilder();
			place_id=params[0];
			LogUtils.i("banner-place_id->"+place_id);
			
			sb.append("place_id=");
			sb.append(place_id);
			sb.append("&");
			sb.append("network=");
			sb.append(PhoneUtils.getNetTypeName(context));
			
//			sb.append("&");
//			sb.append("style_type=");
//			sb.append("");
			
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
	            connection.setConnectTimeout(5000);

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
			       
			} catch (Exception e){
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
			LogUtils.i("Adbar-ad->"+result);
			if(result==null||"".equals(result)){
				LogUtils.i("没有数据");
				return;
			}
	    	JSONObject jo=JSONObject.parseObject(result);
	    	if(jo.getIntValue("error_code")!=0){
				LogUtils.i("没有inline广告");
	    		return;
	    	}
	    	JSONObject data=jo.getJSONObject("data");
		    try{
		    	if(exitFlag){
		    		
		    		//获取网络返回数据,发送出去;
		    		postImg(data);//
		    	}  
		    }catch(Exception e){
		    	LogUtils.i("adbarview-postImg-->挂了");
		    }
		}
	}

	PhoneStateListener listener = new PhoneStateListener(){ 
	    @Override  
	    public void onCallStateChanged(int state, String incomingNumber) {
          switch (state){
            case TelephonyManager.CALL_STATE_IDLE: /* 无任何状态时 */
            	LogUtils.i("IDLE");
            	if(AdSave.getCalled(context)){
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

