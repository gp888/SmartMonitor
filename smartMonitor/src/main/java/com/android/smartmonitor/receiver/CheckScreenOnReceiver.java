package com.android.smartmonitor.receiver;

import com.android.smartmonitor.control.CheckCall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CheckScreenOnReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context paramContext, Intent paramIntent) {
		if (CheckCall.getMyCallState())
			abortBroadcast();
		
		//System.out.println("screenon");
	}
}