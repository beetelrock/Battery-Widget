package com.BeetelRockBattery;

import android.graphics.Color;

import com.BeetelRockBattery.provider.BatteryConfigurationProvider;

public class Settings {
	public int mBattery = BatteryConfigurationProvider.BATTERY_GREEN;
	public int mbackground = BatteryConfigurationProvider.BACKGROUND_DEFAULT;
	public int mTextColor = Color.rgb(255, 255, 255);
	public Settings() {
		
	}
}
