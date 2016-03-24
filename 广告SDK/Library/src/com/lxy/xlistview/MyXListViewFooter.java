/**
 * @file XFooterView.java
 * @create Mar 31, 2012 9:33:43 PM
 * @author Maxwin
 * @description XListView's footer
 */
package com.lxy.xlistview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MyXListViewFooter extends LinearLayout {
	public final static int STATE_NORMAL = 0;
	public final static int STATE_READY = 1;
	public final static int STATE_LOADING = 2;

	private Context mContext;

	private View mContentView;
	private View mProgressBar;
	private TextView mHintView;
	
	private DisplayMetrics dm; 
	
	public MyXListViewFooter(Context context) {
		super(context);
		initView(context);
	}
	
	public MyXListViewFooter(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	
	public void setState(int state) {
		mHintView.setVisibility(View.INVISIBLE);
		mProgressBar.setVisibility(View.INVISIBLE);
		mHintView.setVisibility(View.INVISIBLE);
		if (state == STATE_READY) {
			mHintView.setVisibility(View.VISIBLE);
			mHintView.setText("松开载入更多");
		} else if (state == STATE_LOADING) {
			mProgressBar.setVisibility(View.VISIBLE);
		} else {
			mHintView.setVisibility(View.VISIBLE);
			mHintView.setText("查看更多");
		}
	}
	
	public void setBottomMargin(int height) {
		if (height < 0) return ;
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)mContentView.getLayoutParams();
		lp.bottomMargin = height;
		mContentView.setLayoutParams(lp);
	}
	
	public int getBottomMargin() {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)mContentView.getLayoutParams();
		return lp.bottomMargin;
	}
	
	
	/**
	 * normal status
	 */
	public void normal() {
		mHintView.setVisibility(View.VISIBLE);
		mProgressBar.setVisibility(View.GONE);
	}
	
	
	/**
	 * loading status 
	 */
	public void loading() {
		mHintView.setVisibility(View.GONE);
		mProgressBar.setVisibility(View.VISIBLE);
	}
	
	/**
	 * hide footer when disable pull load more
	 */
	public void hide() {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)mContentView.getLayoutParams();
		lp.height = 0;
		mContentView.setLayoutParams(lp);
	}
	
	/**
	 * show footer
	 */
	public void show() {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)mContentView.getLayoutParams();
		lp.height = LayoutParams.WRAP_CONTENT;
		mContentView.setLayoutParams(lp);
	}
	
	private void initView(Context context) {
		mContext = context;
		dm=getResources().getDisplayMetrics();
		
		LinearLayout moreView =new LinearLayout(mContext);
		LayoutParams params=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT );
		moreView.setOrientation(LinearLayout.HORIZONTAL);
		moreView.setLayoutParams(params);
		
		addView(moreView);
		//---------------------------------------------
		android.widget.RelativeLayout.LayoutParams params_r=new android.widget.RelativeLayout.LayoutParams(
				android.widget.RelativeLayout.LayoutParams.MATCH_PARENT
				,android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
		mContentView = new RelativeLayout(mContext);
		int dp10=dip2px(10, dm.density);
		mContentView.setPadding(dp10, dp10, dp10, dp10);
		mContentView.setLayoutParams(params_r);
		
		//---------------------------------------------
		android.widget.RelativeLayout.LayoutParams params_r_progress=new android.widget.RelativeLayout.LayoutParams(
				android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT
				,android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT);
		params_r_progress.addRule(RelativeLayout.CENTER_IN_PARENT);
		mProgressBar = new ProgressBar(mContext);
		mProgressBar.setVisibility(View.INVISIBLE);
		mProgressBar.setLayoutParams(params_r_progress);
		
		
		mHintView = new TextView(mContext);
		mHintView.setLayoutParams(params_r_progress);
		mHintView.setText("查看更多");
		
	}
	
	public int dip2px(float dipValue, float scale) {
		return (int) (dipValue * scale + 0.5f);
	}
}
