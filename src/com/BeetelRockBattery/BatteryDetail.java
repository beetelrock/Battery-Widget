package com.BeetelRockBattery;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.LevelListDrawable;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.BeetelRockBattery.provider.BatteryConfigurationProvider;

public class BatteryDetail extends Activity {

	private static final String GPS_ENABLED_CHANGE_ACTION = 
								"android.location.GPS_ENABLED_CHANGE";
	private static final String EXTRA_ENABLED = "enabled";
	
	private static final int REQUEST_GPS = 1;
	private static final int REQUEST_SYNC_SETTINGS = 2;

	
	private BroadcastReceiver mBatteryStatusReceiver = null;
	private TextView mLevel = null;
	private TextView mTemperature = null;
	private TextView mHealth = null;
	private TextView mVoltage = null;
	private TextView mTechnology = null;
	private AnimatedImageView mBattery = null;
	private ImageView mCharging = null;
	private ImageButton mCustomization = null;
	private ImageButton mMoreOptions = null;
	private STToggleButton mSync = null;
	
	private Settings mSettings = null;
	private boolean mIsCharging = false;
	private int mPercentage = 0;
	private Intent mPendingUpdate = null;
	
	private ProgressDialog mProgress = null;
	private STToggleButton mBTButton = null;
	private BluetoothAdapter mBtAdapter = null;
	private BroadcastReceiver mBTStateBroadCastRecv = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent != null) {
				if(intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
					int stateNow = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
							BluetoothAdapter.STATE_OFF);
					
					if(mProgress != null && 
							((stateNow == BluetoothAdapter.STATE_ON) ||
							 (stateNow == BluetoothAdapter.STATE_OFF)) ) {
						mProgress.dismiss();
						mBTButton.setState(stateNow == BluetoothAdapter.STATE_ON ? STToggleButton.ON : STToggleButton.OFF);
					}
				}
			}
		}
	};
	
	private LocationManager mLm = null;
	private STToggleButton mGPSButton = null;
	private GpsStatus.Listener mGpsListner = new GpsStatus.Listener() {
		
		@Override
		public void onGpsStatusChanged(int event) {
			switch(event) {
			case GpsStatus.GPS_EVENT_STARTED:
				mGPSButton.setState(STToggleButton.ON);
				break;
			case GpsStatus.GPS_EVENT_STOPPED:
				mGPSButton.setState(STToggleButton.OFF);
				break;
			}
		}
	};
	
	private STToggleButton mWifiButton = null;
	private WifiManager mWifiManager = null;
	private BroadcastReceiver mWifiStateReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			int stateNow = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
					WifiManager.WIFI_STATE_DISABLED);
			
			if(mProgress != null && 
					((stateNow == WifiManager.WIFI_STATE_DISABLED) ||
					 (stateNow == WifiManager.WIFI_STATE_ENABLED)) ) {
				mProgress.dismiss();
				mWifiButton.setState(stateNow == WifiManager.WIFI_STATE_ENABLED ? STToggleButton.ON : STToggleButton.OFF);
			}
		}
	};
	
	public static final String CELSIUS = (char)(0x00B0) + "C";
	public static final String FARENHEIT = (char)(0x00B0) + "F";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.battery_detail);
		
		mSettings = BatteryConfigurationProvider.loadSettings(this);
		
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		
		mBatteryStatusReceiver = new BroadcastReceiver() {		
			@Override
			public void onReceive(Context context, Intent intent) {
				receivedBatteryStatus(intent);
			}
		};
		registerReceiver(mBatteryStatusReceiver,
			new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		
		registerReceiver(mBTStateBroadCastRecv, 
			new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
		
		registerReceiver(mWifiStateReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
		
		mLm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		mProgress = new ProgressDialog(this);
		mProgress.setIndeterminate(true);
		mProgress.setCancelable(false);
		mProgress.setTitle(null);
		mProgress.setMessage(getResources().getString(R.string.wait));
		
		mLevel = (TextView)findViewById(R.id.level);
		mTemperature = (TextView)findViewById(R.id.temperature);
		mHealth = (TextView)findViewById(R.id.health);
		mVoltage = (TextView)findViewById(R.id.voltage);
		mTechnology = (TextView)findViewById(R.id.technology);
		mBattery = (AnimatedImageView)findViewById(R.id.battery_icon);
		mCharging = (ImageView)findViewById(R.id.battery_charging);
		
		updateSkin();
		
		mBTButton = (STToggleButton)findViewById(R.id.btn_bluetooth);
		mGPSButton = (STToggleButton)findViewById(R.id.btn_gps);
		mWifiButton = (STToggleButton)findViewById(R.id.btn_wifi);
		
		mCustomization = (ImageButton)findViewById(R.id.customize);
		
		mCustomization.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BatteryDetail.this.startActivity(new Intent(getApplicationContext(), BatteryConfigure.class));
			}
		});
		
		mBTButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				toogleBluetooth();		
			}
		});
		
		mGPSButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				toogleGps();				
			}
		});
		
		mWifiButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				toogleWifi();
			}
		});
		
		mMoreOptions = (ImageButton)findViewById(R.id.more_details);
		mMoreOptions.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onMoreOptions();
			}
		});
		
		mSync = (STToggleButton)findViewById(R.id.btn_sync);
		mSync.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onSync();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		mSettings = BatteryConfigurationProvider.loadSettings(this);
		checkRadioStatus();
		updateSkin();
		
		if(mIsCharging) {
	        setChargingIcon(mPercentage);
        	
        } else {
        	setDischargingIcon(mPercentage);
        }
		
	}

	private void updateSkin() {
		mLevel.setTextColor(mSettings.mTextColor);
		mTemperature.setTextColor(mSettings.mTextColor);
		mHealth.setTextColor(mSettings.mTextColor);
		mVoltage.setTextColor(mSettings.mTextColor);
		mTechnology.setTextColor(mSettings.mTextColor);
		
		((TextView)(findViewById(R.id.label_info))).setTextColor(mSettings.mTextColor);
		((TextView)(findViewById(R.id.label_health))).setTextColor(mSettings.mTextColor);
		((TextView)(findViewById(R.id.label_level))).setTextColor(mSettings.mTextColor);
		((TextView)(findViewById(R.id.label_tech))).setTextColor(mSettings.mTextColor);
		((TextView)(findViewById(R.id.label_temp))).setTextColor(mSettings.mTextColor);
		((TextView)(findViewById(R.id.label_voltage))).setTextColor(mSettings.mTextColor);
		
		//LinearLayout backSkin = (LinearLayout)findViewById(R.id.detail_background);
		//backSkin.setBackgroundResource(BatteryValuesUtils.getBackgroundSkin(mSettings));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(mPendingUpdate != null)
			BatteryWidget.UpdateService.buildAndPushUpdate(this, mPendingUpdate);
		if(mBatteryStatusReceiver != null) {
			unregisterReceiver(mBatteryStatusReceiver);
		}
		mBatteryStatusReceiver = null;
		
		if(mBTStateBroadCastRecv != null) {
			unregisterReceiver(mBTStateBroadCastRecv);
		}
		mBTStateBroadCastRecv = null;
		
		mLm.removeGpsStatusListener(mGpsListner);
		mGpsListner = null;
		
		if(mWifiStateReceiver != null) {
			unregisterReceiver(mWifiStateReceiver);
		}
		mWifiStateReceiver = null;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {		
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == REQUEST_GPS) {
			checkGps();
		}
	}


	private void receivedBatteryStatus(Intent intent) {
		mPendingUpdate = intent;
		if(intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0) {		
        	if(intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN) != BatteryManager.BATTERY_STATUS_FULL) {
        		mCharging.setVisibility(View.VISIBLE);
        		mIsCharging = true;
        	}
        	else {
        		mCharging.setVisibility(View.GONE);
        		mIsCharging = false;
        	}
        }
        else {
        	mCharging.setVisibility(View.GONE);
        	mIsCharging = false;
        }
        
		mPercentage = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 100);
        mLevel.setText(mPercentage + "%");
        
        if(mIsCharging) {
	        setChargingIcon(mPercentage);
        	
        } else {
        	setDischargingIcon(mPercentage);
        }
    	
    	int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0xdeadbeef);
    	if(temperature != 0xdeadbeef) {
    		temperature = temperature/10;
    		String temperatureString = (temperature + CELSIUS) + " / "
    									+ celsiusToFahrenheit(temperature) + FARENHEIT;
    		mTemperature.setText(temperatureString);
    	}
    	
    	int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 
    			BatteryManager.BATTERY_HEALTH_UNKNOWN);
    	
    	switch(health) {
    	case BatteryManager.BATTERY_HEALTH_DEAD:
    		mHealth.setText(R.string.dead);
    		break;
    	case BatteryManager.BATTERY_HEALTH_GOOD:
    		mHealth.setText(R.string.good);
    		break;
    	case BatteryManager.BATTERY_HEALTH_OVERHEAT:
    		mHealth.setText(R.string.over_heat);
    		break;
    	case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
    		mHealth.setText(R.string.over_voltage);
    		break;
		default:
			mHealth.setText(R.string.unknown);
			break;
    	}
    	
    	int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0xdeadbeef);
    	if(voltage != 0xdeadbeef) {
    		mVoltage.setText(voltage + " mV");
    	} else {
    		mVoltage.setText(R.string.unknown);
    	}
    	
    	String tech = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
    	if(tech != null && tech.length() != 0) {
    		mTechnology.setText(tech);
    	} else {
    		mTechnology.setText(R.string.unknown);
    	}
	}

	private void setChargingIcon(int percentage) {
		LevelListDrawable levelListDrawable = BatteryValuesUtils.getLevelList(this, percentage, mSettings.mBattery);
    	mBattery.setImageDrawable(levelListDrawable.getCurrent());
	}
	
	private void setDischargingIcon(int percentage) {
		mBattery.setImageResource(BatteryWidget.getBatteryIcon(BatteryValuesUtils.normalizeBatteryLevel(percentage), mSettings));
	}
	
	public static int fahrenheitToCelsius(int tFahrenheit) {
		return (int) ((5.0f / 9.0f) * (tFahrenheit - 32));
	}

	public static int celsiusToFahrenheit(int tCelsius) {
		return (int) ((9.0f / 5.0f) * tCelsius + 32);
	}
	
	private void toogleBluetooth() {
		if(mBtAdapter != null) {
			
			if(mBtAdapter.isEnabled()) {
				mBtAdapter.disable();
			} else {
				mBtAdapter.enable();
			}
			
			if(mProgress != null) {
				mProgress.show();
			}
		}	
	}
	
	private void toogleWifi() {
		if(mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		} else {
			mWifiManager.setWifiEnabled(true);
			mProgress.show();
		}
	}
	
	private void toogleGps() {
		/*
		Intent gps = new Intent(GPS_ENABLED_CHANGE_ACTION);
		if(mGPSButton.getState() == STToggleButton.ON) {
			gps.putExtra(EXTRA_ENABLED, false);
		} else {
			LocationManager lm =  (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			if(lm != null) {
				GpsStatus status = lm.getGpsStatus(null);
				gps.putExtra(EXTRA_ENABLED, status != null ? true : false);
			} else {
				mGPSButton.setState(STToggleButton.OFF);
			}
		}
		
		sendBroadcast(gps);
		*/
		
		Intent gps = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivityForResult(gps, REQUEST_GPS);
	}
	
	private void checkGps() {
		if(mLm != null) {
			mLm.addGpsStatusListener(mGpsListner);
			if(mLm.isProviderEnabled(LocationManager.GPS_PROVIDER))
				mGPSButton.setState(STToggleButton.ON);
			else
				mGPSButton.setState(STToggleButton.OFF);
		}
	}
	
	private void checkRadioStatus() {
		if(mBtAdapter != null) {
			mBTButton.setEnabled(true);
			if(mBtAdapter.isEnabled()) {
				mBTButton.setState(STToggleButton.ON);
			} else {
				mBTButton.setState(STToggleButton.OFF);
			}
		} else {
			mBTButton.setEnabled(false);
		}
		
		checkGps();
		
		if(mWifiManager.isWifiEnabled()) {
			mWifiButton.setState(STToggleButton.ON);
		} else {
			mWifiButton.setState(STToggleButton.OFF);
		}
	}
	
	private void onMoreOptions() {
		try {
			startActivity(new Intent(Intent.ACTION_POWER_USAGE_SUMMARY ));
			setResult(RESULT_OK);
			finish();
		} catch(Exception e) {
			Toast.makeText(this, R.string.feature_not_supported, Toast.LENGTH_LONG).show();
		}
	}
	
	private void onSync() {
		startActivityForResult(new Intent(this, SyncSettings.class), REQUEST_SYNC_SETTINGS);
		setResult(RESULT_OK);
		finish();
	}
}

