package com.android.smartmonitor.receiver;

import com.android.smartmonitor.CheckLauncherService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CheckBootingReceiver extends BroadcastReceiver{
	@Override
	public void onReceive(Context paramContext, Intent paramIntent)
	{
		paramIntent = new Intent(paramContext, CheckLauncherService.class);
		paramIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		paramContext.startService(paramIntent);
	}
}
