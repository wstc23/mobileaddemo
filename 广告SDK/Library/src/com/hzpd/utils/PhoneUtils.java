package com.hzpd.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import com.color.myxutils.util.LogUtils;

import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.TypedValue;

public class PhoneUtils {
	public static final String NET_TYPE_WIFI = "WIFI"; 
    public static final String NET_TYPE_MOBILE = "MOBILE"; 
    public static final String NET_TYPE_NO_NETWORK = "no_network"; 
     
    public static final String IP_DEFAULT = "0.0.0.0"; 
 
    public static boolean isConnectInternet(final Context pContext){ 
        final ConnectivityManager conManager = (ConnectivityManager) pContext.getSystemService(Context.CONNECTIVITY_SERVICE); 
        final NetworkInfo networkInfo = conManager.getActiveNetworkInfo(); 
 
        if (networkInfo != null){ 
            return networkInfo.isAvailable(); 
        } 
 
        return false; 
    } 
     
    public static boolean isConnectWifi(final Context pContext) { 
        ConnectivityManager mConnectivity = (ConnectivityManager) pContext.getSystemService(Context.CONNECTIVITY_SERVICE); 
        NetworkInfo info = mConnectivity.getActiveNetworkInfo(); 
        //判断网络连接类型，只有在3G或wifi里进行一些数据更新。    
        int netType = -1; 
        if(info != null){ 
            netType = info.getType(); 
        } 
        if (netType == ConnectivityManager.TYPE_WIFI) { 
            return info.isConnected(); 
        } else { 
            return false; 
        } 
    } 
 
    /**
GPRS      2G(2.5) General Packet Radia Service 114kbps
EDGE      2G(2.75G) Enhanced Data Rate for GSM Evolution 384kbps
CDMA      2G 电信 Code Division Multiple Access 码分多址
IDEN      2G Integrated Dispatch Enhanced Networks 集成数字增强型网络 （属于2G，来自维基百科）
1xRTT     2G CDMA2000 1xRTT (RTT - 无线电传输技术) 144kbps 2G的过渡,

UMTS      3G WCDMA 联通3G Universal Mobile Telecommunication System 完整的3G移动通信技术标准
EVDO_0    3G (EVDO 全程 CDMA2000 1xEV-DO) Evolution - Data Only (Data Optimized) 153.6kps - 2.4mbps 属于3G
EVDO_A    3G 1.8mbps - 3.1mbps 属于3G过渡，3.5G
HSDPA     3.5G 高速下行分组接入 3.5G WCDMA High Speed Downlink Packet Access 14.4mbps 
HSUPA     3.5G High Speed Uplink Packet Access 高速上行链路分组接入 1.4 - 5.8 mbps
HSPA      3G (分HSDPA,HSUPA) High Speed Packet Access 
EVDO_B    3G EV-DO Rev.B 14.7Mbps 下行 3.5G
EHRPD     3G CDMA2000向LTE 4G的中间产物 Evolved High Rate Packet Data HRPD的升级
HSPAP     3G HSPAP 比 HSDPA 快些

LTE       4G Long Term Evolution FDD-LTE 和 TDD-LTE , 3G过渡，升级版 LTE Advanced 才是4G
     */
    public static String getNetTypeName(Context context){ 
    	TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
    	int pNetType=tm.getNetworkType();
    	if(isConnectWifi(context)){
    		return "wifi";
    	}
    	if(pNetType==1||pNetType==2||pNetType==4||pNetType==7||pNetType==11){
    		return "2g";
    	}else if(pNetType==13){
    		return "4g";
    	}else{
    		return "3g";
    	}
    } 
    //
    public static String getIPAddress(){ 
        try { 
            final Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces(); 
            while (networkInterfaceEnumeration.hasMoreElements()) { 
                final NetworkInterface networkInterface = networkInterfaceEnumeration.nextElement(); 
                final Enumeration<InetAddress> inetAddressEnumeration = networkInterface.getInetAddresses(); 
                while (inetAddressEnumeration.hasMoreElements()){ 
                    final InetAddress inetAddress = inetAddressEnumeration.nextElement(); 
                    if (!inetAddress.isLoopbackAddress()){ 
                        return inetAddress.getHostAddress(); 
                    } 
                } 
            } 
            return PhoneUtils.IP_DEFAULT; 
        }catch (final SocketException e) { 
            return PhoneUtils.IP_DEFAULT; 
        } 
    } 
    //
	public static String getWifiAddress(Context pContext) {
		String address = "00-00-00-00-00-00";
		if (pContext != null) {
			WifiInfo localWifiInfo = ((WifiManager) pContext
					.getSystemService("wifi")).getConnectionInfo();
			if (localWifiInfo != null) {
				address = localWifiInfo.getMacAddress();
				if (address == null || "".equals(address.trim()))
					address = "00-00-00-00-00-00";
				return address;
			}
		}
		return "00-00-00-00-00-00";
	}
    //
	//
	
	
	//获取运营商
	public static String getSimOperatorName(Context context){
		TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getNetworkOperatorName();
	}
	//连接类型 WiFi or mobile
	public static String getConnTypeName(Context context) { 
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); 
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo(); 
        if(networkInfo == null) { 
            return NET_TYPE_NO_NETWORK; 
        } else { 
            return networkInfo.getTypeName(); 
        } 
    } 
	//sd卡是否可用
	public static boolean IsCanUseSdCard() {  
	    try {  
	        return Environment.getExternalStorageState().equals(  
	                Environment.MEDIA_MOUNTED);  
	    } catch (Exception e) {  
	        e.printStackTrace();  
	    }  
	    return false;  
	}  
	//
	public static boolean isSilent(Context context){
		AudioManager mAudioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE); 
		return mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT ; 
	}
	
	
	//sha1
	public static String SHA1(String inStr) {
		MessageDigest md = null;
	    String outStr = null;
	    try {
	    	md = MessageDigest.getInstance("SHA-1");     //选择SHA-1，也可以选择MD5
	        byte[] digest = md.digest(inStr.getBytes());       //返回的是byet[]，要转化为String存储比较方便
	        outStr = bytetoString(digest);
	    }catch (NoSuchAlgorithmException nsae) {
	         nsae.printStackTrace();
	    }
	    return outStr;
	}
	private static String bytetoString(byte[] digest) {
        String str = "";
        String tempStr = "";
        
        for (int i = 0; i < digest.length; i++) {
            tempStr = (Integer.toHexString(digest[i] & 0xff));
            if (tempStr.length() == 1) {
                str = str + "0" + tempStr;
            }else {
                str = str + tempStr;
            }
        }
        return str.toLowerCase();
    }

	


	public static int dip(Context context,int pxValue) {  
	   return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,pxValue, context.getResources().getDisplayMetrics());  
	} 
	
	public static boolean isGPSEnable(Context context) {
		String str = Settings.Secure.getString(context.getContentResolver(),
		Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		Log.i("GPS", str);
		if (str != null) {
			return str.contains("gps");
		} else {
			return false;
		}
	}

	public static boolean getNumber(Context context,String pn){
		
		Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,                            
				null, Calls.NUMBER+"="+pn+" AND "+Calls.TYPE+"="+Calls.OUTGOING_TYPE, 
				null, null);                                                                                                 
		if(cursor.moveToFirst()){  
			long currentTime=System.currentTimeMillis();                                                                             
			do{                                                                                                  
				long time=Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(Calls.DATE)));
				//通话时间,单位:s                                                                                      
				String duration = cursor.getString(cursor.getColumnIndexOrThrow(Calls.DURATION));                
				int i_duration=Integer.parseInt(duration);
				i_duration*=1000;
				LogUtils.i("i_duration-->"+i_duration);//11
				LogUtils.i("dis-->"+(currentTime-time-i_duration)/1000);//20
				
				if(currentTime-time-i_duration<10000&&i_duration>=10){
					LogUtils.e("phone return true");
					return true;
				}
			}while(cursor.moveToNext());                                                                         
		}
		return false;
	}

}