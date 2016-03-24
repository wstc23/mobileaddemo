package com.hzpd.utils;

import android.os.Handler;

public class VoiceThread extends Thread {
	private VoicePlayer player;
	private String voiceUrl;
	
	public VoiceThread(Handler handler,String voiceUrl){
		player=new VoicePlayer(handler);
		this.voiceUrl=voiceUrl;
	}
	
	@Override
	public void run() {
		player.playUrl(voiceUrl);
		
	}
	
	public void stopVoice(){
		player.stop();
	}
	
}
