package com.hzpd.bean2;

import java.io.Serializable;

public class AdBean implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String id;
	private String width;
	private String height;
	private String content;
	private String base_url;
	private String view_tracker;
	private String click_tracke;
	private String event_tracker;
	private String tracker;
	private boolean close_button;
	private String effect;
	private String link;
	private String image;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getWidth() {
		return width;
	}
	public void setWidth(String width) {
		this.width = width;
	}
	public String getHeight() {
		return height;
	}
	public void setHeight(String height) {
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
	public String getClick_tracke() {
		return click_tracke;
	}
	public void setClick_tracke(String click_tracke) {
		this.click_tracke = click_tracke;
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
	public boolean getClose_button() {
		return close_button;
	}
	public void setClose_button(boolean close_button) {
		this.close_button = close_button;
	}
	public String getEffect() {
		return effect;
	}
	public void setEffect(String effect) {
		this.effect = effect;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}


}
