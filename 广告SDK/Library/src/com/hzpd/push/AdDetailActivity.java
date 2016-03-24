package com.hzpd.push;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.color.myxutils.BitmapUtils;
import com.color.myxutils.HttpUtils;
import com.color.myxutils.exception.HttpException;
import com.color.myxutils.http.ResponseInfo;
import com.color.myxutils.http.callback.RequestCallBack;
import com.color.myxutils.util.LogUtils;
import com.hzpd.bean3.UserBehavior;
import com.hzpd.http.AdSave;
import com.hzpd.library.AdSDK;
import com.hzpd.wall.WallActivity;
import com.hzpd.wall.WallActivity.MylistUpdateBroadcast;

public class AdDetailActivity extends Activity {
	private static final String img_back_URL="http://wisead.cn/assets/static/back.png";
	
	private JSONObject object;
	private String place_id;
	
	private ImageView image;
	private TextView tv_name;
	private TextView bt_install;
	private ImageView img_back;
	
	private TextView tv_introduce;
	private HorizontalScrollView hscroll;
	private List<ImageView> imgvList;
	
	private BitmapUtils bitmapUtils;
	private HttpUtils httpUtils;
	
	private DisplayMetrics dm;
	
	private GradientDrawable mDrawable;
	private String score;
	private MylistUpdateBroadcast mb;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		LinearLayout r=getLayout();
		if(r==null) return;
		
		setContentView(r);
		
		onAdShowed(object, place_id);
		
		if(AdSDK.getInstance().isAddedAd(object.getString("id"))){
			bt_install.setClickable(false);
			bt_install.setText("正在下载");
		}else{
			bt_install.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					bt_install.setClickable(false);
					bt_install.setText("正在下载");
					
					Intent intent=new Intent(AdDetailActivity.this,PushService.class);
					intent.setAction(PushService.downloadAction);
					intent.putExtra("id", object.getIntValue("id"));
					intent.putExtra("obj", object.toJSONString());
					intent.putExtra("place_id", place_id);
					
					if(score!=null&&!"0".equals(score)){
						intent.putExtra("score", score);
					}
					
					startService(intent);
					
				}
			});
		}
		
		File f=new File(AdSave.sdcardPath+"back.png");
		if(!f.exists()){
			httpUtils.download(img_back_URL
				, AdSave.sdcardPath+"back.png"
				, new RequestCallBack<File>() {
				@Override
				public void onSuccess(ResponseInfo<File> responseInfo) {
					img_back.setImageBitmap(BitmapFactory.decodeFile(responseInfo.result.getAbsolutePath()));
				}
				@Override
				public void onFailure(HttpException error, String msg) {
					
				}
			});
		}else{
			img_back.setImageBitmap(BitmapFactory.decodeFile(f.getAbsolutePath()));
		}
		img_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		mb=new MylistUpdateBroadcast();
		IntentFilter filter=new IntentFilter();
		filter.addAction(WallActivity.LIST_UPDATE);
		this.registerReceiver(mb, filter);
	}
	
	private LinearLayout getLayout(){
		Intent intent=getIntent();
		if(intent==null) return null;
		dm=getResources().getDisplayMetrics();
		
		try {
			String jsonString= intent.getStringExtra("object");
			place_id=intent.getStringExtra("place_id");
			LogUtils.e("-->"+jsonString);
			object=JSONObject.parseObject(jsonString);
			
			onViewClick(object, place_id);
		} catch (Exception e) {
			return null;
		}
			
		score=object.getString("offer_app_credit");

		bitmapUtils=new BitmapUtils(this);
		httpUtils=new HttpUtils();
		//--------------------------------------------------
		LayoutParams paramsrll=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		
		LinearLayout ll=new LinearLayout(this);
		ll.setLayoutParams(paramsrll);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setBackgroundColor(Color.WHITE);
		//-----------------------------------------------
		int w= dip2px(50, dm.density);
		RelativeLayout llHead=new RelativeLayout(this);
		LayoutParams paramsllhead=new LayoutParams(LayoutParams.MATCH_PARENT, w);
		llHead.setLayoutParams(paramsllhead);
		llHead.setBackgroundColor(Color.parseColor("#2c9ad9"));
		ll.addView(llHead);
		
		TextView title_tv=new TextView(this);
		title_tv.setTextSize(dip2px(13, dm.density));
		title_tv.setTextColor(Color.WHITE);
		title_tv.setText("应用信息");
		RelativeLayout.LayoutParams paramstv=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		paramstv.addRule(RelativeLayout.CENTER_IN_PARENT);
		title_tv.setLayoutParams(paramstv);
		llHead.addView(title_tv);
		img_back=new ImageView(this);
		RelativeLayout.LayoutParams paramsimg=new RelativeLayout.LayoutParams(dip2px(40, dm.density),dip2px(40, dm.density));
		paramsimg.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		paramsimg.setMargins(dip2px(10, dm.density), 0, 0, 0);
		paramsimg.addRule(RelativeLayout.CENTER_VERTICAL);
		img_back.setLayoutParams(paramsimg);
		llHead.addView(img_back);
//		bitmapUtils.display(img_back, img_back_URL);//url
		//----------------------------------------
		ScrollView scroll=new ScrollView(this);
		LayoutParams paramssc=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		scroll.setLayoutParams(paramssc);
		
		//--------------------------------------------
		LayoutParams paramsbody=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		
		LinearLayout llbody=new LinearLayout(this);
		llbody.setLayoutParams(paramsbody);
		llbody.setOrientation(LinearLayout.VERTICAL);
		llbody.setPadding(0, dip2px(20, dm.density), 0, dip2px(20, dm.density));
		
		//---------------------------------------
		scroll.addView(llbody);
		ll.addView(scroll);
		//------------------------------------------
		RelativeLayout rll1=new RelativeLayout(this);
		LayoutParams paramsrll1=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		rll1.setLayoutParams(paramsrll1);
		llbody.addView(rll1);
		//----------------------------------------------------
		int px=dip2px(64, dm.density);
		RelativeLayout.LayoutParams paramImage=new RelativeLayout.LayoutParams(px,px);
		px=dip2px(10, dm.density);
		paramImage.setMargins(px,px,px,px);
		image=new ImageView(this);
		image.setLayoutParams(paramImage);
		image.setId(1990);
		bitmapUtils.display(image,object.getJSONObject(object.getString("effect")).getString("icon"));
		rll1.addView(image);
		//------------------------------------------------
		RelativeLayout.LayoutParams paramstv_name=new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		paramstv_name.addRule(RelativeLayout.RIGHT_OF,image.getId());
		paramstv_name.addRule(RelativeLayout.CENTER_VERTICAL);
		paramstv_name.topMargin=dip2px(10, dm.density);
		tv_name=new TextView(this);
		tv_name.setTextSize(dip2px(10, dm.density));
		tv_name.setTextColor(Color.parseColor("#A9A9A9"));
		tv_name.setLayoutParams(paramstv_name);
		tv_name.setText(object.getJSONObject(object.getString("effect"))
				.getString("name"));
		
		rll1.addView(tv_name);
		//-------------------------------------------------
		LayoutParams paramsBt=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		paramsBt.setMargins(dip2px(20, dm.density), dip2px(20, dm.density)
				, dip2px(20, dm.density), dip2px(20, dm.density));
		
//		paramsBt.rightMargin=dip2px(20, dm.density);
		
		bt_install=new TextView(this);
		bt_install.setText("马上安装");
		TextPaint paint = bt_install.getPaint();
		paint.setFakeBoldText(true); 
		
		bt_install.setTextSize(dip2px(9, dm.density));
		bt_install.setTextColor(Color.WHITE);
		bt_install.setLayoutParams(paramsBt);
		bt_install.setGravity(Gravity.CENTER);
		bt_install.setHeight(dip2px(35, dm.density));
		
		//###########
		Path mPath = new Path();  
		Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);  
//		Rect mRect = new Rect(0, 0, 180, 80);  
	  
		mDrawable = new GradientDrawable();  
	    mDrawable.setShape(GradientDrawable.RECTANGLE);  
	    mDrawable.setStroke(dip2px(1, dm.density), Color.parseColor("#ff9a09"));
	    
	    mDrawable.setColor(Color.parseColor("#ff9a09"));
	    mDrawable.setCornerRadius(dip2px(5, dm.density));
	    bt_install.setBackgroundDrawable(mDrawable);
		//###########
		bt_install.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mDrawable.setColor(Color.parseColor("#e67700"));
					bt_install.setBackgroundDrawable(mDrawable);
					break;
				case MotionEvent.ACTION_UP:
					mDrawable.setColor(Color.parseColor("#ff9a09"));
					bt_install.setBackgroundDrawable(mDrawable);
					break;
				}
				return false;
			}
		});
		
		llbody.addView(bt_install);
		
		//-----------------------------------------------------
		LayoutParams paramLine=new LayoutParams(
				LayoutParams.MATCH_PARENT,dip2px(1, dm.density));
		paramLine.topMargin=dip2px(10, dm.density);
		View dividerLine=new View(this);
		dividerLine.setLayoutParams(paramLine);
		dividerLine.setId(1991);
		dividerLine.setBackgroundColor(Color.parseColor("#bfbfbf"));
		llbody.addView(dividerLine);
		//--------------------------------------------------------
		
		LinearLayout intronduce_ll=new LinearLayout(this);
		LayoutParams intronduce_ll_params=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		intronduce_ll.setLayoutParams(intronduce_ll_params);
		intronduce_ll.setOrientation(LinearLayout.VERTICAL);
		intronduce_ll.setBackgroundColor(Color.parseColor("#ececec"));
		
		llbody.addView(intronduce_ll);
		
		LayoutParams paramtv_introduce=new LayoutParams(
				LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
	
		tv_introduce=new TextView(this);
		tv_introduce.setTextSize(dip2px(8, dm.density));
		tv_introduce.setTextColor(Color.BLACK);
		tv_introduce.setLineSpacing(1f, 1.2f);
		StringBuilder sb=new StringBuilder();
		sb.append("\t\t");
		sb.append(object.getJSONObject(object.getString("effect")).getString("description"));
		tv_introduce.setText(sb.toString());
		tv_introduce.setLayoutParams(paramtv_introduce);
		px=dip2px(10, dm.density);
		tv_introduce.setPadding(px,px,px,px);
		
		intronduce_ll.addView(tv_introduce);
		
//		//--------------
//		LayoutParams paramLine1=new LayoutParams(
//				LayoutParams.MATCH_PARENT,dip2px(1, dm.density));
//		paramLine1.topMargin=dip2px(10, dm.density);
//		View dividerLine1=new View(this);
//		dividerLine1.setLayoutParams(paramLine1);
//		dividerLine1.setBackgroundColor(Color.parseColor("#bfbfbf"));
//		llbody.addView(dividerLine1);
//		//------
//		TextView tv_pic=new TextView(this);
//		LayoutParams paramTvPic=new LayoutParams(
//				LayoutParams.MATCH_PARENT,dip2px(40, dm.density));
//		tv_pic.setLayoutParams(paramTvPic);
//		tv_pic.setTextColor(Color.BLACK);
//		tv_pic.setText("应用截图");
//		tv_pic.setTextSize(dip2px(11, dm.density));
//		tv_pic.setGravity(Gravity.CENTER_VERTICAL);
//		tv_pic.setPadding(dip2px(10, dm.density), 0, 0, 0);
//		tv_pic.setBackgroundColor(Color.parseColor("#f7f7f7"));
//		llbody.addView(tv_pic);
//		
//		//------
//		LayoutParams paramLine2=new LayoutParams(
//				LayoutParams.MATCH_PARENT,dip2px(1, dm.density));
//		View dividerLine2=new View(this);
//		dividerLine2.setLayoutParams(paramLine2);
//		dividerLine2.setBackgroundColor(Color.parseColor("#bfbfbf"));
//		llbody.addView(dividerLine2);
//		//------
		LayoutParams paramshscroll=new LayoutParams(
				LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		
		hscroll=new HorizontalScrollView(this);
		hscroll.setLayoutParams(paramshscroll);
		intronduce_ll.addView(hscroll);
		
		//--------------------------------------------------------
		LayoutParams paramLine3=new LayoutParams(
				LayoutParams.MATCH_PARENT,dip2px(1, dm.density));
		paramLine3.bottomMargin=dip2px(50, dm.density);
		View dividerLine3=new View(this);
		
		dividerLine3.setLayoutParams(paramLine3);
		dividerLine3.setBackgroundColor(Color.parseColor("#bfbfbf"));
		llbody.addView(dividerLine3);
		//------------------------------------------------------------
		LinearLayout.LayoutParams pl=new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT
				,LinearLayout.LayoutParams.WRAP_CONTENT);
		LinearLayout linearLayout=new LinearLayout(this);
		linearLayout.setLayoutParams(pl);
		linearLayout.setOrientation(LinearLayout.HORIZONTAL);
		linearLayout.setPadding(0, 0, dip2px(10, dm.density), 0);
		
		hscroll.addView(linearLayout);
		
		imgvList=new ArrayList<ImageView>();
		
		//--------------------------------------------------------
		final int wi=dip2px(90, dm.density);
		final int he=dip2px(150, dm.density);
		LinearLayout.LayoutParams paramsImg=new LinearLayout.LayoutParams(wi,he);
		
		paramsImg.setMargins(dip2px(20, dm.density), dip2px(15, dm.density)
				, 0, dip2px(20, dm.density));
		JSONArray array=object.getJSONObject(object.getString("effect")).getJSONArray("image");
	
		for(int i=0;i<array.size();i++){
			String imgUrl=(String) array.getString(i);
			LogUtils.i("imgUrl-->"+imgUrl);
			final ImageView myimg=new ImageView(this);
			myimg.setScaleType(ScaleType.FIT_XY);
			imgvList.add(myimg);
			myimg.setLayoutParams(paramsImg);
		
			linearLayout.addView(myimg);
			bitmapUtils.display(myimg, imgUrl);
			
		}
		
		return ll;
	}

	
	public int dip2px(float dipValue, float scale) {
	  return (int) (dipValue * scale + 0.5f);
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mb);
		super.onDestroy();
	}
	
	public void onclick(){
		Intent intent=new Intent(this,PushService.class);
		intent.putExtra("id", object.getInteger("id"));
    	intent.setAction(PushService.downloadAction);
    	startService(intent);
	}
	
	public class MylistUpdateBroadcast extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			LogUtils.i("--update--");
			if(bt_install==null){
				return;
			}
			if(AdSDK.getInstance().isAddedAd(object.getString("id"))){
				bt_install.setClickable(false);
				bt_install.setText("正在下载");
			}else{
				bt_install.setClickable(true);
				bt_install.setText("马上安装");
			
				bt_install.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						
						Intent intent=new Intent(AdDetailActivity.this,PushService.class);
						intent.setAction(PushService.downloadAction);
						intent.putExtra("id", object.getInteger("id"));
						intent.putExtra("obj", object.toJSONString());
						intent.putExtra("place_id", place_id);
						
						if(score!=null&&!"0".equals(score)){
							intent.putExtra("score", score);
						}
						
						startService(intent);
						bt_install.setClickable(false);
						bt_install.setText("正在下载");
					}
				});
			}
		}
	}
	
	
	
	//点击
	private void onViewClick(JSONObject myad,String place_id){
		if(myad==null) return;
		UserBehavior ub=AdSDK.getInstance().getUserBehavior("adclick", myad.getString("id"), place_id);
		AdSDK.getInstance().uploadUserBehavior(ub);
	}
	
	
	//展示
	private void onAdShowed(JSONObject myad,String place_id){
		UserBehavior ub=AdSDK.getInstance().getUserBehavior("adshowed", myad.getString("id"), place_id);
		AdSDK.getInstance().uploadUserBehavior(ub);
	}
			
}
