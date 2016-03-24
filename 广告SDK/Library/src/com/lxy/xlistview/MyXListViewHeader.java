/**
 * @file XListViewHeader.java
 * @create Apr 18, 2012 5:22:27 PM
 * @author Maxwin
 * @description XListView's header
 */
package com.lxy.xlistview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MyXListViewHeader extends LinearLayout {
	private LinearLayout mContainer;//最外层
	private RelativeLayout mHeaderViewContent;//
	
	private TextView mHeaderTimeView;//
	private ImageView mArrowImageView;
	private ProgressBar mProgressBar;
	private TextView mHintTextView;
	private int mState = STATE_NORMAL;

	private Animation mRotateUpAnim;
	private Animation mRotateDownAnim;
	
	private final int ROTATE_ANIM_DURATION = 180;
	
	public final static int STATE_NORMAL = 0;
	public final static int STATE_READY = 1;
	public final static int STATE_REFRESHING = 2;

	private DisplayMetrics dm;
	
	public MyXListViewHeader(Context context) {
		super(context);
		initView(context);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public MyXListViewHeader(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	private void initView(Context context) {
		dm=getResources().getDisplayMetrics();
		// 初始情况，设置下拉刷新view高度为0
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, 0);
		mContainer = new LinearLayout(context);
		
		addView(mContainer, lp);
		setGravity(Gravity.BOTTOM);
		//-----------------------relativelayout--------------------------
		android.widget.RelativeLayout.LayoutParams params_mh=new android.widget.RelativeLayout.LayoutParams(
				android.widget.RelativeLayout.LayoutParams.MATCH_PARENT,dip2px(60, dm.density)); 
		mHeaderViewContent=new RelativeLayout(context);
		mHeaderViewContent.setLayoutParams(params_mh);
		
		mContainer.addView(mHeaderViewContent);
		//-------------------LinearLayout1------------------------------
		android.widget.RelativeLayout.LayoutParams params_l1=new android.widget.RelativeLayout.LayoutParams(
				android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT
				,android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT); 
		params_l1.addRule(RelativeLayout.CENTER_IN_PARENT);
		LinearLayout xlistview_header_text=new LinearLayout(context);
		xlistview_header_text.setId(1991);
		xlistview_header_text.setLayoutParams(params_l1);
		xlistview_header_text.setGravity(Gravity.CENTER);
		xlistview_header_text.setOrientation(LinearLayout.VERTICAL);
		
		mHeaderViewContent.addView(xlistview_header_text);
		//---------------------textview1----------------------------
		LayoutParams pa_t1=new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		mHintTextView=new TextView(context);
		mHintTextView.setLayoutParams(pa_t1);
		mHintTextView.setTextSize(dip2px(5, dm.density));
		
		xlistview_header_text.addView(mHintTextView);
		//---------------------linearlayout2----------------------------
		LayoutParams params_l2=new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params_l2.setMargins(0, dip2px(3, dm.density), 0, 0);
		LinearLayout l2=new LinearLayout(context);
		l2.setLayoutParams(params_l2);
		
		xlistview_header_text.addView(l2);
		//---------------------textview2----------------------------
		LayoutParams params_tv2=new LayoutParams(
				LayoutParams.WRAP_CONTENT
				, LayoutParams.WRAP_CONTENT);
		TextView xlistview_header_last_time=new TextView(context);
		xlistview_header_last_time.setLayoutParams(params_tv2);
		xlistview_header_last_time.setText("上次更新时间：");
		xlistview_header_last_time.setTextSize(dip2px(5, dm.density));
		
		l2.addView(xlistview_header_last_time);
		
		//------------------------textview3-------------------------
		mHeaderTimeView=new TextView(context);
		mHeaderTimeView.setLayoutParams(params_tv2);
		mHeaderTimeView.setTextSize(dip2px(5, dm.density));
		
		l2.addView(mHeaderTimeView);
		
		//-------------------------------------------------
		android.widget.RelativeLayout.LayoutParams params_arrow=new android.widget.RelativeLayout.LayoutParams(
				android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT
				,android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
		params_arrow.addRule(RelativeLayout.ALIGN_LEFT,1990);
		params_arrow.addRule(RelativeLayout.CENTER_VERTICAL);
		params_arrow.setMargins(dip2px(-35, dm.density), 0, 0, 0);
		mArrowImageView = new ImageView(context);
		mArrowImageView.setLayoutParams(params_arrow);

		
		mHeaderViewContent.addView(mArrowImageView);
		//-------------------------------------------------
		android.widget.RelativeLayout.LayoutParams params_progress=new android.widget.RelativeLayout.LayoutParams(
				dip2px(30, dm.density),dip2px(30, dm.density));
		params_progress.addRule(RelativeLayout.ALIGN_LEFT,1991);
		params_progress.addRule(RelativeLayout.CENTER_VERTICAL);
		params_progress.setMargins(dip2px(-40, dm.density), 0, 0, 0);
		mProgressBar = new ProgressBar(context);
		mProgressBar.setId(1990);
		mProgressBar.setVisibility(View.INVISIBLE);
		mProgressBar.setLayoutParams(params_progress);
		
		
		mHeaderViewContent.addView(mProgressBar);
		
		//-------------动画--------------------
		mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
		mRotateUpAnim.setFillAfter(true);
		mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
		mRotateDownAnim.setFillAfter(true);
		
	}

	public RelativeLayout getmHeaderViewContent(){
		return mHeaderViewContent;
	}
	
	public TextView getmHeaderTimeView(){
		return mHeaderTimeView;
	}
	
	public void setState(int state) {
		if (state == mState) return ;
		
		if (state == STATE_REFRESHING) {	// 显示进度
			mArrowImageView.clearAnimation();
			mArrowImageView.setVisibility(View.INVISIBLE);
			mProgressBar.setVisibility(View.VISIBLE);
		} else {	// 显示箭头图片
			mArrowImageView.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.INVISIBLE);
		}
		
		switch(state){
		case STATE_NORMAL:
			if (mState == STATE_READY) {
				mArrowImageView.startAnimation(mRotateDownAnim);
			}
			if (mState == STATE_REFRESHING) {
				mArrowImageView.clearAnimation();
			}
			mHintTextView.setText("下拉刷新");
			break;
		case STATE_READY:
			if (mState != STATE_READY) {
				mArrowImageView.clearAnimation();
				mArrowImageView.startAnimation(mRotateUpAnim);
				mHintTextView.setText("松开刷新数据");
			}
			break;
		case STATE_REFRESHING:
			mHintTextView.setText("正在加载...");
			break;
			default:
		}
		
		mState = state;
	}
	
	public void setVisiableHeight(int height) {
		if (height < 0)
			height = 0;
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContainer
				.getLayoutParams();
		lp.height = height;
		mContainer.setLayoutParams(lp);
	}

	public int getVisiableHeight() {
		return mContainer.getHeight();
	}

	public int dip2px(float dipValue, float scale) {
		return (int) (dipValue * scale + 0.5f);
	}
}
