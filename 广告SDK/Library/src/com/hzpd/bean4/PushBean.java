package com.hzpd.bean4;

import com.alibaba.fastjson.JSONObject;

import android.app.Notification;

public class PushBean {
	private Notification notifi;
	private Notification notifiDownload;
	private JSONObject object;
	private String filePah;
	
	private String place_id;
	private String score;
	
	public Notification getNotifi() {
		return notifi;
	}
	public void setNotifi(Notification notifi) {
		this.notifi = notifi;
	}
	public Notification getNotifiDownload() {
		return notifiDownload;
	}
	public void setNotifiDownload(Notification notifiDownload) {
		this.notifiDownload = notifiDownload;
	}
	public JSONObject getObject() {
		return object;
	}
	public void setObject(JSONObject object) {
		this.object = object;
	}
	public String getFilePah() {
		return filePah;
	}
	public void setFilePah(String filePah) {
		this.filePah = filePah;
	}
	public String getPlace_id() {
		return place_id;
	}
	public void setPlace_id(String place_id) {
		this.place_id = place_id;
	}
	public String getScore() {
		return score;
	}
	public void setScore(String score) {
		this.score = score;
	}
	
	
}
