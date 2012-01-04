package com.BeetelRockBattery;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.LevelListDrawable;
import android.text.format.Time;

import com.BeetelRockBattery.provider.BRProvider;
import com.BeetelRockBattery.provider.BRProvider.BatteryColumns;
import com.BeetelRockBattery.provider.BatteryConfigurationProvider;

public class BatteryValuesUtils {
	static void saveBatteryValues(Context ctx, ContentValues values) {
		try {			
			recycleBatteryValues(ctx);
			
			BRProvider provider = BRProvider.getProvider(ctx);
			provider.insert(BatteryColumns.CONTENT_URI, values);
			provider = null;
			BRProvider.releaseProvider();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static Cursor loadBatteryValue(Context ctx) {
		try {
			BRProvider provider = BRProvider.getProvider(ctx);
			return provider.query(BatteryColumns.CONTENT_URI, null, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			BRProvider.releaseProvider();
		}
		return null;
	}
	
	static void recycleBatteryValues(Context ctx) {
		try {
			BRProvider provider = BRProvider.getProvider(ctx);
			Time timeNow = new Time();
			timeNow.setToNow();
			timeNow.hour = timeNow.minute = timeNow.second = 0;
			provider.delete(BatteryColumns.CONTENT_URI, BatteryColumns.DATE_TIME + "<" + timeNow.toMillis(false));
			provider = null;
			BRProvider.releaseProvider();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static public int normalizeBatteryLevel(int percentage) {
			
        int level = 0;
    	if(percentage > 0 && percentage <= 5) {
    		level = 5;
        }
    	else if(percentage > 5 && percentage < 10) {
    		level = 5;
        }
        else if(percentage >= 10 && percentage < 20) {
        	level = 10;
        }
        else if(percentage >= 20 && percentage < 30) {
        	level = 20;
        }
        else if(percentage >= 30 && percentage < 40) {
        	level = 30;
        }
        else if(percentage >= 40 && percentage < 50) {
        	level = 40;
        }
        else if(percentage >= 50 && percentage < 60) {
        	level = 50;
        }
        else if(percentage >= 60 && percentage < 70) {
        	level = 60;
        }
        else if(percentage >= 70 && percentage < 80) {
        	level = 70;
        }
        else if(percentage >= 80 && percentage < 90) {
        	level = 80;
        }
        else if(percentage >= 90 && percentage < 100) {
        	level = 90;
        } 
        else if(percentage == 100) {
        	level = 100;
        }
        else {
        	level = 0;
        }
    	
    	return level;
	}
	
	public static LevelListDrawable getLevelList(Context context, int percentage, int battery) {
		
		int batteryLevelList = R.drawable.battery_charge_level;
		
		switch(battery) {
		case BatteryConfigurationProvider.BATTERY_BLUE:
			batteryLevelList = R.drawable.battery_blue_charge_level;
			break;
		case BatteryConfigurationProvider.BATTERY_LIGHT_BLUE:
			batteryLevelList = R.drawable.battery_light_blue_charge_level;
			break;
		case BatteryConfigurationProvider.BATTERY_PINK:
			batteryLevelList = R.drawable.battery_pink_charge_level;
			break;
		case BatteryConfigurationProvider.BATTERY_ORANGE:
			batteryLevelList = R.drawable.battery_orange_charge_level;
			break;
		}
		
		LevelListDrawable levelListDrawable = (LevelListDrawable)context.getResources().getDrawable(batteryLevelList);
		levelListDrawable.setLevel(normalizeBatteryLevel(percentage));
		return levelListDrawable;
	}
}
