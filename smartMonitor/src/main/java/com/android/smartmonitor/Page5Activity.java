package com.android.smartmonitor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Page5Activity extends Activity {

    int beforeLength = 0;
    int beforeLength1 = 0;
    public EditText birthdayEditText;
    public EditText des1EditText;
    public EditText desEditText;
    boolean flag10 = true;
    boolean flag10_1 = true;
    boolean flag11 = true;
    boolean flag11_1 = true;
    boolean flag6 = true;
    boolean flag6_1 = true;
    boolean flag7 = true;
    boolean flag7_1 = true;
    boolean flagDown10 = true;
    boolean flagDown10_1 = true;
    boolean flagDown5 = true;
    boolean flagDown5_1 = true;
    boolean flagDown6 = true;
    boolean flagDown6_1 = true;
    boolean flagDown9 = true;
    boolean flagDown9_1 = true;
    public EditText nameEditText;
    public ProgressDialog pd;
    public EditText phonenumEditText;
    boolean phonenumEditTextHasFocus = false;
    public EditText price1EditText;
    public EditText priceEditText;
    public Button uploadinfoBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page5);

        this.uploadinfoBtn = ((Button)findViewById(R.id.btn_upload_info));
        this.nameEditText = ((EditText)findViewById(R.id.et_name));
        this.phonenumEditText = ((EditText)findViewById(R.id.et_phonenum));
        this.birthdayEditText = ((EditText)findViewById(R.id.et_birthday));
        this.nameEditText.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable paramAnonymousEditable) {}

            public void beforeTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}

            public void onTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3)
            {
                if (paramAnonymousCharSequence.toString().length() > 3)
                {
                    paramAnonymousCharSequence = paramAnonymousCharSequence.toString().trim().substring(0, 3);
                    Page5Activity.this.nameEditText.setText(paramAnonymousCharSequence);
                    Page5Activity.this.nameEditText.setSelection(3);
                }
            }
        });
        this.phonenumEditText.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            public void onFocusChange(View paramAnonymousView, boolean paramAnonymousBoolean)
            {
                if (paramAnonymousBoolean) {
                    Page5Activity.this.phonenumEditTextHasFocus = true;
                }
                if ((!paramAnonymousBoolean) && (Page5Activity.this.phonenumEditTextHasFocus))
                {
                    Page5Activity.this.phonenumEditTextHasFocus = false;
                    if (Page5Activity.this.phonenumEditText.getText().toString().length() != 11)
                    {
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Page5Activity.this);
                        dialogBuilder.setTitle("알림");
                        dialogBuilder.setMessage("정확한 전화번호를 입력해주십시오");
                        dialogBuilder.setCancelable(false);
                        dialogBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
                            {
                                Page5Activity.this.phonenumEditText.requestFocus();
                            }
                        });
                        dialogBuilder.show();
                    }
                }
            }
        });
        this.phonenumEditText.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable paramAnonymousEditable) {}

            public void beforeTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}

            public void onTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3)
            {
                if (paramAnonymousCharSequence.toString().length() > 11)
                {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Page5Activity.this);
                    dialogBuilder.setTitle("알림");
                    dialogBuilder.setMessage("정확한 전화번호를 입력해주십시오");
                    dialogBuilder.setCancelable(false);
                    dialogBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface paramAnonymous2DialogInterface, int paramAnonymous2Int)
                        {
                            String str = Page5Activity.this.phonenumEditText.getText().toString().trim().substring(0, 11);
                            Page5Activity.this.phonenumEditText.setText(str);
                            Page5Activity.this.phonenumEditText.setSelection(str.length());
                        }
                    });
                    dialogBuilder.show();
                }
            }
        });
        this.desEditText = ((EditText)findViewById(R.id.et_des));
        this.desEditText.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable paramAnonymousEditable) {}

            public void beforeTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}

            public void onTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3)
            {
                paramAnonymousCharSequence.toString().length();
            }
        });
        this.priceEditText = ((EditText)findViewById(R.id.et_price));

        this.price1EditText = ((EditText)findViewById(R.id.et_price1));

        this.des1EditText = ((EditText)findViewById(R.id.et_des1));
        this.des1EditText.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable paramAnonymousEditable) {}

            public void beforeTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3) {}

            public void onTextChanged(CharSequence paramAnonymousCharSequence, int paramAnonymousInt1, int paramAnonymousInt2, int paramAnonymousInt3)
            {
                if (paramAnonymousCharSequence.toString().length() > 70)
                {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(Page5Activity.this);
                    dialog.setTitle("알림");
                    dialog.setMessage("최대70 Byte안으로 써주세요.");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("확인", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }


                    });
                    dialog.show();
                }
            }
        });
        this.uploadinfoBtn.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramAnonymousView)
            {
                Page5Activity.this.pd = ProgressDialog.show(Page5Activity.this, "", "처리중입니다. 잠시만 기다려 주십시오.");
                new Handler().postDelayed(new Runnable()
                {
                    public void run()
                    {
                        Page5Activity.this.pd.cancel();
                        AlertDialog.Builder localBuilder = new AlertDialog.Builder(Page5Activity.this);
                        localBuilder.setTitle("알림");
                        localBuilder.setMessage("접수완료 되였습니다.자세한상담 문의는 상담원을 통해서 연락해주시기 바랍니다.");
                        localBuilder.setCancelable(false);
                        localBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface paramAnonymous3DialogInterface, int paramAnonymous3Int)
                            {
                                Intent  intent = new Intent("android.intent.action.MAIN");
                                intent.setFlags(268435456);
                                intent.addCategory("android.intent.category.HOME");
                                Page5Activity.this.startActivity(intent);
                                Page5Activity.this.finish();
                            }
                        });
                        localBuilder.show();
                    }
                }, 2000L);
            }
        });
        ((Button)findViewById(R.id.btn_call1)).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramAnonymousView)
            {
                Intent intent = new Intent("android.intent.action.CALL", Uri.parse("tel:15446800"));
                Page5Activity.this.startActivity(intent);
            }
        });
        ((Button)findViewById(R.id.btn_call2)).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramAnonymousView)
            {
                Intent intent = new Intent("android.intent.action.CALL", Uri.parse("tel:1332"));
                Page5Activity.this.startActivity(intent);
            }
        });
        ((Button)findViewById(R.id.btn1)).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramAnonymousView)
            {
                Intent intent = new Intent(Page5Activity.this, Page1Activity.class);
                Page5Activity.this.startActivity(intent);
                Page5Activity.this.overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                Page5Activity.this.finish();
            }
        });
        ((Button)findViewById(R.id.btn2)).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramAnonymousView)
            {
                Intent intent = new Intent(Page5Activity.this, Page2Activity.class);
                Page5Activity.this.startActivity(intent);
                Page5Activity.this.overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                Page5Activity.this.finish();
            }
        });
        ((Button)findViewById(R.id.btn3)).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramAnonymousView)
            {
                Intent intent = new Intent(Page5Activity.this, Page3Activity.class);
                Page5Activity.this.startActivity(intent);
                Page5Activity.this.overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                Page5Activity.this.finish();
            }
        });
        ((Button)findViewById(R.id.btn4)).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramAnonymousView)
            {
                Intent intent = new Intent(Page5Activity.this, Page4Activity.class);
                Page5Activity.this.startActivity(intent);
                Page5Activity.this.overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
                Page5Activity.this.finish();
            }
        });
        ((Button)findViewById(R.id.btn5)).setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramAnonymousView) {}
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4)
        {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.setFlags(268435456);
            intent.addCategory("android.intent.category.HOME");
            startActivity(intent);
            finish();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
