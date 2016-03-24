package com.hzpd.wall;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.color.myxutils.HttpUtils;
import com.color.myxutils.exception.HttpException;
import com.color.myxutils.http.ResponseInfo;
import com.color.myxutils.http.callback.RequestCallBack;
import com.color.myxutils.util.LogUtils;
import com.hzpd.bean3.UserBehavior;
import com.hzpd.http.AdSave;
import com.hzpd.library.AdSDK;
import com.hzpd.push.AdDetailActivity;
import com.hzpd.push.PushService;
import com.hzpd.utils.Code;

public class WallActivity extends Activity {
	public static final String img_back_URL="http://wisead.cn/assets/static/back.png";
	public static final String scorebg_URL="http://wisead.cn/assets/static/scorebg.png";
	public static final String LIST_UPDATE="list.update";
	
	private LinearLayout root;
	private ListView listview;
	private WallListadapter adapter;
	private String wallplace_id;
	private MyAsync myAsync;
	private DisplayMetrics dm;
	private ImageView img_back;
	private HttpUtils httpUtils;
	
	private MylistUpdateBroadcast mb;//
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Intent intent=getIntent();
		wallplace_id=intent.getStringExtra("place_id");
		
		root=getLayout();
		
		setContentView(root);
		httpUtils=new HttpUtils();
		
		mb=new MylistUpdateBroadcast();
		IntentFilter filter=new IntentFilter();
		filter.addAction(LIST_UPDATE);
		this.registerReceiver(mb, filter);
		
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
		
		File f2=new File(AdSave.sdcardPath+"scorebg.png");
		if(!f2.exists()){
			httpUtils.download(scorebg_URL
				, AdSave.sdcardPath+"scorebg.png"
				, new RequestCallBack<File>() {
				@Override
				public void onSuccess(ResponseInfo<File> responseInfo) {
					
				}
				@Override
				public void onFailure(HttpException error, String msg) {
					
				}
			});
		}
		
		img_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		myAsync=new MyAsync();
		myAsync.execute(wallplace_id);
		
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//itemclick
				
				JSONObject obj=(JSONObject) adapter.getItem(position);
				JSONObject adobj=obj.getJSONObject("ad");
				JSONObject download=adobj.getJSONObject(adobj.getString("effect"));
				try{
					if("indirect".equals(download.getString("download_mode"))){
						Intent intent =new Intent(WallActivity.this, AdDetailActivity.class); // 
						intent.putExtra("object", adobj.toJSONString());
						intent.putExtra("place_id", wallplace_id);
						
						intent.setAction(PushService.DetailAction);
						startActivity(intent);
					}
				}catch(Exception e){
					//TODO
				}
				LogUtils.i("-->"+adobj.toJSONString());
				
				onViewClick(adobj, wallplace_id);
				
			}
		});
		
		
	}

	private LinearLayout getLayout(){
		
		dm=getResources().getDisplayMetrics();
		
		LayoutParams paramsrll=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		
		LinearLayout l_root=new LinearLayout(this);
		l_root.setLayoutParams(paramsrll);
		l_root.setOrientation(LinearLayout.VERTICAL);
		l_root.setBackgroundColor(Color.WHITE);
		
		
		//----------------------------------------------------------
		int w= dip2px(50, dm.density);
		RelativeLayout llHead=new RelativeLayout(this);
		LayoutParams paramsllhead=new LayoutParams(LayoutParams.MATCH_PARENT, w);
		llHead.setLayoutParams(paramsllhead);
		llHead.setBackgroundColor(Color.parseColor("#2c9ad9"));
		l_root.addView(llHead);
		//--
		TextView title_tv=new TextView(this);
		title_tv.setTextSize(dip2px(11, dm.density));
		title_tv.setTextColor(Color.WHITE);
		title_tv.setText("免费获取积分");
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
		
		//------------------------------------------
		LayoutParams paramslist=new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		listview=new ListView(this);
		
		listview.setDivider(null);
		listview.setDividerHeight(0);
		listview.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		
		listview.setCacheColorHint(Color.TRANSPARENT);
		listview.setBackgroundColor(Color.TRANSPARENT);
		listview.setLayoutParams(paramslist);
		
		adapter=new WallListadapter(this,dm,wallplace_id);
		listview.setAdapter(adapter);
		
		l_root.addView(listview);
		
		//------------------------------------------
		
		return l_root;
	}
	
	private class MyAsync extends AsyncTask<String, String, JSONArray>{
		@Override
		protected JSONArray doInBackground(String... params) {
			JSONArray array = null;
			
			StringBuilder sb=new StringBuilder();
			
			sb.append("place_id=");
			sb.append(wallplace_id);
			
			HttpURLConnection connection = null;
	        URL url;
			StringBuilder result =new StringBuilder();
			try {
			  	url = new URL(Code.url);
	            connection = (HttpURLConnection) url.openConnection();
	            connection.setRequestMethod("POST");
	            connection.setDoInput(true);
	            connection.setDoOutput(true);
	            connection.setUseCaches(false);
	            connection.setConnectTimeout(5000);

	            connection.setRequestProperty("Content-Type",
	                    "application/x-www-form-urlencoded");

	            OutputStreamWriter osw = new OutputStreamWriter(
	                    connection.getOutputStream(), "UTF-8");
	            osw.write(sb.toString());
	            osw.flush();
	            osw.close();

	            BufferedReader br = new BufferedReader(new InputStreamReader(
	                    connection.getInputStream(), "UTF-8"));
	            String temp;
	            while ((temp = br.readLine()) != null) {
	                result.append(temp);
	            }
			} catch (Exception e){
				e.printStackTrace();
			}finally{
				if (connection != null) {
	                connection.disconnect();
	            }
			}
			LogUtils.i("Wallactivity--->"+result.toString());
			try{
				JSONObject object=JSONObject.parseObject(result.toString());
				if(0==object.getIntValue("error_code")){
					array=object.getJSONArray("data");
					
//					for(int i=0;i<array.size();i++){
//						JSONObject o=array.getJSONObject(i);
//						JSONObject oo=o.getJSONObject("ad");
//						PushBean pb=new PushBean();
//						pb.setObject(oo);
//						pb.setPlace_id(place_id);
////						pushbean.put(oo.getInteger("id"), pb);
////						//
////						notifycation(oo,place_id);
//					}
				}
			}catch(Exception e){
				LogUtils.i("getWall failed!");
			}
			
			return array;
		}

		@Override
		protected void onPostExecute(JSONArray result) {
			if(result==null){
				return;
			}
			adapter.setData(result);
		}
	}
	
	public int dip2px(float dipValue, float scale) {
		return (int) (dipValue * scale + 0.5f);
	}

	@Override
	protected void onResume() {
		super.onResume();
		listview.setAdapter(adapter);
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mb);
		super.onDestroy();
	}
	
	public class MylistUpdateBroadcast extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			LogUtils.i("--update--");
			if(adapter!=null){
				LogUtils.i("update");
				adapter.notifyDataSetChanged();
			}
		}
	}
	
	private void onViewClick(JSONObject myad,String place_id){
		if(myad==null) return;
		UserBehavior ub=AdSDK.getInstance().getUserBehavior("adclick", myad.getString("id"), place_id);
		AdSDK.getInstance().uploadUserBehavior(ub);
	}
	
}


