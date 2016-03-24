package com.hzpd.bean;

import java.io.Serializable;

public class AdBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String id;
	private int width;
	private int height;
	private String content;
	private String base_url;
	private String view_tracker;
	private String event_tracker;
	private String tracker;
	private String image;
	private boolean close_button;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getBase_url() {
		return base_url;
	}
	public void setBase_url(String base_url) {
		this.base_url = base_url;
	}
	public String getView_tracker() {
		return view_tracker;
	}
	public void setView_tracker(String view_tracker) {
		this.view_tracker = view_tracker;
	}
	public String getEvent_tracker() {
		return event_tracker;
	}
	public void setEvent_tracker(String event_tracker) {
		this.event_tracker = event_tracker;
	}
	public String getTracker() {
		return tracker;
	}
	public void setTracker(String tracker) {
		this.tracker = tracker;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public boolean isClose_button() {
		return close_button;
	}
	public void setClose_button(boolean close_button) {
		this.close_button = close_button;
	}
	
}
