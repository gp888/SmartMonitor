package com.android.smartmonitor.receiver;

import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class CheckPhoneStateListener extends PhoneStateListener
{
	private int pState = TelephonyManager.CALL_STATE_IDLE;     
	private Intent intent;

	public CheckPhoneStateListener(Intent _intent)
    {
		super();
		intent = _intent;
    }  
	
	public void onCallStateChanged(int state, String incomingNumber)
    {
        if(state != pState)
        {
        	if(state == TelephonyManager.CALL_STATE_IDLE)
        	{
        		
        	}
        	else if(state == TelephonyManager.CALL_STATE_RINGING)
        	{
        		
        	}
        	else if(state == TelephonyManager.CALL_STATE_OFFHOOK)
        	{
                String phone_num = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        	}
        	
        	pState = state;
        }
    }
}
