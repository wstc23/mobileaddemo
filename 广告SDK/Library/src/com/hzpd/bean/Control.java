package com.hzpd.bean;

import java.io.Serializable;

public class Control implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String time_duration;

	public int getTime_duration() {
		int du=Integer.parseInt(time_duration);
		return du;
	}

	public void setTime_duration(String time_duration) {
		this.time_duration = time_duration;
	}
	
}
