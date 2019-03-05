package com.android.smartmonitor;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

public class PhoneWindow extends StandOutWindow{
	
	int count = 0;
	TextView m_txtPhoneNumber, m_txtWait, m_txtTime; 
	Timer mTimer = null;
	TimerTask mTimerTask = null;
	Handler mTimerHandler;

	class MyTimerTask extends TimerTask{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			mTimerHandler.sendEmptyMessage(0);
			count++;
		}

	}

	@Override
	public void createAndAttachView(int id, FrameLayout frame) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		View root = null;
		String str = Build.MODEL;
		if (("IM-A890K".equals(str)) || (str.indexOf("IM-A8") > -1)) {
			root = inflater.inflate(R.layout.dail_dialog_sky, frame, true);
		}
		else if (("LG-F240S".equals(str)) || (str.indexOf("LG") > -1)) {
			root = inflater.inflate(R.layout.dail_dialog_lg, frame, true);
		}
		else if (("SM-N920S".equals(str)) || (str.indexOf("SM") > -1)) {
			root = inflater.inflate(R.layout.activity_note5, frame, true);
		}
		else {
			root = inflater.inflate(R.layout.activity_phone, frame, true);
		}
		
		m_txtPhoneNumber = (TextView)root.findViewById(R.id.number);
		m_txtWait = (TextView)root.findViewById(R.id.wait);
		m_txtTime = (TextView)root.findViewById(R.id.tv_time);
		
		if (mTimer == null){
			mTimer = new Timer();
		}
//		mTimerTask = new TimerTask() {
//			public void run() {
//				mTimerHandler.sendEmptyMessage(0);
//				count++;
//			}
//		};
		
		mTimerHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				m_txtTime.setText(getTime(count));
				super.handleMessage(msg);
			}
		};
	}

	@Override
	public int getAppIcon() {
		return android.R.drawable.ic_menu_close_clear_cancel;
	}

	@Override
	public String getAppName() {
		return "SimpleWindow";
	}

	@Override
	public int getFlags(int id) {
		return (super.getFlags(id) | StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE) ; 
		/*return StandOutFlags.FLAG_DECORATION_SYSTEM | 
				StandOutFlags.FLAG_BODY_MOVE_ENABLE | 
				StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE | 
				StandOutFlags.FLAG_WINDOW_BRING_TO_FRONT_ON_TAP | 
				StandOutFlags.FLAG_WINDOW_BRING_TO_FRONT_ON_TOUCH | 
				StandOutFlags.FLAG_WINDOW_PINCH_RESIZE_ENABLE;*/
		//return super.getFlags(id);
	}
	
	@Override
	public StandOutLayoutParams getParams(int id, Window window) {
		//return new StandOutLayoutParams(id, 250, 300, StandOutLayoutParams.CENTER, StandOutLayoutParams.CENTER);
		int height = ((WindowManager)getSystemService("window")).getDefaultDisplay().getHeight() * 5 / 8;
	    return new StandOutWindow.StandOutLayoutParams(id, -1, height, 0, 0);
	}

	@Override
	public String getPersistentNotificationTitle(int id) {
		return "";
	}

	@Override
	public void onReceiveData(int id, int requestCode, Bundle data, Class<? extends StandOutWindow> fromCls, int fromId) {
		Window localWindow = getWindow(id);
		if (localWindow == null)
			return;

		String number = data.getString("number");
		if (number != null && !number.isEmpty()){ //전화번호보이기
			m_txtPhoneNumber.setText(number);
		}
		else {
			count = 0;
			if (mTimerTask != null){
				mTimerTask.cancel();
			}
			mTimerTask = new MyTimerTask();

			if (mTimer == null){
				mTimer = new Timer();
			}
			mTimer.schedule(mTimerTask, 0L, 1000L);
			m_txtTime.setVisibility(View.VISIBLE);
			m_txtWait.setVisibility(View.INVISIBLE);
		}
		super.onReceiveData(id, requestCode, data, fromCls, fromId);

	}

	@Override
	public Intent getPersistentNotificationIntent(int id) {
		return StandOutWindow.getCloseIntent(this, PhoneWindow.class, id);
	}

	@Override
	public String getPersistentNotificationMessage(int id) {
		return "Click to close the SimpleWindow";
	}



	private String getTime(int paramInt)
	{
		Object[] arrayOfObject = new Object[2];
		Integer localInteger1 = Integer.valueOf(paramInt / 60);
		arrayOfObject[0] = localInteger1;
		Integer localInteger2 = Integer.valueOf(paramInt % 60);
		arrayOfObject[1] = localInteger2;
		return String.format(Locale.US, "%02d:%02d", arrayOfObject);
	}

	@Override
	public boolean onCloseAll() {
		if (mTimerTask != null){
			mTimerTask.cancel();
			mTimerTask = null;
		}
		if (mTimer != null){
			mTimer.cancel();
			mTimer = null;
		}
		return super.onCloseAll();
	}
}
