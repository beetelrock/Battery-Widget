package com.BeetelRockBattery.provider;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;

import com.BeetelRockBattery.Settings;

public class BatteryConfigurationProvider {

	public static final String BATTERY_CONFIG_SETTINGS = "BeetelRockBatterySettings";
	public static final String BATTERY_CONFIG_BATTERY = "BRBW_battery";
	public static final String BATTERY_CONFIG_BACK = "BRBW_background";
	public static final String BATTERY_CONFIG_TEXT = "BRBW_text_color";
	
	public static final int BATTERY_GREEN = 0;
	public static final int BATTERY_BLUE = 1;
	public static final int BATTERY_LIGHT_BLUE = 2;
	public static final int BATTERY_PINK = 3;
	public static final int BATTERY_ORANGE = 4;
		
	public static final int BACKGROUND_DEFAULT = 0;
	public static final int BACKGROUND_CHOCO = 1;
	public static final int BACKGROUND_SLATE = 2;
	public static final int BACKGROUND_DAWN = 3;
	public static final int BACKGROUND_AQUA = 4;
	public static final int BACKGROUND_GOLD = 5;
	public static final int BACKGROUND_SILVER = 6;
	public static final int BACKGROUND_GREEN = 7;
	public static final int BACKGROUND_SUN_DOWN = 8;

	
	public static Settings loadSettings(Context context) {
		Settings settings = new Settings();
		SharedPreferences prefs = context.getSharedPreferences(BATTERY_CONFIG_SETTINGS, Context.MODE_PRIVATE);
		
		if(prefs != null) {
			settings.mBattery = prefs.getInt(BATTERY_CONFIG_BATTERY, BATTERY_GREEN);
			settings.mbackground = prefs.getInt(BATTERY_CONFIG_BACK, BACKGROUND_DEFAULT);
			settings.mTextColor = prefs.getInt(BATTERY_CONFIG_TEXT, Color.rgb(255, 255, 255));
			//Log.e("KAMAL", "color = " + settings.mTextColor);
		}
		return settings;
	}
	
	public static void saveSettings(Context context, Settings settings) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(BATTERY_CONFIG_SETTINGS, Context.MODE_PRIVATE).edit();
		
		if(prefs != null) {
			prefs.putInt(BATTERY_CONFIG_BATTERY, settings.mBattery);
			prefs.putInt(BATTERY_CONFIG_BACK, settings.mbackground);
			prefs.putInt(BATTERY_CONFIG_TEXT, settings.mTextColor);
			
			//Log.e("KAMAL", "color = " + settings.mTextColor);
			prefs.commit();
		}
	}
}
