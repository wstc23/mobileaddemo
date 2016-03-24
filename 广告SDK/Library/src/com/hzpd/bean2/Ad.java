package com.hzpd.bean2;

import java.io.Serializable;

import com.hzpd.bean.AdBean;
/**
 * 
 * @author xinyuan
 * ²åÆÁ¡¢Ìõ·ù
 */
public class Ad implements Serializable{
	private static final long serialVersionUID = 1L;

	private int phpad_id;
	private Control control;
	private AdBean ad;
	
	
	public int getPhpad_id() {
		return phpad_id;
	}
	public void setPhpad_id(int phpad_id) {
		this.phpad_id = phpad_id;
	}
	public Control getControl() {
		return control;
	}
	public void setControl(Control control) {
		this.control = control;
	}
	public AdBean getAd() {
		return ad;
	}
	public void setAd(AdBean ad) {
		this.ad = ad;
	}
	
}
