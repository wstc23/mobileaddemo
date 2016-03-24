package com.hzpd.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.DisplayMetrics;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.VideoView;

import com.alibaba.fastjson.JSONObject;
import com.color.myxutils.BitmapUtils;
import com.color.myxutils.util.LogUtils;
import com.hzpd.view.gif.GifView;

public class AdSave {
	public static String sdcardPath="/sdcard/adSdk/";
	
	public static String getMac(Activity activity){
		WifiManager wifi = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);		 
		WifiInfo info = wifi.getConnectionInfo();	 
		return info.getMacAddress();
	}
	
	public static JSONObject getAd(Context context,String key){
		SharedPreferences sharedPreferences=context.getSharedPreferences("hzpd", Context.MODE_PRIVATE);
		String s_ad=sharedPreferences.getString(key, null);
		if(s_ad==null){
			return null;
		}else{
			return JSONObject.parseObject(s_ad);
		}
	}
	
	public static boolean saveAd(Context context,JSONObject ad,String key){
		SharedPreferences sharedPreferences=context.getSharedPreferences("hzpd", Context.MODE_PRIVATE);
		Editor editor=sharedPreferences.edit();
		String s_ad=ad.toJSONString();
		LogUtils.i(s_ad);
		editor.putString(key, s_ad);
		return editor.commit();
	}
	
	public static void getPicture(Context context,String fileName,ImageView view){
		FileInputStream fis;
		Bitmap bm=null;
		try {
			fis = new FileInputStream(new File(fileName));
			bm=BitmapFactory.decodeStream(fis);
			view.setImageBitmap(bm);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void getGIF(Context context,String fileName,GifView view){
		DisplayMetrics dm=context.getApplicationContext().getResources().getDisplayMetrics();
		int newWidth = dm.widthPixels;
		int newHeight = dm.heightPixels;
		view.setMaxHeight(newHeight);
		view.setMaxWidth(newWidth);
		view.setMinimumHeight(newHeight);
		view.setMinimumWidth(newWidth);
		try {
			view.setGifImage(new FileInputStream(new File(fileName)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void getHtml(Context context,String fileName,WebView webView){
		webView.loadUrl("file:///mnt"+fileName);
	}
	
	public static void getVideo(Context context,String fileName,VideoView videoView){
		File f=new File(sdcardPath+fileName);
		if(f.exists()){
			videoView.setVideoURI(Uri.parse(sdcardPath+fileName)); 
			videoView.start();
		}
	}
	
	public static boolean getCalled(Context context){
		SharedPreferences sharedPreferences=context.getSharedPreferences("hzpd", Context.MODE_PRIVATE);
		return sharedPreferences.getBoolean("called", false);
	}
	
	public static boolean setCalled(Context context,boolean value){
		SharedPreferences sharedPreferences=context.getSharedPreferences("hzpd", Context.MODE_PRIVATE);
		Editor editor=sharedPreferences.edit();
		editor.putBoolean("called", value);
		return editor.commit();
	}
	
	public static String getAdPlaceId(Context context){
		SharedPreferences sharedPreferences=context.getSharedPreferences("hzpd", Context.MODE_PRIVATE);
		return sharedPreferences.getString("CalledPlaceId", null);
	}
	public static boolean setAdPlaceId(Context context,String value){
		SharedPreferences sharedPreferences=context.getSharedPreferences("hzpd", Context.MODE_PRIVATE);
		Editor editor=sharedPreferences.edit();
		editor.putString("CalledPlaceId", value);
		return editor.commit();
	}
	
	public static String getAdJson(Context context){
		SharedPreferences sharedPreferences=context.getSharedPreferences("hzpd", Context.MODE_PRIVATE);
		return sharedPreferences.getString("adjson", null);
	}
	public static boolean setAdJson(Context context,String value){
		SharedPreferences sharedPreferences=context.getSharedPreferences("hzpd", Context.MODE_PRIVATE);
		Editor editor=sharedPreferences.edit();
		editor.putString("adjson", value);
		return editor.commit();
	}
	
	public static boolean isInstall(Context context){
		SharedPreferences sharedPreferences=context.getSharedPreferences("hzpd", Context.MODE_PRIVATE);
		return sharedPreferences.getBoolean("isInstall", false);
	}
	
	public static boolean setInstalled(Context context,boolean flag){
		return context.getSharedPreferences("hzpd", Context.MODE_PRIVATE)
			.edit().putBoolean("isInstall", flag).commit();
	}
	
	public static String getDeviceId(Context context){
		SharedPreferences sharedPreferences=context.getSharedPreferences("hzpd", Context.MODE_PRIVATE);
		return sharedPreferences.getString("deviceid", null);
	}
	public static boolean setDeviceId(Context context,String deviceId){
		return context.getSharedPreferences("hzpd", Context.MODE_PRIVATE)
				.edit().putString("deviceid", deviceId).commit();
	}
}
