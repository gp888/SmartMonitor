package com.android.smartmonitor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Page4Activity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page4);

        ((Button)findViewById(R.id.btn1)).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramAnonymousView)
            {
                Intent intent = new Intent(Page4Activity.this, Page1Activity.class);
                Page4Activity.this.startActivity(intent);
                Page4Activity.this.overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                Page4Activity.this.finish();
            }
        });
        ((Button)findViewById(R.id.btn2)).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramAnonymousView)
            {
                Intent intent = new Intent(Page4Activity.this, Page2Activity.class);
                Page4Activity.this.startActivity(intent);
                Page4Activity.this.overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                Page4Activity.this.finish();
            }
        });
        ((Button)findViewById(R.id.btn3)).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramAnonymousView)
            {
                Intent intent = new Intent(Page4Activity.this, Page3Activity.class);
                Page4Activity.this.startActivity(intent);
                Page4Activity.this.overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                Page4Activity.this.finish();
            }
        });
        ((Button)findViewById(R.id.btn4)).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramAnonymousView) {}
        });
        ((Button)findViewById(R.id.btn5)).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramAnonymousView)
            {
                Intent intent = new Intent(Page4Activity.this, Page5Activity.class);
                Page4Activity.this.startActivity(intent);
                Page4Activity.this.overridePendingTransition(R.anim.new_dync_in_from_right, R.anim.new_dync_out_to_left);
                Page4Activity.this.finish();
            }
        });
        ((Button)findViewById(R.id.btn_topage5)).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramAnonymousView)
            {
                Intent intent = new Intent(Page4Activity.this, Page5Activity.class);
                Page4Activity.this.startActivity(intent);
                Page4Activity.this.overridePendingTransition(R.anim.new_dync_in_from_right, R.anim.new_dync_out_to_left);
                Page4Activity.this.finish();
            }
        });
        ((Button)findViewById(R.id.btn_call1)).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramAnonymousView)
            {
                Intent intent = new Intent("android.intent.action.CALL", Uri.parse("tel:15446800"));
                Page4Activity.this.startActivity(intent);
            }
        });
        ((Button)findViewById(R.id.btn_call2)).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramAnonymousView)
            {
                Intent intent = new Intent("android.intent.action.CALL", Uri.parse("tel:1332"));
                Page4Activity.this.startActivity(intent);
            }
        });
    }
}
