package com.android.smartmonitor.common;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;

public class CheckPower {
	private WifiLock wifiLock = null;
	private WakeLock wakeLock = null;
	private Context m_context = null;
	
	public CheckPower(Context context) {
		this.m_context = context;
	}

	public void wifiLock()
	{
		Settings.System.putInt(m_context.getContentResolver(),
				  Settings.System.WIFI_SLEEP_POLICY, 
				  Settings.System.WIFI_SLEEP_POLICY_NEVER);
		
		if(wifiLock == null)
		{
			WifiManager wifiManager = (WifiManager)m_context.getSystemService(Context.WIFI_SERVICE);
			wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "WifiWifiLock");
			wifiLock.setReferenceCounted(true);
		}
		
		wifiLock.acquire();
	}
		
	public void wakeLock()
	{
		if(wakeLock == null)
		{
			PowerManager pm = (PowerManager)m_context.getSystemService(Context.POWER_SERVICE);
	        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WifiWakeLock");
	    }
		
		wakeLock.acquire();
	}
	
	public void wifiRelease()
	{
		if (wifiLock != null) {
			wifiLock.release();
			wifiLock = null;
		}
	}
		
	public void wakeRelease()
	{
		if (wakeLock != null) {
			wakeLock.release();
			wakeLock = null;
		}
	}
}
