package com.hzpd.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.color.myxutils.util.LogUtils;

public class AppInstallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action=intent.getAction();
        LogUtils.i("AppInstallReceiver--->"+action);
        if (action.equals(Intent.ACTION_PACKAGE_ADDED)
        		||action.equals(Intent.ACTION_PACKAGE_REPLACED)) {
        	
            String packageName = intent.getData().getSchemeSpecificPart();
            Intent service=new Intent(context,PushService.class);
            service.setAction(PushService.installAction);
            service.putExtra("package", packageName);
            context.startService(service);
        }

    }

}
