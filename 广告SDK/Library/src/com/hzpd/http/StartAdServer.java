package com.hzpd.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.color.myxutils.HttpUtils;
import com.color.myxutils.exception.HttpException;
import com.color.myxutils.http.ResponseInfo;
import com.color.myxutils.http.callback.RequestCallBack;
import com.color.myxutils.util.LogUtils;
import com.hzpd.library.AdSDK;
import com.hzpd.utils.Code;
import com.hzpd.utils.PhoneUtils;

public class StartAdServer extends Thread {
	private Context context;
	private String key;
	
	public StartAdServer(Context context,String key){
	
		this.context=context;
		this.key=key;
	}
	
	private void post(){
		LogUtils.i("server-->"+key);
//		---------------------------
		StringBuilder sb=new StringBuilder();
		sb.append("place_id=");
		sb.append(key);
		sb.append("&");
		sb.append("network=");
		sb.append(PhoneUtils.getNetTypeName(context));
		
//		sb.append("&");
//		sb.append("style_type=");
//		sb.append("");
		
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
            connection.setConnectTimeout(3000);

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
		} finally{
			if (connection != null) {
                connection.disconnect();
            }
		}
		
	   String json=result.toString();
	   LogUtils.i("pop-ad->"+ json);
	   if(TextUtils.isEmpty(json)){
		   return;
	   }
	   JSONObject jo=null;
	   try {
		   jo=JSONObject.parseObject(json);
		} catch (Exception e) {
			return;
		}
	   
	   if(jo.getIntValue("error_code")!=0){
//		   Toast.makeText(context, ""+jo.getString("error_msg"), Toast.LENGTH_SHORT).show();
		   LogUtils.i(jo.getString("error_msg"));
		   AdSDK.getInstance().saveStartPlaceId(null);
		   return;
	   }
	   JSONObject data=jo.getJSONObject("data");
	    
       if(AdSave.saveAd(context, data,key)){
	    	LogUtils.i("ad string saved time:"+data.getJSONObject("control").getString("time_duration"));
	    }
       
       if(data.getJSONObject("ad") == null){
    	   AdSDK.getInstance().saveStartPlaceId(null);
    	   return;
       } 
       
	    String content=data.getJSONObject("ad").getString("content");
	    String da=data.getJSONObject("ad").getString(content);

	    String suffix[]=da.split("\\.");
	    String suff=suffix[suffix.length-1];
	    if(!"video".equals(content)){
	    	postData(da,AdSave.sdcardPath+key+"."+suff);
	    }else{
	    	LogUtils.i("content--->"+content);
	    	AdSDK.getInstance().saveStartPlaceId(key);
	    }
	}

	private void postData(final String url,String fileName){
		LogUtils.i("url-->"+url);
		LogUtils.i("fileName-->"+fileName);
		HttpUtils http = new HttpUtils();
		http.download(url,
			fileName,
		    false, 
		    false, 
		    new RequestCallBack<File>() {
				@Override
				public void onSuccess(ResponseInfo<File> responseInfo) {
					LogUtils.i("download success");
					AdSDK.getInstance().saveStartPlaceId(key);
				}
				@Override
				public void onFailure(HttpException error, String msg) {
				}
		});
		
	}
	
	@Override
	public void run(){
		LogUtils.i("server start");
		if(key!=null){
			post();
		}
	}

}
