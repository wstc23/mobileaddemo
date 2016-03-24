package com.hzpd.utils;

import java.io.IOException;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.util.Log;

public class VoicePlayer implements OnCompletionListener, MediaPlayer.OnPreparedListener{
	public MediaPlayer mediaPlayer;
	private Handler handler;
	
	
	public VoicePlayer(Handler handler){
		this.handler=handler;
		
		try {
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnPreparedListener(this);
			mediaPlayer.setOnCompletionListener(this);
		} catch (Exception e) {
			Log.e("mediaPlayer", "error", e);
		}
		
	}
	
	public void play(){
		mediaPlayer.start();
	}
	
	public void playUrl(String voiceUrl){
		try {
			mediaPlayer.reset();
			mediaPlayer.setDataSource(voiceUrl);
			mediaPlayer.prepare();//prepare之后自动播放
			//mediaPlayer.start();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public void pause(){
		mediaPlayer.pause();
	}
	
	public void stop(){
		if (mediaPlayer != null) { 
			mediaPlayer.stop();
            mediaPlayer.release(); 
            mediaPlayer = null; 
        } 
	}

	@Override
	public void onPrepared(MediaPlayer arg0) {
		arg0.start();
		Log.i("mediaPlayer", "onPrepared");
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		Log.i("mediaPlayer", "onCompletion");
		handler.sendEmptyMessage(Code.musicComplete);
	}

}

