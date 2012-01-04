package com.BeetelRockBattery;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.BeetelRockBattery.provider.BatteryConfigurationProvider;

public class BatteryWidget extends AppWidgetProvider {
	
	public static final String TAG = "BatteryWidget";
	
	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);		
		Logger.log(TAG, "enabling battery");
	}
	
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		Logger.log(TAG, "disabling battery");
	}


	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
        int[] appWidgetIds) {
        // To prevent any ANR timeouts, we perform the update in a service
		
		String ids = "";		
		for(int id : appWidgetIds) {
			ids += id + ", "; 
		}
		Logger.log(TAG, "Got update for: " + ids);
        context.startService(new Intent(context, UpdateService.class));
    }

    public static class UpdateService extends Service {

    	private Logger logger = null;
    	private BroadcastReceiver mBatteryStatusReceiver = null;
    	
    	@Override
		public void onCreate() {
    		Logger.log(TAG, "Creating new Service");
    		logger = Logger.getLogger(this);
			super.onCreate();
			
			if(mBatteryStatusReceiver == null) {
        		Logger.log(TAG, "Register battery receiver");
				mBatteryStatusReceiver = new BroadcastReceiver() {
					
					@Override
					public void onReceive(Context context, Intent intent) {
						Logger.log(TAG, "receieved battery status: " + intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0));
						buildAndPushUpdate(context, intent);
					}
				};
				registerReceiver(mBatteryStatusReceiver,
						new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        	}
			
			PendingIntent intent = PendingIntent.getBroadcast(this, 0, new Intent(RefreshReceiver.REFRESH_ACTION), PendingIntent.FLAG_UPDATE_CURRENT);
			AlarmManager alm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
			alm.set(AlarmManager.RTC, System.currentTimeMillis() + RefreshReceiver.THRESHOLD, intent);
			
		}
    	
    	@Override
		public void onDestroy() {
			super.onDestroy();
			Logger.log(TAG, "Removing the KBatteryStatusReceiver");
			if(mBatteryStatusReceiver != null) {
				unregisterReceiver(mBatteryStatusReceiver);
			}
			mBatteryStatusReceiver = null;
		}

        /*@Override
        public void onStart(Intent intent, int startId) {
        	//Logger.log(TAG, "OnStart for Id = " + startId);
			mBatteryStatusReceiver = new BroadcastReceiver() {
				
				@Override
				public void onReceive(Context context, Intent intent) {
					//Logger.log(TAG, "receieved battery status: " + intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0));
					buildAndPushUpdate(context, intent);
				}
			};
			registerReceiver(mBatteryStatusReceiver,
					new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }*/

        @Override
		public int onStartCommand(Intent intent, int flags, int startId) {
        	Logger.log(TAG, "OnStartCommand for Id = " + startId);
        	if(mBatteryStatusReceiver == null) {
        		Logger.log(TAG, "Register battery receiver");
				mBatteryStatusReceiver = new BroadcastReceiver() {
					
					@Override
					public void onReceive(Context context, Intent intent) {
						Logger.log(TAG, "receieved battery status: " + intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0));
						buildAndPushUpdate(context, intent);
					}
				};
				registerReceiver(mBatteryStatusReceiver,
						new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        	}
			super.onStartCommand(intent, flags, startId);
			return START_STICKY;
		}

		public static void buildAndPushUpdate(Context context, Intent intent) {
			Logger.log(TAG, "buildAndPushUpdate");
            RemoteViews updateViews = null;
            ContentValues values = new ContentValues();
            Settings settings = BatteryConfigurationProvider.loadSettings(context);
            
            updateViews = new RemoteViews(context.getPackageName(), R.layout.battery_widget_layout);
            
            boolean isCharging = false;
            if(intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0) {
            	if(intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN) != BatteryManager.BATTERY_STATUS_FULL) {
            		updateViews.setViewVisibility(R.id.battery_charging, View.VISIBLE);
            		isCharging = true;
            		Logger.log(TAG, "isCharging");
            	}
            	else {
            		updateViews.setViewVisibility(R.id.battery_charging, View.GONE);
            		isCharging = false;
            	}
            }
            else {
            	updateViews.setViewVisibility(R.id.battery_charging, View.GONE);
            	isCharging = false;
            }
            
            int percentage = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 100);
            updateViews.setTextViewText(R.id.percentage, "" + percentage + "%");
            Logger.log(TAG, percentage + "%");
            //updateViews.setTextColor(R.id.percentage, settings.mTextColor);
            
            int level = BatteryValuesUtils.normalizeBatteryLevel(percentage);
            updateViews.setImageViewResource(R.id.battery_icon, getBatteryIcon(level, settings));
            Logger.log(TAG, "Level: " + percentage);
        	
        	Intent detail = new Intent(context, BatteryDetail.class);
        	PendingIntent detailBattery = PendingIntent.getActivity(context, 0, detail, 0);
        	
        	updateViews.setOnClickPendingIntent(R.id.battery, detailBattery);
        	
            // Push update for this widget to the home screen
            ComponentName thisWidget = new ComponentName(context, BatteryWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            manager.updateAppWidget(thisWidget, updateViews);
        }

		@Override
        public IBinder onBind(Intent intent) {
            // We don't need to bind to this service
            return null;
        }
    }

    
    public static int getBatteryIcon(int level, Settings settings) {
    	
    	Integer batteryGreen[] = {
    		R.drawable.battery_green_30,
    		R.drawable.battery_green_40,
    		R.drawable.battery_green_50,
    		R.drawable.battery_green_60,
    		R.drawable.battery_green_70,
    		R.drawable.battery_green_80,
    		R.drawable.battery_green_90,
    		R.drawable.battery_green_100,
    	};
    	
    	Integer batteryBlue[] = {
        		R.drawable.battery_blue_30,
        		R.drawable.battery_blue_40,
        		R.drawable.battery_blue_50,
        		R.drawable.battery_blue_60,
        		R.drawable.battery_blue_70,
        		R.drawable.battery_blue_80,
        		R.drawable.battery_blue_90,
        		R.drawable.battery_blue_100,
        	};
    	Integer batteryLightBlue[] = {
        		R.drawable.battery_light_blue_30,
        		R.drawable.battery_light_blue_40,
        		R.drawable.battery_light_blue_50,
        		R.drawable.battery_light_blue_60,
        		R.drawable.battery_light_blue_70,
        		R.drawable.battery_light_blue_80,
        		R.drawable.battery_light_blue_90,
        		R.drawable.battery_light_blue_100,
        	};
    	
    	Integer batteryPink[] = {
        		R.drawable.battery_pink_30,
        		R.drawable.battery_pink_40,
        		R.drawable.battery_pink_50,
        		R.drawable.battery_pink_60,
        		R.drawable.battery_pink_70,
        		R.drawable.battery_pink_80,
        		R.drawable.battery_pink_90,
        		R.drawable.battery_pink_100,
        	};
    	
    	Integer batteryOrange[] = {
        		R.drawable.battery_orange_30,
        		R.drawable.battery_orange_40,
        		R.drawable.battery_orange_50,
        		R.drawable.battery_orange_60,
        		R.drawable.battery_orange_70,
        		R.drawable.battery_orange_80,
        		R.drawable.battery_orange_90,
        		R.drawable.battery_orange_100,
        	};
    	
    	if(level == 0)
    		return R.drawable.battery_empty;
    	else if(level == 5)
    		return R.drawable.battery_5;
    	else if(level == 10)
    		return R.drawable.battery_10;
    	else if(level == 20)
    		return R.drawable.battery_20;
    	else {
    		//return batteryMap[settings.mBattery][level/10-3];
    		switch(settings.mBattery) {
    		case BatteryConfigurationProvider.BATTERY_BLUE:
    			return batteryBlue[level/10-3];
    		case BatteryConfigurationProvider.BATTERY_LIGHT_BLUE:
    			return batteryLightBlue[level/10-3];
    		case BatteryConfigurationProvider.BATTERY_PINK:
    			return batteryPink[level/10-3];
    		case BatteryConfigurationProvider.BATTERY_ORANGE:
    			return batteryOrange[level/10-3];
    		default:
    			return batteryGreen[level/10-3];
    		}
    	}
    }

}
