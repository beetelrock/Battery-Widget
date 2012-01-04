package com.BeetelRockBattery;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.BeetelRockBattery.BatteryWidget.UpdateService;

public class RefreshReceiver extends BroadcastReceiver {

	public static final int THRESHOLD = 10 * 1000 * 60;
	public static final String REFRESH_ACTION = "com.BeetelRockBattery.REFRESH";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent != null) {
			if(intent.getAction() != null) {
				if(intent.getAction().equals(REFRESH_ACTION)) {
	
					context.startService(new Intent(context, UpdateService.class));					
					PendingIntent intentRefresh = PendingIntent.getBroadcast(context, 0, new Intent(RefreshReceiver.REFRESH_ACTION), PendingIntent.FLAG_UPDATE_CURRENT);
					AlarmManager alm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
					alm.set(AlarmManager.RTC, System.currentTimeMillis() + RefreshReceiver.THRESHOLD, intentRefresh);
				}
			}
		}
	}

}
