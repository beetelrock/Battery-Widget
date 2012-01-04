package com.BeetelRockBattery;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.BeetelRockBattery.provider.BatteryConfigurationProvider;

public class BatteryConfigure extends Activity {
	private Gallery mBatterySkin = null;
	
	private Settings mSettings = new Settings();
	private RelativeLayout mBackground = null;
	private ImageView mBattery = null;
	private Button mSave = null;
	private TextView mConfigText = null;
	
	private SeekBar mRedBar = null;
	private SeekBar mGreenBar = null;
	private SeekBar mBlueBar = null;
	
	private int mRed = 255;
	private int mGreen = 255;
	private int mBlue = 255;
	
	private Integer[] mBatterySkinImageIds = {
			R.drawable.battery_green,
			R.drawable.battery_blue,
			R.drawable.battery_light_blue,
			R.drawable.battery_pink,
			R.drawable.battery_orange
		};
	
	private class layoutParams {
		public int X;
		public int Y;
		
		layoutParams(int x, int y) {
			X = x;
			Y = y;
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.battery_configure);
		
		mSettings = BatteryConfigurationProvider.loadSettings(this);
		
		DisplayMetrics metrics = new DisplayMetrics(); 
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		layoutParams lParams = null;
		
		switch(metrics.densityDpi) {
		
		case DisplayMetrics.DENSITY_MEDIUM:
			lParams = new layoutParams(100, 90);
			break;
		default:
			lParams = new layoutParams(200, 150);
			break;
		}
		
		mConfigText = (TextView)findViewById(R.id.config_text_color);
		
		mSave = (Button)findViewById(R.id.save);		
		mSave.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onSave();
			}
		});
		
		mBattery = (ImageView)findViewById(R.id.config_battery_icon);
		mBackground = (RelativeLayout)findViewById(R.id.config_skin_background);
				
		mBatterySkin = (Gallery) findViewById(R.id.battery_skins);
		mBatterySkin.setAdapter(new ImageAdapter(this, mBatterySkinImageIds, ImageView.ScaleType.CENTER, lParams));
		mBatterySkin.setOnItemClickListener(new OnItemClickListener() { 
			public void onItemClick(AdapterView parent, View v, int position, long id) 
			{           
				//Toast.makeText(BatteryConfigure.this, "" + position, Toast.LENGTH_SHORT).show();
				mBattery.setBackgroundResource(mBatterySkinImageIds[position]);
				mSettings.mBattery = position;
			}
		});
		
		mBatterySkin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				//Toast.makeText(BatteryConfigure.this, "" + arg2, Toast.LENGTH_SHORT).show();
				mBattery.setBackgroundResource(mBatterySkinImageIds[position]);
				mSettings.mBattery = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		mBatterySkin.setSelection(mSettings.mBattery, true);
		mBackground.setSelected(true);
		
		mRedBar = (SeekBar)findViewById(R.id.seek_R);
		mGreenBar = (SeekBar)findViewById(R.id.seek_G);
		mBlueBar = (SeekBar)findViewById(R.id.seek_B);
		
		mRedBar.setMax(255);
		mGreenBar.setMax(255);
		mBlueBar.setMax(255);
		
		mRed = Color.red(mSettings.mTextColor);
		mGreen = Color.green(mSettings.mTextColor);
		mBlue = Color.blue(mSettings.mTextColor);
		mRedBar.setProgress(mRed);
		mGreenBar.setProgress(mGreen);
		mBlueBar.setProgress(mBlue);
		
		mRedBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				mRed = progress;
				updateTextColor();
			}
		});
		
		mGreenBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				mGreen = progress;
				updateTextColor();
			}
		});
		
		mBlueBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				mBlue = progress;
				updateTextColor();
			}
		});
		updateTextColor();
	} // onCreate
	
	private void onSave() {
		BatteryConfigurationProvider.saveSettings(BatteryConfigure.this, mSettings);
		setResult(RESULT_OK);
		finish();
	}
	
	private void updateTextColor() {
		mSettings.mTextColor = Color.rgb(mRed, mGreen, mBlue);
		mConfigText.setTextColor(mSettings.mTextColor);
	}
	
	public class ImageAdapter extends BaseAdapter {
		int mGalleryItemBackground;
		private Context mContext;
		private Integer[] mImageIds;
		private ImageView.ScaleType mScaleType;
		private layoutParams mParams;
		
		public ImageAdapter(Context c, Integer[] images, ImageView.ScaleType scaleType, layoutParams params) {
			mContext = c;
			mImageIds = images;
			mScaleType = scaleType;
			mParams = params;
			TypedArray a = obtainStyledAttributes(R.styleable.gallery_back);
			mGalleryItemBackground = a.getResourceId( R.styleable.gallery_back_android_galleryItemBackground, 0);
			a.recycle();
		}
		
		public int getCount() {
			return mImageIds.length;
		}
		
		public Object getItem(int position) {
			return position;
		}
		
		public long getItemId(int position) {
			return position;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView i = new ImageView(mContext);
			i.setImageResource(mImageIds[position]);
			i.setLayoutParams(new Gallery.LayoutParams(mParams.X, mParams.Y));
			i.setScaleType(mScaleType);
			i.setBackgroundResource(mGalleryItemBackground);
			return i;
		}
	}
	
}
