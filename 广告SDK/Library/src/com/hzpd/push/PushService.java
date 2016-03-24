package com.hzpd.push;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.color.myxutils.BitmapUtils;
import com.color.myxutils.HttpUtils;
import com.color.myxutils.exception.HttpException;
import com.color.myxutils.http.HttpHandler;
import com.color.myxutils.http.ResponseInfo;
import com.color.myxutils.http.HttpHandler.State;
import com.color.myxutils.http.callback.RequestCallBack;
import com.color.myxutils.util.LogUtils;
import com.hzpd.bean3.UserBehavior;
import com.hzpd.bean4.PushBean;
import com.hzpd.http.AdSave;
import com.hzpd.library.AdSDK;
import com.hzpd.utils.Code;
import com.hzpd.wall.WallActivity;

public class PushService extends Service {
	public static String DetailAction="com.lxy.push.detail";
	public static String StartAction="com.lxy.push.start";
	public static String downloadAction="com.lxy.push.download";
	public static String installAction="com.lxy.push.install";
	public static String deleteAction="com.lxy.deleteDownload";
	
	private List<String> pushPlaceId;
	private int index5=0;
	
	private TimerTask timeTask;//
	private Timer timer;		//
	private long period=1*60*1000;//轮询间隔
	private BitmapUtils bitmapUtis;
	
	private NotificationManager notificationManager;
	private Map<Integer, PushBean> pushbean;//存储推送广告
	private AppInstallReceiver  appInstalled;//安装广播
	private String packageName;		//安装包名
	private int ad_id;				//正在安装的广告id
	
	private boolean broadCastFlag=false;//是否注册广播
	
	private HashMap<Integer, HttpHandler> downloadMap;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		downloadMap=new HashMap<Integer, HttpHandler>();
		
		bitmapUtis=new BitmapUtils(this);
		bitmapUtis.configDiskCacheEnabled(true);
		notificationManager = (NotificationManager)    
	            this.getSystemService(android.content.Context.NOTIFICATION_SERVICE); 
		pushbean=new HashMap<Integer, PushBean>();

		appInstalled=new AppInstallReceiver();
		
		LogUtils.i("push-service-start");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent==null) return super.onStartCommand(intent, flags, startId);
		
		String action=intent.getAction();
		LogUtils.i("action--"+action);
		if(StartAction.equals(action)){
			pushPlaceId=intent.getStringArrayListExtra("pushPlaceId");
			getPush();
		}else if(downloadAction.equals(action)){
			int id=intent.getIntExtra("id", 0);
			PushBean pb=pushbean.get(id);
			
			if(pb==null){
				pb=new PushBean();
				String jstring=intent.getStringExtra("obj");
				String place_id=intent.getStringExtra("place_id");
				String score=intent.getStringExtra("score");
				LogUtils.i("score-->"+score);
				pb.setScore(score);
				
				JSONObject object=JSONObject.parseObject(jstring);
				pb.setObject(object);
				pb.setPlace_id(place_id);
				pushbean.put(id, pb);
				AdSDK.getInstance().addAd(id+"");//
			}
			
			JSONObject adobj=pb.getObject().getJSONObject(
					pushbean.get(id).getObject().getString("effect"));
			String url=adobj.getString("link");
			LogUtils.i("downloadurl-->"+url);
			
			//-----
			String is_deleteable=adobj.getString("is_deleteable");
			LogUtils.i("is_deleteable-->"+is_deleteable);
			
			if("1".equals(is_deleteable)){
				downloadApp(url, id, true);
			}else{
				downloadApp(url, id, false);
			}
			
		}else if(deleteAction.equals(action)){
			LogUtils.i("--deleteAction--");
			
			int notifyId=intent.getIntExtra("notifyId", -1);
			
			if(-1!=notifyId){
				LogUtils.i("notifyId-->"+notifyId);
				
				HttpHandler<String> httpHandler=downloadMap.get(notifyId);
				
				if(httpHandler!=null&&(httpHandler.getState()==State.STARTED
						||httpHandler.getState()==State.LOADING
						||httpHandler.getState()==State.WAITING)){
					LogUtils.i("stop download!!!");
					
					httpHandler.cancel();
					
					pushbean.remove(notifyId);
					downloadMap.remove(notifyId);
					
					notificationManager.cancel(notifyId);
					
					AdSDK.getInstance().removeAd(notifyId+"");
					
					Intent inte=new Intent();
					inte.setAction(WallActivity.LIST_UPDATE);
					sendBroadcast(inte);
					
				}else{
					LogUtils.i("unStop!!!");
				}
			}
		}else if(installAction.equals(action)){
			if(-1!=ad_id){
				if(packageName.equals(intent.getStringExtra("package"))){
					
					//installed 发送安装成功消息
					if(pushbean.get(ad_id).getObject()
							  .getJSONObject(pushbean.get(ad_id).getObject()
									  .getString("effect")).getString("require_action")
							  .equals("download")){
						  UserBehavior ub=AdSDK.getInstance().getUserBehavior("dealreach", 
								  pushbean.get(ad_id).getObject().getString("id")
								  , pushbean.get(ad_id).getPlace_id());
						  AdSDK.getInstance().uploadUserBehavior(ub);	
						  LogUtils.i("upload push installed!!");
					}
					pushbean.remove(ad_id);
					LogUtils.i("安装成功消息");
					packageName="";
					ad_id=-1;
					this.unregisterReceiver(appInstalled);
					broadCastFlag=false;
				}
			}
		}
		
		return START_REDELIVER_INTENT;
	}
	
	//定时获取推送
	private void getPush(){
		if(timer!=null){
			timer.cancel();
		}
		
		timer=new Timer();
		timeTask=new TimerTask() {
			@Override
			public void run() {
				//
				StringBuilder sb=new StringBuilder();
				String place_id=getPushId();
				if(place_id==null) return;
				
				sb.append("place_id=");
				sb.append(place_id);
				sb.append("&");
				sb.append("device_id=");
				sb.append(AdSave.getDeviceId(PushService.this));
				LogUtils.i("my_push-->"+sb);
				
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
		            LogUtils.i("-->"+sb.toString());
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
				LogUtils.i("pushService--->"+result.toString());
				try{
					JSONObject object=JSONObject.parseObject(result.toString());
					if(0==object.getIntValue("error_code")){
						JSONArray array=object.getJSONArray("data");
						if(array==null) return;
						for(int i=0;i<array.size();i++){
							JSONObject o=array.getJSONObject(i);
							JSONObject oo=o.getJSONObject("ad");
							PushBean pb=new PushBean();
							pb.setObject(oo);
							pb.setPlace_id(place_id);
							pushbean.put(oo.getInteger("id"), pb);
							//
							notifycation(oo,place_id);
						}
					}
				}catch(Exception e){
					LogUtils.i("getPush failed!");
				}
			}
		};
		
		timer.schedule(timeTask, 0, period);
		
	}
	
	//--获取图标
	//发送通知
	@SuppressLint("NewApi")
	private void notifycation(JSONObject obj,String place_id){
		
		LogUtils.e("notifycation-->"+obj.toJSONString());
		onAdShowed(obj, place_id);
		// 定义Notification的各种属性   
       
//        notification.flags |= Notification.FLAG_ONGOING_EVENT; 
        //叠加效果常量
        //notification.defaults=Notification.DEFAULT_LIGHTS|Notification.DEFAULT_SOUND;
        // 设置通知的事件消息   
        CharSequence contentTitle =obj.getString("title"); // 通知栏标题   
        CharSequence contentText =obj.getString("subtitle"); // 通知栏内容   
        PendingIntent contentItent;
        if("indirect".equals(obj.getJSONObject(obj.getString("effect")).getString("download_mode"))){
        	Intent intent =new Intent(this, AdDetailActivity.class); // 
        	intent.putExtra("object", obj.toJSONString());
        	intent.putExtra("place_id", place_id);
        	intent.setAction(DetailAction);
        	contentItent= PendingIntent.getActivity(this, 0, intent, obj.getIntValue("id"));
        }else{
        	Intent intent =new Intent(this, PushService.class); // 
        	intent.putExtra("id", obj.getIntValue("id"));
        	
        	intent.setAction(downloadAction);
        	contentItent= PendingIntent.getService(this, 0, intent, obj.getIntValue("id"));
        }
        Notification notification = new Notification.Builder(getApplicationContext())
        //title
        .setContentTitle(contentTitle)
        //text
        .setContentText(contentText)
        //small icon
        .setSmallIcon(android.R.drawable.stat_sys_download_done)
        
        .setContentIntent(contentItent)
        //big icon
        .build();
        notification.flags = Notification.FLAG_AUTO_CANCEL; 
        notification.defaults = Notification.DEFAULT_SOUND; 
        
       
        notificationManager.notify(obj.getIntValue("id"), notification);
        //
        pushbean.get(obj.getIntValue("id")).setNotifi(notification);
        
	}
	
	//下载app
	private void downloadApp(String url,final int notifyId,final boolean isDelete){
		
		if(url==null){
			return ;
		}else{
			if(!url.endsWith(".apk")){
				LogUtils.i("not .apk");
				return ;
			}
		}
		
		String path=AdSave.sdcardPath+"apk/myapk"+System.currentTimeMillis()+".apk";
		
		HttpUtils httpdownload=new HttpUtils();
		HttpHandler<File> myhandler =httpdownload.download(url
			, path
			, false
			, true
			, new RequestCallBack<File>() {
				int inotifyid=notifyId;
				@Override
				public void onCancelled() {
					super.onCancelled();
					LogUtils.i("download cancled");
				}
			  @Override
			  public void onStart() {
				  //
				  LogUtils.i("inotifyid-->"+inotifyid);
				  
				  notificationManager.cancel(inotifyid);
				  downloadNotification(inotifyid,isDelete);
//				  pushbean.get(inotifyid).getNotifiDownload().icon=android.R.drawable.stat_sys_download;
			  }
			  @Override
			  public void onLoading(long total, long current,
					boolean isUploading) {
				  //更新下载进度
				  LogUtils.i("total-->"+total+"   current-->"+current);
				  progressNotifycation(inotifyid,current*100/total,isDelete);
			  }
			  @Override
			  public void onSuccess(ResponseInfo<File> responseInfo) {
				  //点击安装
//				  pushbean.get(notifyId).setFilePah(responseInfo.result.getAbsolutePath());
				  downloadDone(notifyId);
				  AdSDK.getInstance().removeAd(""+notifyId);
				  //直接安装

				  onAdDownload(pushbean.get(inotifyid).getObject(), pushbean.get(inotifyid).getPlace_id());
			  
				  if("download".equals(pushbean.get(inotifyid).getObject()
						  .getJSONObject(pushbean.get(inotifyid).getObject()
								  .getString("effect")).getString("require_action"))){
					  onAdReach(pushbean.get(inotifyid).getObject(), pushbean.get(inotifyid).getPlace_id());
				  }
				  
				  notificationManager.cancel(inotifyid);//
				  downloadMap.remove(inotifyid);//
				//获取得分
				  
				String s=pushbean.get(inotifyid).getScore();
				
				if(s!=null){
					Intent br_intent=new Intent();
					br_intent.setAction(AdSDK.wallAction);
					br_intent.putExtra("score",s);
					sendBroadcast(br_intent);
					LogUtils.i("上传下载量---》"+inotifyid+"-->"+s);
				}
				  
//				detect(pushbean.get(notifyId).getObject().getJSONObject(
//						pushbean.get(notifyId).getObject().getString("effect"))
//						.getString("name"));
				installApk(responseInfo.result,inotifyid);
				  
			  }
			  
			  @Override
			  public void onFailure(HttpException error, String msg) {
				  notificationManager.cancel(notifyId);
				  pushbean.remove(notifyId);
			  }
			  
		});
		if(isDelete){
			downloadMap.put(notifyId, myhandler);
			LogUtils.i("--downloadApp put--"+notifyId);
		}
		
	}
	
	//下载app通知
	@SuppressLint("NewApi")
	private void downloadNotification(int notifyId,boolean isDelete){
		LogUtils.i("downloadNotification-->"+isDelete);
		
		// 定义Notification的各种属性   
//        notification.flags |= Notification.FLAG_AUTO_CANCEL; 
        
//        notification.defaults = Notification.DEFAULT_ALL; 
//        notification.icon=android.R.drawable.stat_sys_download;
//        notification.tickerText="开始下载《"+pushbean.get(notifyId).getObject()
//        		.getJSONObject(pushbean.get(notifyId).getObject().getString("effect"))
//        		.getString("name")+"》";
        // 设置通知的事件消息   
        CharSequence contentTitle ="正在下载《"+pushbean.get(notifyId).getObject()
        		.getJSONObject(pushbean.get(notifyId).getObject().getString("effect"))
        		.getString("name")+"》"; // 通知栏标题   
        CharSequence contentText ="已完成：0 %"; // 通知栏内容   
        Intent intent=new Intent(this,PushService.class);
        if(isDelete){
			intent.setAction(deleteAction);
			intent.putExtra("notifyId", notifyId);
			LogUtils.i("downloadNotification-->"+notifyId);
		}
        PendingIntent pendingIntent=PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        notification.setLatestEventInfo(this, contentTitle, contentText, pendingIntent);  
        Notification notification = new Notification.Builder(getApplicationContext())
        //title
        .setContentTitle(contentTitle)
        //text
        .setContentText(contentText)
        //small icon
        .setSmallIcon(android.R.drawable.stat_sys_download)
        .setContentIntent(pendingIntent)
        //big icon
        .build();
        notification.flags = Notification.FLAG_AUTO_CANCEL; 
        notification.defaults = Notification.DEFAULT_SOUND; 
        
        
        
        notificationManager.notify(notifyId, notification);
        
        pushbean.get(notifyId).setNotifiDownload(notification);
        
        
	}
	
	//进度更新通知
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private void progressNotifycation(int notifyId,float progress,boolean isDelete){
		
		PushBean pb=pushbean.get(notifyId);
		if(pb==null){
			return;
		}
		
//		Notification notification=pb.getNotifiDownload();
		
//        notification.icon=android.R.drawable.stat_sys_download;
//				pb.getNotifiDownload();
		//
		Intent intent=new Intent(this,PushService.class);
		
		if(isDelete){
			intent.setAction(deleteAction);
			intent.putExtra("notifyId", notifyId);
			LogUtils.i("----->"+notifyId);
		}
		
		
	    PendingIntent pendingIntent=PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//		notification.setLatestEventInfo(this, "正在下载《"+pushbean.get(notifyId).getObject()
//        		.getJSONObject(pushbean.get(notifyId).getObject().getString("effect"))
//        		.getString("name")+"》",  "已完成："+progress+" %", pendingIntent);
		
	    
		String contentTitle = "正在下载《"+pushbean.get(notifyId).getObject()
        		.getJSONObject(pushbean.get(notifyId).getObject().getString("effect"))
        		.getString("name")+"》";
		String contentText="已完成："+progress+" %";
		
		Notification notification = new Notification.Builder(getApplicationContext())
        //title
        .setContentTitle(contentTitle)
        //text
        .setContentText(contentText)
        //small icon
        .setSmallIcon(android.R.drawable.stat_sys_download)
        .setContentIntent(pendingIntent)
        //big icon
        .build();
        notification.flags = Notification.FLAG_AUTO_CANCEL; 
        notification.defaults = Notification.DEFAULT_SOUND; 
		notificationManager.notify(notifyId, notification);
	}
	
	//下载完成通知
	private void downloadDone(int notifyId){
		PushBean pb=pushbean.get(notifyId);
		if(pb==null){
			return;
		}
		Notification notification=pb.getNotifiDownload();
		notification.icon=android.R.drawable.stat_sys_download_done;
		//
		Intent intent=new Intent(this,PushService.class);
		intent.setAction(installAction);
		intent.putExtra("id", notifyId);
		PendingIntent pi=PendingIntent.getService(this, 0, intent, 0);
//		notification.setLatestEventInfo(this, "正在下载《"+pushbean.get(notifyId).getObject()
//        		.getJSONObject(pushbean.get(notifyId).getObject().getString("effect"))
//        		.getString("name")+"》",  "已完成：100 %", pi);
		notificationManager.notify(notifyId, notification);
		
		
	}
	//安装app
	private void installApk(File file,int notifyId){
		
	    PackageManager pm = getPackageManager();     
        PackageInfo info = pm.getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_ACTIVITIES);     
        if(info != null){     
            ApplicationInfo appInfo = info.applicationInfo;     
              
            packageName = appInfo.packageName;  //得到安装包名称   
            String version=info.versionName;       //得到版本信息     
            LogUtils.i("  packageName-->"+packageName+"  version-->"+version);
            
            if(broadCastFlag){
            	unregisterReceiver(appInstalled);
            }
            IntentFilter filter=new IntentFilter();
    		filter.addAction(Intent.ACTION_PACKAGE_ADDED);
    		filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
    		filter.addDataScheme("package");
    		this.registerReceiver(appInstalled, filter);
    		broadCastFlag=true;
    		LogUtils.i("registerReceiver");
    		ad_id=notifyId;
    		
        }     
		
		Intent intent = new Intent(Intent.ACTION_VIEW); 
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive"); 
		startActivity(intent);
		
	}
	
	@Override
	public void onDestroy() {
		if(broadCastFlag){
			unregisterReceiver(appInstalled);
		}
		bitmapUtis.clearDiskCache();
		super.onDestroy();
	}
	
	private String getPushId(){
		if (pushPlaceId != null&&pushPlaceId.size()>0) {
			String s = pushPlaceId.get(index5 % pushPlaceId.size());
			index5++;
			LogUtils.i("pushPlaceId-->" + s);
			return s;
		}
		return null;
	}

	
	//图展示
	private void onAdShowed(JSONObject myad,String place_id){
		UserBehavior ub=AdSDK.getInstance().getUserBehavior("adshowed", myad.getString("id"), place_id);
		AdSDK.getInstance().uploadUserBehavior(ub);
	}
	private void onAdDownload(JSONObject myad,String place_id){
		LogUtils.i("upload push downloadcomplete download!!");
		UserBehavior ub=AdSDK.getInstance().getUserBehavior("downloadcomplete", myad.getString("id"), place_id);
		AdSDK.getInstance().uploadUserBehavior(ub);
	}
	private void onAdReach(JSONObject myad,String place_id){
		LogUtils.i("upload push download!!");
		UserBehavior ub=AdSDK.getInstance().getUserBehavior("dealreach", 
				myad.getString("id"), 
				place_id);
		AdSDK.getInstance().uploadUserBehavior(ub);
	}

}
