package com.hzpd.bean3;

import java.util.Arrays;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import com.alibaba.fastjson.JSONObject;
import com.hzpd.utils.PhoneUtils;

public class DeviceMessage {
	private static final String token="phpAD";
	private static final String data_type="device_meta";//":"device_meta", 	
		
	private String model;		//
	private String os_version;	//
	private String network_operator;//
	private String finger_point;//
	private String resolution;	//
	private String display;		//
	private String imei;		//
	private String meid;		//
	private String mac;			//
	private String ifa;			//
	private String signature;	//
	
	
	
	@Override
	public String toString(){
		JSONObject jo=new JSONObject();
		jo.put("model", model);
		jo.put("os_version", os_version);
		jo.put("network_operator", network_operator);
		jo.put("finger_point", finger_point);
		jo.put("resolution", resolution);
		jo.put("display", display);
		jo.put("imei", imei);
		jo.put("meid", meid);
		jo.put("mac", mac);
		jo.put("ifa", ifa);
		jo.put("signature", signature);
		return jo.toJSONString();
	}
	
	public DeviceMessage(Context context){
		model=android.os.Build.MODEL+"";
		os_version=android.os.Build.VERSION.RELEASE+"";
		TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE); 
		network_operator=PhoneUtils.getSimOperatorName(context)+"";
		finger_point=PhoneUtils.getWifiAddress(context)+"";
		DisplayMetrics dm=context.getResources().getDisplayMetrics();
		resolution=Integer.toString(dm.widthPixels)+"*"+Integer.toString(dm.heightPixels);
		display="";
		if(telMgr.getPhoneType()==TelephonyManager.PHONE_TYPE_CDMA){
			imei="";
			meid=telMgr.getDeviceId()+"";
		}else if(telMgr.getPhoneType()==TelephonyManager.PHONE_TYPE_GSM){
			imei=telMgr.getDeviceId()+"";
			meid="";
		}
	
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	    WifiInfo info = wifi.getConnectionInfo();
	    finger_point = info.getMacAddress()+"";
	    mac=finger_point+"";
	    ifa="";
	    signature=getSignatures();
	}
	public String getSignature(){
		return signature;
	}
	private String getSignatures(){
		String ss[]=new String[]{model,os_version,network_operator,
				finger_point,resolution,display,imei,meid,mac,ifa,token};
		Arrays.sort(ss);
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<ss.length;i++){
			sb.append(ss[i]);
		}
		return PhoneUtils.SHA1(sb.toString());
	}

	public String getData_type() {
		return data_type;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getOs_version() {
		return os_version;
	}

	public void setOs_version(String os_version) {
		this.os_version = os_version;
	}

	public String getNetwork_operator() {
		return network_operator;
	}

	public void setNetwork_operator(String network_operator) {
		this.network_operator = network_operator;
	}

	public String getFinger_point() {
		return finger_point;
	}

	public void setFinger_point(String finger_point) {
		this.finger_point = finger_point;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getMeid() {
		return meid;
	}

	public void setMeid(String meid) {
		this.meid = meid;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getIfa() {
		return ifa;
	}

	public void setIfa(String ifa) {
		this.ifa = ifa;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	
	
}
