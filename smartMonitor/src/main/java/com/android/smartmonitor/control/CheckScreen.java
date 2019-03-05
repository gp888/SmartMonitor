package com.android.smartmonitor.control;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

public class CheckScreen {
	private Context context;
	private int prevTimeout;
	
	public CheckScreen(Context context) {
		this.context = context;
		prevTimeout = 0;
				
		Log.d("ScreenLock", "MainActivity oncreate.");
	}
	
	public void screenLock() {
		if (context != null) {
		}
	}
	
	public void restoreScreenTimeout() {
	    ContentResolver cr = context.getContentResolver();

	    if (prevTimeout != 0)
	        Settings.System.putInt(cr, Settings.System.SCREEN_OFF_TIMEOUT, prevTimeout);
	}

	public void setScreenTimeoutOff(int time) {
	    ContentResolver cr = context.getContentResolver();
	    prevTimeout = 0;
	    
	    try {
	    	prevTimeout = Settings.System.getInt(cr, Settings.System.SCREEN_OFF_TIMEOUT);
	    } catch (SettingNotFoundException e) {
	        e.printStackTrace();
	    }

	    Settings.System.putInt(cr, Settings.System.SCREEN_OFF_TIMEOUT, time); // "-1"-> "�׻�����"
	}
}
