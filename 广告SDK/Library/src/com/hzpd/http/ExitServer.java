package com.hzpd.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.color.myxutils.HttpUtils;
import com.color.myxutils.exception.HttpException;
import com.color.myxutils.http.RequestParams;
import com.color.myxutils.http.ResponseInfo;
import com.color.myxutils.http.callback.RequestCallBack;
import com.color.myxutils.http.client.HttpRequest;
import com.color.myxutils.util.LogUtils;
import com.hzpd.bean2.Ad;
import com.hzpd.utils.Code;

public class ExitServer {
	private HttpClient httpClient;
	private Context context;
	private Handler handler;
	private String key;
	
	public ExitServer(Context context,Handler handler,String key){
		httpClient = new DefaultHttpClient(); 
		this.context=context;
		this.handler=handler;
		this.key=key;	
	}
	
	
	public void postData(){
		//��GET��ʽһ�����Ƚ���������List
		HttpUtils http = new HttpUtils();
		RequestParams params = new RequestParams();
		params.addBodyParameter("place_id", key);
		
		http.send(HttpRequest.HttpMethod.POST,
			Code.url,
		    params,
		    new RequestCallBack<String>(){
		        @Override
		        public void onLoading(long total, long current, boolean isUploading) {
		        }
		        @Override
		        public void onSuccess(ResponseInfo<String> responseInfo) {
		        	LogUtils.i(responseInfo.result);
				    Ad ad = null;
				    try{
						ad=JSONObject.parseObject(responseInfo.result,Ad.class);	
					    Log.i("","��װbean��ɣ�");
					    postImg(ad.getAd().getImage());
					    Message msg=handler.obtainMessage();
		                msg.what=Code.barAd;
		                msg.obj=ad;
		                handler.sendMessage(msg);
				    }catch(Exception e){
				    	Log.i("","��վ�ҵ��ˣ�");
				    }
		        }
		        @Override
		        public void onStart() {
		        }
		        @Override
		        public void onFailure(HttpException error, String msg) {
		        }
		});
	}
	
	private void postImg(String url){
		try {
			HttpGet httpget = new HttpGet(url);
		    HttpResponse response = httpClient.execute(httpget); //ִ��POST����
		    int resCode=response.getStatusLine().getStatusCode();
		    Log.i("", "pic resCode = " + resCode); //��ȡ��Ӧ��
		    if(HttpStatus.SC_OK==resCode){
		    	InputStream is = response.getEntity().getContent();
                byte[] bytes = new byte[1024];
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int count = 0;
                while ((count = is.read(bytes)) != -1) {
                        bos.write(bytes, 0, count);
                }
                byte[] byteArray = bos.toByteArray();
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0,
                                byteArray.length);
                Message msg=handler.obtainMessage();
                msg.what=Code.barAdImg;
                msg.obj=bitmap;
                handler.sendMessage(msg);
		    }
		} catch (UnsupportedEncodingException e) {
		    e.printStackTrace();
		} catch (ClientProtocolException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
}
