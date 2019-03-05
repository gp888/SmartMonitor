package com.android.smartmonitor.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class CheckNC {
	private static final int timeoutSec = 60;

	/////////////////////Communication Basic Function
	public static boolean download(String url, String fileName)
	{
		try
		{
			URL u = new URL(url);
			HttpURLConnection connection = (HttpURLConnection)u.openConnection();
			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			connection.connect();
			
			InputStream input = connection.getInputStream();
			
			File dir = new File(CheckGlobal.g_recordPath);

	        if (!dir.exists()) {
	            try {
	                dir.mkdirs();
	            } catch (Exception e) {
	                return false;
	            }
	        } else {
	            if (!dir.canWrite()) {
	                return false;
	            }
	        }
			
			OutputStream output = new FileOutputStream(fileName);
			
			byte[] data = new byte[1024];
			long total = 0L;
			int count = 0;
			while ((count = input.read(data)) != -1)
			{
				total += count;
				output.write(data, 0, count);
			}
			
			output.flush();
			output.close();
			input.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public static String send(String url)
	{
		try
		{
			HttpGet localHttpGet = new HttpGet(url);
			
			//HttpParams httpParameters = new BasicHttpParams();
			//HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutSec * 1000);
			//HttpConnectionParams.setSoTimeout(httpParameters, timeoutSec * 1000);
			//DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpResponse localHttpResponse = httpClient.execute(localHttpGet);

			httpClient = null;
			localHttpGet = null;
			if(localHttpResponse == null)
				return null;
			
			return EntityUtils.toString(localHttpResponse.getEntity());
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String send(String url, HashMap<String, String> paramMap, String charSet)
	{
		StringBuilder localStringBuilder = new StringBuilder();
		
		try
		{
			if ((paramMap != null) && (!paramMap.isEmpty()))
			{
				Iterator<Entry<String, String>> localIterator = paramMap.entrySet().iterator();
				while (localIterator.hasNext())
				{
					HashMap.Entry<String, String> localEntry = localIterator.next();
					String value = new String(localEntry.getValue().toString().getBytes(), charSet);
					
					value = URLEncoder.encode(value, charSet);
					value = CheckGlobal.KMSEncode(value);
					value = URLEncoder.encode(value, charSet);
					
					localStringBuilder.append(localEntry.getKey().toString()).append('=').append(value).append('&');
				}
		
				if (localStringBuilder.length() > 0)
					localStringBuilder.deleteCharAt(-1 + localStringBuilder.length());
			}
			
			HttpGet localHttpGet = new HttpGet(url + "?" + localStringBuilder.toString());
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutSec * 1000);
			HttpConnectionParams.setSoTimeout        (httpParameters, timeoutSec * 1000);
			DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
			//DefaultHttpClient httpClient = new DefaultHttpClient();

			HttpResponse localHttpResponse = httpClient.execute(localHttpGet);
			
			localStringBuilder = null;
			httpClient = null;
			localHttpGet = null;
			
			if(localHttpResponse == null)
				return null;
			
			return EntityUtils.toString(localHttpResponse.getEntity());
		}
		catch (Exception e) {
			e.printStackTrace();
			localStringBuilder = null;
			return null;
		}
	}

	public static String sendWithPost(String url, HashMap<String, String> paramMap, String charSet)
	{
		try
		{
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(url);
			
			if ((paramMap != null) && (!paramMap.isEmpty()))
			{
				ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				Iterator<Entry<String, String>> localIterator = paramMap.entrySet().iterator();
				while (localIterator.hasNext())
				{
					HashMap.Entry<String, String> localEntry = localIterator.next();
					String value = new String(localEntry.getValue().toString().getBytes(), charSet);
					
					value = URLEncoder.encode(value, charSet);
					value = CheckGlobal.KMSEncode(value);
					
					nameValuePairs.add(new BasicNameValuePair(localEntry.getKey().toString(), value));
				}
		
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			}
			
			HttpResponse localHttpResponse = httpClient.execute(httppost);
			if(localHttpResponse == null)
				return null;
			
			return EntityUtils.toString(localHttpResponse.getEntity());
		}
		catch (Exception e) {
			return null;
		}
	}	
}
