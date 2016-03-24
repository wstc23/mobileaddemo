package com.hzpd.library;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.http.HttpEntity;

import com.alibaba.fastjson.JSONObject;
import com.color.myxutils.DbUtils;
import com.color.myxutils.HttpUtils;
import com.color.myxutils.exception.DbException;
import com.color.myxutils.exception.HttpException;
import com.color.myxutils.http.RequestParams;
import com.color.myxutils.http.ResponseInfo;
import com.color.myxutils.http.callback.RequestCallBack;
import com.color.myxutils.http.client.HttpRequest;
import com.color.myxutils.http.client.HttpRequest.HttpMethod;
import com.color.myxutils.util.LogUtils;
import com.hzpd.bean3.DeviceMessage;
import com.hzpd.bean3.UserBehavior;
import com.hzpd.http.AdSave;
import com.hzpd.http.StartAdServer;
import com.hzpd.push.PushService;
import com.hzpd.utils.Code;
import com.hzpd.view.AdBarView;
import com.hzpd.view.ExitAdActivity;
import com.hzpd.view.PopScreenAD;
import com.hzpd.view.WelcomeAdLayout;
import com.hzpd.wall.WallActivity;
import com.hzpd.wall.WallCallBack;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

public class AdSDK {
	public static final String wallAction = "com.lxy.wallAction";
	private static AdSDK instance = null;

	private Context context;
	private DeviceMessage dm;
	private String app_id;

	private LocationManager locationManager = null;
	private MyLocListener loclistener;

	private String location_lat = "";
	private String location_lng = "";
	private String location_type = "";

	private DbUtils db;
	private List<UserBehavior> list;

	private PopScreenAD pop;

	private List<String> bannerPlaceId;
	private List<String> popPlaceId;
	private List<String> startPlaceId;//开屏id
	private List<String> wallPlaceId;
	private List<String> pushPlaceId;

	private int index1 = 0;
	private int index2 = 0;
	private int index3 = 0;
	private int index4 = 0;
	private int index5 = 0;

	private boolean isFinished = false;
	// ----
	private WelcomeAdLayout layout;
	private String activity;
	//
	private BroadcastReceiver receiver;

	private HashSet<String> adSet;

	public Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			JSONObject jo = JSONObject.parseObject((String) msg.obj);
			
			switch (msg.what) {
			case Code.behaviorSucess: {
				// UserBehavior-->DeviceMessage
				if (4012 == jo.getIntValue("error_code")) {
					uploadDevicemeta();
				} else {
					LogUtils.i("error_msg-->" + jo.getString("error_msg"));
				}
			}
				break;
			case Code.pushid: {
				if (pushPlaceId != null && pushPlaceId.size() > 0) {

					LogUtils.i("pushPlaceId.size()-->" + pushPlaceId.size());
					LogUtils.i("deviceId-->" + AdSave.getDeviceId(context));
					Intent intent = new Intent(context, PushService.class);
					intent.putStringArrayListExtra("pushPlaceId", (ArrayList<String>) pushPlaceId);
					intent.setAction(PushService.StartAction);
					context.startService(intent);
				}
			}
				break;
				//开平图;
			case Code.pl: {
				//
				if (AdSave.getDeviceId(context) == null) {
					uploadDevicemeta();
					LogUtils.i("regetdeviceid---");
				} else {
					if (pushPlaceId != null && pushPlaceId.size() > 0) {
						LogUtils.i("pushPlaceId.size()-->" + pushPlaceId.size());
						Intent intent = new Intent(context, PushService.class);
						intent.putStringArrayListExtra("pushPlaceId", (ArrayList<String>) pushPlaceId);
						intent.setAction(PushService.StartAction);
						context.startService(intent);
					}
				}
				if (isFinished) {
					JSONObject ad = AdSave.getAd(context, getStartPlaceId());
					if (ad != null) {
						LogUtils.i("开屏ad-->1" + ad.toJSONString());
						layout.setKey(ad, activity);
					} else {
						layout.setKey(null, activity);
					}
					StartAdServer server = new StartAdServer(context, getStartNewPlaceId());
					server.start();
				}

			}
				break;
			case Code.location:{
				LogUtils.i("jo--->"+jo.toJSONString());
				location_lng = jo.getString("lng");
				location_lat = jo.getString("lat");
				location_type = "GPS";
				LogUtils.i("location_lng-->"+location_lng+"  location_lat-->"+location_lat);
			}break;
			default:
				LogUtils.i("error_msg-->" + jo.toJSONString());
				break;
			}
		}
	};

	private AdSDK(final Context context, String app_id) {

		//初始化,数据,设置,app_id
		this.context = context;
		this.app_id = app_id;

		File f = new File(AdSave.sdcardPath);
		if (!f.exists()) {
			f.mkdirs();
		}
		// 缓存数据库
		db = DbUtils.create(context);

		getPlaceId();
		//上传用户行为,打开,显示;
		onStart();
		//设备信息,类
		dm = new DeviceMessage(context);
		LogUtils.i("dm-->" + dm.toString());
		//
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		openGPS(context);

		// 查找到服务信息
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度
		criteria.setAltitudeRequired(false); // 设置是否需要海拔信息
		criteria.setSpeedRequired(false); // 设置是否要求速度
		criteria.setBearingRequired(false); // 设置是否需要方位信息
		criteria.setCostAllowed(true); // 设置是否允许运营商收费
		criteria.setPowerRequirement(Criteria.POWER_HIGH);// 设置对电源的需求

		String provider = locationManager.getBestProvider(criteria, true); // 获取GPS信息
		Location location = null;
		if(provider != null){
			location = locationManager.getLastKnownLocation(provider); // 通过GPS获取位置
			loclistener=new MyLocListener();
			locationManager.requestLocationUpdates(provider, 10 * 1000, 50, loclistener);
		}
		if(null!=location){
			if (TextUtils.isEmpty(location.getLongitude() + "") 
					&& TextUtils.isEmpty(location.getLatitude() + "")) {
				location_lng = location.getLongitude() + "";
				location_lat = location.getLatitude() + "";
			}
		}
		
		adSet = new HashSet<String>();
	}
	//提前,数据,准备好;
	// 开屏.广告启用;
	public void startAD(WelcomeAdLayout layout, String activity) {
		if (!isConnect(context)) {
			//没网,清空数据,退出
			layout.setKey(null, activity);
			return;
		}
		if (startPlaceId == null) {
			this.layout = layout;
			this.activity = activity;
			isFinished = true;
			LogUtils.i("startPlaceId null");
			layout.setKey(null, activity);
		} else {
			JSONObject ad = AdSave.getAd(context, getStartPlaceId());
			if (ad != null) {
				LogUtils.i("开屏ad-->2" + ad.toJSONString());
				layout.setKey(ad, activity);
			} else {
				//ad==null;
				LogUtils.i("开屏startPlaceId null");
				layout.setKey(null, activity);
			}
			//开启服务,从新获取,开屏图; 
			StartAdServer server = new StartAdServer(context, getStartNewPlaceId());
			server.start();
		}
	}

	// 横幅,inline广告;处理;
	public void bannerAD(AdBarView view) {
		if (!isConnect(context)) {
			//先隐藏数据;
			view.setVisibility(View.GONE);
			return;
		}
		view.start();
	}

	// 插屏
	public void popScreenAD(Activity activity) {
		if (pop != null) {
			if (pop.isShowing()) {
				pop.dismiss();
			}
		}
		if (isConnect(activity)) {
			pop = new PopScreenAD(activity);
			pop.show();
		}
	}

	// 退屏
	private void exitAD(Activity activity, String key) {
		Intent intent = new Intent(activity, ExitAdActivity.class);
		intent.putExtra("key", key);
		activity.startActivity(intent);
	}

	// 积分墙
	public void wall(Context context, final WallCallBack callback) {

		String wall_placeId = getWallPlaceId();
		if (wall_placeId == null) {
			LogUtils.i("wall_placeId==null");
			return;
		}

		if (receiver == null) {
			receiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {

					String score = intent.getStringExtra("score");
					LogUtils.i("score-->" + score);
					callback.myWallcallback(score);
				}
			};
			IntentFilter filter = new IntentFilter();
			filter.addAction(wallAction);
			context.registerReceiver(receiver, filter);
		}

		Intent intent = new Intent(context, com.hzpd.wall.WallActivity.class);
		intent.putExtra("place_id", wall_placeId);
		context.startActivity(intent);
	}

	public static synchronized AdSDK getInstance() {
		return instance;
	}

	
	/**
	 * 程序入口;
	 * @param context
	 * @param app_id 传入的应用id;
	 * @return
	 */
	public static synchronized boolean init(Context context, String app_id) {
		//创建sdk实例;
		if (instance == null) {
			if (context != null) {
				instance = new AdSDK(context, app_id);
				return true;
			}
		}

		File f2 = new File(AdSave.sdcardPath + "scorebg.png");
		HttpUtils httpUtils = new HttpUtils();
		File f = new File(AdSave.sdcardPath + "back.png");
		if (!f.exists()) {
			httpUtils.download(WallActivity.img_back_URL, AdSave.sdcardPath + "back.png", new RequestCallBack<File>() {
				@Override
				public void onSuccess(ResponseInfo<File> responseInfo) {
				}

				@Override
				public void onFailure(HttpException error, String msg) {
				}
			});
		}
		if (!f2.exists()) {
			httpUtils.download(WallActivity.scorebg_URL, AdSave.sdcardPath + "scorebg.png",
					new RequestCallBack<File>() {
						@Override
						public void onSuccess(ResponseInfo<File> responseInfo) {

						}

						@Override
						public void onFailure(HttpException error, String msg) {

						}
					});
		}
		return false;
	}

	public String getApp_id() {
		return app_id;
	}

	public void setApp_id(String app_id) {
		this.app_id = app_id;
	}

	public Context getContext() {
		return context;
	}

	//
	private boolean isWIFI() {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			return true;
		}
		return false;
	}

	// 是否有网络连接
	private boolean isConnect(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}
	// 上传用户行为
	public void uploadUserBehavior(final UserBehavior ub) {
		LogUtils.i("uploadeub-->" + ub.toString());
		LogUtils.i("uploadeub-behavior->" + ub.getBehavior_type());
		if (isConnect(context)) {
			if (list == null) {
				list = new ArrayList<UserBehavior>();
			}
			list.clear();
			list.add(ub);
			HttpUtils http = new HttpUtils();
			RequestParams params = new RequestParams();
			params.addBodyParameter("data_type", "user_behavior");
			for (int i = 0; i < list.size(); i++) {
				UserBehavior userb = list.get(i);
				//
				params.addBodyParameter("data[" + i + "][ad_id]", userb.getAd_id());
				params.addBodyParameter("data[" + i + "][app_id]", userb.getApp_id());
				params.addBodyParameter("data[" + i + "][behavior_type]", userb.getBehavior_type());
				params.addBodyParameter("data[" + i + "][connection]", userb.getConnection());
				params.addBodyParameter("data[" + i + "][finger_point]", userb.getFinger_point());
				params.addBodyParameter("data[" + i + "][local_time]", userb.getLocal_time());
				params.addBodyParameter("data[" + i + "][location_lat]", userb.getLocation_lat());
				params.addBodyParameter("data[" + i + "][location_lng]", userb.getLocation_lng());
				params.addBodyParameter("data[" + i + "][location_type]", userb.getLocation_type());
				params.addBodyParameter("data[" + i + "][place_id]", userb.getPlace_id());
				params.addBodyParameter("data[" + i + "][signature]", userb.getSignature());
				params.addBodyParameter("data[" + i + "][sdcard_enable]", userb.getSdcard_enable());
				params.addBodyParameter("data[" + i + "][sound_enable]", userb.getSound_enable());
			}
			LogUtils.i("params-->"+params.getEntity());
				
			http.send(HttpRequest.HttpMethod.POST, Code.log, params, new RequestCallBack<String>() {
				@Override
				public void onSuccess(ResponseInfo<String> responseInfo) {
					LogUtils.i("uploadUserBehavior responseInfo.result-->" + responseInfo.result);
					Message msg = handler.obtainMessage();
					JSONObject jo = null;
					try {
						jo = JSONObject.parseObject(responseInfo.result);
					} catch (Exception e) {
						return;
					}
					if (jo.getIntValue("error_code") == 0) {
						for (int i = 0; i < list.size(); i++) {
							UserBehavior ub = list.get(i);
						}
						msg.what = Code.behaviorSucess;
					} else if (jo.getIntValue("error_code") == 4012) {
						msg.what = Code.behaviorSucess;
					} else {
						msg.what = Code.behaviorFailed;
					}

					msg.obj = responseInfo.result;
					handler.sendMessage(msg);
				}

				@Override
				public void onFailure(HttpException error, String msg) {
					LogUtils.i("upload behavior failed");
				}
			});
		} else {// curl
		}
	}
	// 上传设备信息
	public void uploadDevicemeta() {

		LogUtils.i("upload device meta");
		if (isConnect(context)) {
			HttpUtils http = new HttpUtils();
			RequestParams params = new RequestParams();
			params.addBodyParameter("data_type", dm.getData_type());
			params.addBodyParameter("data[model]", dm.getModel());
			params.addBodyParameter("data[os_version]", dm.getOs_version());
			params.addBodyParameter("data[network_operator]", dm.getNetwork_operator());
			params.addBodyParameter("data[finger_point]", dm.getFinger_point());
			params.addBodyParameter("data[resolution]", dm.getResolution());
			params.addBodyParameter("data[display]", dm.getDisplay());
			params.addBodyParameter("data[imei]", dm.getImei());
			params.addBodyParameter("data[meid]", dm.getMeid());
			params.addBodyParameter("data[mac]", dm.getMac());
			params.addBodyParameter("data[ifa]", dm.getIfa());
			params.addBodyParameter("data[signature]", dm.getSignature());

			http.send(HttpRequest.HttpMethod.POST, Code.log, params, new RequestCallBack<String>() {
				@Override
				public void onSuccess(ResponseInfo<String> responseInfo) {
					LogUtils.i("device meta--> " + responseInfo.result);
					JSONObject jo = null;
					try {
						jo = JSONObject.parseObject(responseInfo.result);
					} catch (Exception e) {
						return;
					}
					if (jo.getIntValue("error_code") == 0) {
						LogUtils.i("upload device meta success!");
						String device_id = jo.getJSONObject("data").getString("device_id");
						boolean b = AdSave.setDeviceId(context, device_id);
						if (b) {
							LogUtils.i("setDeviceId");
						} else {
							LogUtils.i("setDeviceId failed");
						}
						handler.sendEmptyMessage(Code.pushid);
					}
				}
				@Override
				public void onFailure(HttpException error, String msg) {
				}
			});
		}
	}

	// 广告销毁
	public void destroy(Context context) {
		if (pop != null) {
			if (pop.isShowing()) {
				pop.dismiss();
			}
		}
		if(null!=locationManager){
			if(null!=loclistener){
				locationManager.removeUpdates(loclistener);
			}
		}
		closeGPS(context);

		if (receiver != null) {
			try {
				context.unregisterReceiver(receiver);
			} catch (Exception e) {
			}
		}

		UserBehavior ub = getUserBehavior("appclose", "", "");
		uploadUserBehavior(ub);
		AdSave.setInstalled(context, false);
	}

	// 广告开始
	private void onStart() {
		if (!AdSave.isInstall(context)) {
			UserBehavior ub = getUserBehavior("appinstall", "", "");
			uploadUserBehavior(ub);
			LogUtils.i("appinstall");
			AdSave.setInstalled(context, true);
		}
		UserBehavior ub = getUserBehavior("applanch", "", "");
		uploadUserBehavior(ub);
	}

	// 用户行为
	public UserBehavior getUserBehavior(String behavior_type, String ad_id, String place_id) {
		UserBehavior ub = new UserBehavior(context, app_id, behavior_type, ad_id, place_id, location_lat, location_lng,
				location_type);
		return ub;
	}
	// 得到所有place_id
	private void getPlaceId() {
		if (isConnect(context) && app_id != null && !"".equals(app_id)) {
			HttpUtils http = new HttpUtils();
			RequestParams params = new RequestParams();
			params.addBodyParameter("appid", app_id);

			http.send(HttpMethod.POST, Code.place_id, params, new RequestCallBack<String>() {
				@Override
				public void onSuccess(ResponseInfo<String> responseInfo) {
					LogUtils.i("place_id--> " + responseInfo.result);
					JSONObject jo = JSONObject.parseObject(responseInfo.result);
					if (0 == jo.getIntValue("error_code")) {
						//横幅,标语 inline
						
						bannerPlaceId = JSONObject.parseArray(jo.getString("1"), String.class);
						//弹窗;
						popPlaceId = JSONObject.parseArray(jo.getString("2"), String.class);
						//开屏图;
						startPlaceId = JSONObject.parseArray(jo.getString("3"), String.class);
						//推送;
						pushPlaceId = JSONObject.parseArray(jo.getString("6"), String.class);
						//积分墙;
						wallPlaceId = JSONObject.parseArray(jo.getString("5"), String.class);
						if (null != startPlaceId && startPlaceId.size() > 0) {
							handler.sendEmptyMessage(Code.pl);
						}
					}

				}

				@Override
				public void onFailure(HttpException error, String msg) {
					LogUtils.e("getPalceIdfailed");
				}
			});

		}else{
			
			LogUtils.e("place_id--> null");
		}
	}

	//
	public String getStartPlaceId() {
		if (startPlaceId != null) {
			String s = getLastStartPlaceId();
			index3 = startPlaceId.indexOf(s);
			//获取,id保存的位置
			if (index3 == -1) {
				index3 = 0;
			} else {
				index3++;
			}
			LogUtils.i("startplaceid->" + s);
			return s;
		}
		return null;
	}

	//获取,新开屏id;
	public String getStartNewPlaceId() {
		if (startPlaceId != null && startPlaceId.size() > 0) {
			//求余数;
			String s = startPlaceId.get(index3 % startPlaceId.size());
			return s;
		}
		return null;
	}

	//
	public String getPopPlaceId() {
		if (popPlaceId != null && popPlaceId.size() > 0) {
			String s = popPlaceId.get(index2 % popPlaceId.size());
			index2++;
			LogUtils.i("PopPlaceId-->" + s);
			return s;
		}
		return null;
	}

	//
	public String getBannerPlaceId() {
		if (bannerPlaceId != null && bannerPlaceId.size() > 0) {
			String s = bannerPlaceId.get(index1 % bannerPlaceId.size());
			index1++;
			LogUtils.i("bannerPlaceId-->" + s);
			return s;
		}
		return null;
	}

	//
	public String getWallPlaceId() {
		if (wallPlaceId != null && wallPlaceId.size() > 0) {
			String s = wallPlaceId.get(index5 % wallPlaceId.size());
			index5++;
			LogUtils.i("wallPlaceId-->" + s);
			return s;
		}
		return null;
	}

	//
	public List<String> getPushPlaceId() {
		return pushPlaceId;
	}

	//
	public void saveStartPlaceId(String placeid) {
		SharedPreferences sharedPreferences = context.getSharedPreferences("hzpd", Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putString("placeid", placeid);
		editor.commit();
	}

	/**
	 * 获取原来保存的开机id
	 * @return
	 */
	private String getLastStartPlaceId() {
		SharedPreferences sharedPreferences = context.getSharedPreferences("hzpd", Context.MODE_PRIVATE);
		return sharedPreferences.getString("placeid", "");
	}

	public boolean addAd(String id) {
		return adSet.add(id);
	}

	public boolean removeAd(String id) {
		return adSet.remove(id);
	}

	public boolean isAddedAd(String id) {
		return adSet.contains(id);
	}

	public static int dip2px(float dipValue, float scale) {
		return (int) (dipValue * scale + 0.5f);
	}

	private boolean isGpsOPen(final Context context) {
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		if (gps || network) {
			return true;
		}

		return false;
	}

	private void openGPS(Context context) {
		// ContentResolver resolver = context.getContentResolver();
		// Settings.Secure.setLocationProviderEnabled(resolver,
		// LocationManager.GPS_PROVIDER, true);

		if (isGpsOPen(context)) {
			Intent GPSIntent = new Intent();
			GPSIntent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
			GPSIntent.addCategory("android.intent.category.ALTERNATIVE");
			GPSIntent.setData(Uri.parse("custom:3"));
			try {
				PendingIntent.getBroadcast(context, 0, GPSIntent, 0).send();
			} catch (CanceledException e) {
				e.printStackTrace();
			}
		}
	}

	private void closeGPS(Context context) {
		if (!isGpsOPen(context)) {
			Intent poke = new Intent();
			poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
			poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
			poke.setData(Uri.parse("3"));
			context.sendBroadcast(poke);
		}
	}
	
	class MyLocListener implements LocationListener{
		@Override
		public void onLocationChanged(Location location) {
			if (!TextUtils.isEmpty(location.getLongitude() + "")
					&& !TextUtils.isEmpty(location.getLatitude() + "")) {
				
				JSONObject obj=new JSONObject();
				obj.put("lng", location.getLongitude()+"");
				obj.put("lat", location.getLatitude() + "");
				Message msg=handler.obtainMessage();
				msg.what=Code.location;
				msg.obj=obj.toJSONString();
				handler.sendMessage(msg);
			}
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		@Override
		public void onProviderEnabled(String provider) {
			LogUtils.i("gps enable");
		}
		@Override
		public void onProviderDisabled(String provider) {
			LogUtils.i("gps disable");
		}
		
	}
}
