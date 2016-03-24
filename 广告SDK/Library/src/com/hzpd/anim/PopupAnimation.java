package com.hzpd.anim;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;

public class PopupAnimation {

	public AnimationSet getEnter(){
		AnimationSet set=new AnimationSet(true);
		ScaleAnimation scal=new ScaleAnimation(0.6f, 1.0f, 0.6f, 1.0f,
				Animation.RELATIVE_TO_PARENT,0.5f,
				Animation.RELATIVE_TO_PARENT,0.5f);
		scal.setDuration(300);
		AlphaAnimation alph=new AlphaAnimation(0.0f, 1.0f);
		alph.setDuration(300);
		set.addAnimation(scal);
		set.addAnimation(alph);
		set.setFillAfter(true);
		return set;
	}
	public AnimationSet getExit(){
		AnimationSet set=new AnimationSet(true);
		ScaleAnimation scal=new ScaleAnimation(1.0f, 0.5f, 1.0f, 0.5f,
				Animation.RELATIVE_TO_PARENT,0.5f,
				Animation.RELATIVE_TO_PARENT,0.5f);
		scal.setDuration(300);
		AlphaAnimation alph=new AlphaAnimation(1.0f, 0.0f);
		alph.setDuration(300);
		set.addAnimation(scal);
		set.setFillAfter(true);
		return set;
	}
}
