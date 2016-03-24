package com.hzpd.bean3;

import java.util.Arrays;

import android.content.Context;

import com.alibaba.fastjson.JSONObject;
import com.color.myxutils.db.annotation.Id;
import com.color.myxutils.db.annotation.Table;
import com.color.myxutils.db.annotation.Transient;
import com.hzpd.utils.PhoneUtils;


@Table(name="UserBehavior")
public class UserBehavior {
	@Id(column="id")
	private long id=0;
	private String app_id="";//
	private String finger_point="";//
	private String connection="";//":
	private String location_lat="";//
	private String location_lng="";//
	private String location_type="";//
	private String local_time="";//
	private String sdcard_enable="";//":"0 or 1",
	private String sound_enable="";//":"0 or 1",
	private String behavior_type="";//":"applanch", "normal", "appclose", "appinstall", "adshowed", "adclick" or "dealreach",
	private String ad_id="";//adshowed   adclick   dealreach
	private String place_id="";//adshowed,adclick,dealreach
	private String signature="";//
	
	@Transient
	private String data_type="user_behavior";
	@Transient
	private final String token="phpAD";
	
	
	public UserBehavior(){}
	
	public UserBehavior(Context context,
			String app_id,String behavior_type,String ad_id,String place_id,
			String location_lat,String location_lng,String location_type){
		this.app_id=app_id;//
		finger_point=PhoneUtils.getWifiAddress(context);
		connection=PhoneUtils.getConnTypeName(context);//
		local_time=System.currentTimeMillis()/1000+"";
		if(PhoneUtils.IsCanUseSdCard()){
			sdcard_enable="1";
		}else{
			sdcard_enable="0";
		}
		if(PhoneUtils.isSilent(context)){
			sound_enable="1";
		}else{
			sound_enable="0";
		}
		
		this.behavior_type=behavior_type;
//		:applanch, normal, appclose, appinstall, adshowed, adclick or dealreach,
		this.ad_id=ad_id;
//		id  adshowed adclick dealreach
		this.place_id=place_id;
//		adshowed,adclick,dealreach

		this.location_lat=location_lat;
		this.location_lng=location_lng;
		this.location_type=location_type;
		
		this.signature=getSignatures();
	}

	public String getSignature(){
		return signature;
	}
	
	private String getSignatures(){
		String ll[]=new String[]{
				app_id,
				finger_point,
				connection,
				location_lat,
				location_lng,
				location_type,
				local_time,
				sdcard_enable,
				sound_enable,
				behavior_type,
				ad_id,
				place_id,
				token
		};
		Arrays.sort(ll);
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<ll.length;i++){
			sb.append(ll[i]);
		}
		return PhoneUtils.SHA1(sb.toString());
	}

	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getApp_id() {
		return app_id;
	}

	public void setApp_id(String app_id) {
		this.app_id = app_id;
	}

	public String getFinger_point() {
		return finger_point;
	}

	public void setFinger_point(String finger_point) {
		this.finger_point = finger_point;
	}

	public String getConnection() {
		return connection;
	}

	public void setConnection(String connection) {
		this.connection = connection;
	}

	public String getLocation_lat() {
		return location_lat;
	}

	public void setLocation_lat(String location_lat) {
		this.location_lat = location_lat;
	}

	public String getLocation_lng() {
		return location_lng;
	}

	public void setLocation_lng(String location_lng) {
		this.location_lng = location_lng;
	}

	public String getLocation_type() {
		return location_type;
	}

	public void setLocation_type(String location_type) {
		this.location_type = location_type;
	}

	public String getLocal_time() {
		return local_time;
	}

	public void setLocal_time(String local_time) {
		this.local_time = local_time;
	}

	public String getSdcard_enable() {
		return sdcard_enable;
	}

	public void setSdcard_enable(String sdcard_enable) {
		this.sdcard_enable = sdcard_enable;
	}

	public String getSound_enable() {
		return sound_enable;
	}

	public void setSound_enable(String sound_enable) {
		this.sound_enable = sound_enable;
	}

	public String getBehavior_type() {
		return behavior_type;
	}

	public void setBehavior_type(String behavior_type) {
		this.behavior_type = behavior_type;
	}

	public String getAd_id() {
		return ad_id;
	}

	public void setAd_id(String ad_id) {
		this.ad_id = ad_id;
	}

	public String getPlace_id() {
		return place_id;
	}

	public void setPlace_id(String place_id) {
		this.place_id = place_id;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	@Override
	public String toString(){
		JSONObject jo=new JSONObject();
		jo.put("app_id", app_id);
		jo.put("finger_point", finger_point);
		jo.put("connection", connection);
		jo.put("location_lat", location_lat);
		jo.put("location_lng", location_lng);
		jo.put("location_type", location_type);
		jo.put("local_time", local_time);
		jo.put("sdcard_enable", sdcard_enable);
		jo.put("sound_enable", sound_enable);
		jo.put("behavior_type", behavior_type);
		jo.put("ad_id", ad_id);
		jo.put("place_id", place_id);
		jo.put("signature", signature);
		return jo.toJSONString();
	}
	
	public String getData_type() {
		return data_type;
	}
	public void setData_type(String data_type) {
		this.data_type = data_type;
	}

}
