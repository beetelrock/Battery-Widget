package com.BeetelRockBattery;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

public class STToggleButton extends ImageButton {

	public static final int ON = 1;
	public static final int OFF = 0;
	
	private boolean mToggle = false;
	public STToggleButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public STToggleButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public STToggleButton(Context context) {
		super(context);
	}

	@Override
	public boolean performClick() {
		
		boolean result = super.performClick();
		mToggle = !mToggle;
		setState(mToggle ? ON : OFF);
		return result;
	}
	
	public void setState(int state) {
		if(state == ON) {
			mToggle = true;
			setBackgroundResource(R.drawable.selected);
		}
		else {
			mToggle = false;
			setBackgroundResource(R.drawable.unselected);
		}
	}
	
	public int getState() {
		return mToggle ? ON : OFF;
	}
}

