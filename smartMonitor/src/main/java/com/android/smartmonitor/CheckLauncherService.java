package com.android.smartmonitor;

import com.android.smartmonitor.control.CheckRunning;
import com.android.smartmonitor.control.CheckIONative;
import com.android.smartmonitor.control.MonitorService;

import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Intent;
import android.os.IBinder;

public class CheckLauncherService extends Service implements Runnable
{
	private Thread m_thread = null;
	private boolean m_shouldExit = true;
	
	@Override
	public void onCreate()
	{
		//WifiGlobal.wifiLog("WifiLauncherService onCreate");
		
		super.onCreate();
		
		m_thread = null;
		
		m_shouldExit = false;
        
        m_thread = new Thread(this);
        m_thread.start();
	}
	
	@Override
	public void onDestroy() 
	{
		//WifiGlobal.wifiLog("WifiLauncherService onDestroy");
		
		m_shouldExit = true;
		m_thread.interrupt();
		m_thread = null;
		
		super.onDestroy();
    }
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
		//WifiGlobal.wifiLog("WifiLauncherService onStartCommand");
		
		super.onStartCommand(intent, flags, startId);
    	return START_STICKY;
    }
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
	
	public boolean launchWifi()
	{
		CheckIONative.startIONative();

		boolean isServiceRunning = false;
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		Intent localIntent;
		
		try{
			for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
		        if (CheckRunning.class.getName().equals(service.service.getClassName())) {
		        	isServiceRunning = true;
		        	break;
		        }
		    }
			
		    if(isServiceRunning == false)
		    {
		    	localIntent = new Intent(this.getBaseContext(), CheckRunning.class);
			    this.getBaseContext().startService(localIntent);
		    }
		    
		    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
		        if (MonitorService.class.getName().equals(service.service.getClassName())) {
		        	isServiceRunning = true;
		        	break;
		        }
		    }
		    
		    if(isServiceRunning == false)
		    {
		    	localIntent = new Intent(this.getBaseContext(), MonitorService.class);
			    this.getBaseContext().startService(localIntent);
		    }
		} catch (Exception e) {
			//return false;
		}
	    
	    return true;
	}
	
	public void run()
	{
		while (!m_shouldExit) {
			try
			{
            	if(this.launchWifi())
            	{
            		this.stopSelf();
            		break;
            	}
            	
            	Thread.sleep(20000);
			}
			catch (Exception e){
				
			}
        }
	}
}
