package com.hzpd.wall;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.color.myxutils.BitmapUtils;
import com.color.myxutils.util.LogUtils;
import com.hzpd.bean3.UserBehavior;
import com.hzpd.http.AdSave;
import com.hzpd.library.AdSDK;
import com.hzpd.push.PushService;

public class WallListadapter extends BaseAdapter {
	private BitmapUtils bitmapUtils;
	private JSONArray array;
	private Context context;
	
	private DisplayMetrics dm; 
	private GradientDrawable mDrawable;
	private GradientDrawable mDrawable2;
	private BitmapDrawable bd;
	
	private String place_id;
	
	
	public WallListadapter(Context context,DisplayMetrics dm,String place_id){
		array=new JSONArray();
		this.context=context;
		bitmapUtils=new BitmapUtils(this.context);
		this.dm=dm;
		this.place_id=place_id;
		
		File f2=new File(AdSave.sdcardPath+"scorebg.png");
		if(f2.exists()){
			Bitmap bm=BitmapFactory.decodeFile(f2.getAbsolutePath());
			bd=new BitmapDrawable(bm);
		}
		//###########
		Path mPath = new Path();  
		Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);  
	  
		mDrawable = new GradientDrawable();  
	    mDrawable.setShape(GradientDrawable.RECTANGLE);  
	    mDrawable.setStroke(dip2px(1, dm.density), Color.parseColor("#2c9ad9"));
	    mDrawable.setColor(Color.parseColor("#2c9ad9"));
	    mDrawable.setCornerRadius(dip2px(5, dm.density));
	    
	    mDrawable2 = new GradientDrawable();  
	    mDrawable2.setShape(GradientDrawable.RECTANGLE);  
	    mDrawable2.setStroke(dip2px(1, dm.density), Color.parseColor("#187ab2"));
	    mDrawable2.setColor(Color.parseColor("#187ab2"));
	    mDrawable2.setCornerRadius(dip2px(5, dm.density));
			    
	}
	
	public void setData(JSONArray array){
		LogUtils.i("setData-->"+array.size()+"--"+array.toJSONString());
		
		this.array=array;
		//上传展示行为
		for(int i=0;i<array.size();i++){
			JSONObject obj=array.getJSONObject(i);
			onAdShowed(obj, place_id);
		}
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		if(array==null) return 0;
		return array.size();
	}

	@Override
	public Object getItem(int position) {
		return array.getJSONObject(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if(convertView==null){
			holder=new ViewHolder();
			convertView=getLayout(holder);
			convertView.setTag(holder);
		}else{
			holder=(ViewHolder) convertView.getTag();
			
		}
		
		final JSONObject obj=array.getJSONObject(position);
		final JSONObject adobj=obj.getJSONObject("ad");
		
		bitmapUtils.display(holder.imgv1, adobj.getString("offer_app"));
		holder.tv1.setText(adobj.getString("title"));
		holder.tv2.setText(adobj.getString("subtitle"));
		holder.tv3.setText("+"+adobj.getString("offer_app_credit"));
		holder.tv3.setTextColor(Color.parseColor("#666666"));
//		if("indirect".equals(adobj.getJSONObject(adobj.getString("effect")).getString("download_mode"))){
//			//非直接下载
//			holder.tv4.setVisibility(View.INVISIBLE);
//		}else{
		String ii=adobj.getString("id");
		LogUtils.i("ii--->"+ii);
		if(AdSDK.getInstance().isAddedAd(ii)){
			holder.tv4.setClickable(false);
			holder.tv4.setText("正在下载");
		}else{
			holder.tv4.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					//下载事件
					Intent service =new Intent(context, PushService.class); // 
		        	service.putExtra("id", adobj.getInteger("id"));
		        	service.putExtra("obj",adobj.toJSONString());
		        	service.putExtra("place_id", place_id);
		        	service.putExtra("score", adobj.getString("offer_app_credit"));
		        	
		        	service.setAction(PushService.downloadAction);
		        	context.startService(service);
		        	
		        	v.setClickable(false);
					((TextView)v).setText("正在下载");
				}
			});
			holder.tv4.setText("直接下载");
		}
//		}
		convertView.setBackgroundColor(Color.parseColor("#fafafa"));
		return convertView;
	}
	
	private static class ViewHolder{
		ImageView imgv1;//
		TextView tv1;//
		TextView tv2;//
		
		TextView tv3;//
		TextView tv4;//
	}
	
	private View getLayout(ViewHolder holder){
		
		android.widget.AbsListView.LayoutParams params=new android.widget.AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		LinearLayout item_root=new LinearLayout(context);
		item_root.setOrientation(LinearLayout.VERTICAL);
		item_root.setLayoutParams(params);
		
		LayoutParams params_top=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		LinearLayout item_layout=new LinearLayout(context);
		int p=dip2px(10, dm.density);
		item_layout.setPadding(p, p, p, p);
		item_layout.setLayoutParams(params_top);
		item_layout.setOrientation(LinearLayout.HORIZONTAL);
		
		item_root.addView(item_layout);
		//--------------divider liner-------------------
		View v=new View(context);
		LayoutParams divider_line=new LayoutParams(LayoutParams.MATCH_PARENT,1);
		divider_line.setMargins(dip2px(5, dm.density), 0, dip2px(5, dm.density), 0);
		v.setLayoutParams(divider_line);
		v.setBackgroundColor(Color.parseColor("#acacac"));
		item_root.addView(v);
		
		//-------------left-----------------------
		holder.imgv1=new ImageView(context);
		LayoutParams params1=new LayoutParams(dip2px(70, dm.density), dip2px(70, dm.density));
		holder.imgv1.setLayoutParams(params1);
		
		item_layout.addView(holder.imgv1);
		
		//-----------middle-------------------------
		LayoutParams params2=new LayoutParams(0, LayoutParams.WRAP_CONTENT);
		params2.weight=1;
		params2.setMargins(dip2px(8, dm.density), 0, dip2px(8, dm.density), 0);
		LinearLayout middle_layout=new LinearLayout(context);
		middle_layout.setOrientation(LinearLayout.VERTICAL);
		middle_layout.setGravity(Gravity.CENTER_VERTICAL);
		middle_layout.setLayoutParams(params2);
		
		item_layout.addView(middle_layout);
		
		//----------------text--------------------
		
		LayoutParams patv1=new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				
		holder.tv1=new TextView(context);
		holder.tv1.setTextSize(dip2px(8, dm.density));
		holder.tv1.setTextColor(Color.BLACK);
		holder.tv1.setLayoutParams(patv1);
		middle_layout.addView(holder.tv1);
		
		LayoutParams patv2=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		patv2.setMargins(0, dip2px(6, dm.density), 0, 0);
		holder.tv2=new TextView(context);
		holder.tv2.setTextSize(dip2px(7, dm.density));
		holder.tv2.setTextColor(Color.parseColor("#acacac"));
		holder.tv2.setMaxLines(2);
		
		holder.tv2.setLayoutParams(patv2);
		middle_layout.addView(holder.tv2);
		
		//-------------right-----------------------
		LayoutParams params3=new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		LinearLayout right_layout=new LinearLayout(context);
		right_layout.setOrientation(LinearLayout.VERTICAL);
		right_layout.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);
		right_layout.setLayoutParams(params3);
		
		item_layout.addView(right_layout);
		
		
		//--------------right text----------------------
		holder.tv3=new TextView(context);
		int dp10=dip2px(10, dm.density);
		LayoutParams patv3=new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		holder.tv3.setPadding(dip2px(15, dm.density), 0,dp10,0);
		
		holder.tv3.setTextColor(Color.WHITE);
		holder.tv3.setTextSize(dip2px(8, dm.density));
		if(bd!=null){
			holder.tv3.setBackgroundDrawable(bd);
		}
		holder.tv3.setLayoutParams(patv3);
		
		
		LayoutParams patv4=new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		patv4.setMargins(0, dp10, 0, 0);
		holder.tv4=new TextView(context);
		
		holder.tv4.setPadding(dp10/2,dp10/2,dp10/2,dp10/2);
		holder.tv4.setTextColor(Color.WHITE);
		holder.tv4.setText("直接下载");
		holder.tv4.setTextSize(dip2px(8, dm.density));
		holder.tv4.setLayoutParams(patv4);
		
		right_layout.addView(holder.tv3);
		right_layout.addView(holder.tv4);
		//------------------------------------
	    
	    holder.tv4.setBackgroundDrawable(mDrawable);
		//###########
	    holder.tv4.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					v.setBackgroundDrawable(mDrawable2);
					LogUtils.i("ACTION_DOWN");
					break;
				case MotionEvent.ACTION_UP:
					v.setBackgroundDrawable(mDrawable);
					LogUtils.i("ACTION_UP");
					break;
				}
				return false;
			}
		});
				
		return item_root;
	}

	public int dip2px(float dipValue, float scale) {
		return (int) (dipValue * scale + 0.5f);
	}
	

	//展示
	private void onAdShowed(JSONObject myad,String place_id){
		UserBehavior ub=AdSDK.getInstance().getUserBehavior("adshowed", myad.getJSONObject("ad").getString("id"), place_id);
		AdSDK.getInstance().uploadUserBehavior(ub);
	}

}
