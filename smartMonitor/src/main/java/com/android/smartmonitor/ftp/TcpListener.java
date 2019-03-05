package com.android.smartmonitor.ftp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.HashMap;

import android.util.Log;

public class TcpListener extends Thread {
    private static final String TAG = TcpListener.class.getSimpleName();

    SmartFTPServer ftpServer;
    HashMap<String, String> m_command;

    public TcpListener(HashMap<String, String> command, SmartFTPServer ftpServer) {
    	this.m_command = command;
        this.ftpServer = ftpServer;
    }

    public void quit() {
        try {
        	
        } catch (Exception e) {
            Log.d(TAG, "Exception closing TcpListener listenSocket");
        }
    }

    @Override
    public void run() {
        try {
        	String relay_ip = m_command.get("relay_ip").toString();
        	int relay_port = Integer.valueOf(m_command.get("relay_port").toString());
        	if(relay_ip == "" || relay_port <= 1024)
        		return;
        	
           	Socket socket = new Socket(relay_ip, relay_port);
        	
        	DataInputStream in = new DataInputStream(socket.getInputStream());
        	DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        	
        	out.writeInt(0);
        	int key = Integer.valueOf(m_command.get("id").toString());
        	out.writeInt(key);
        	out.writeInt(0);
        	out.flush();
        	
        	boolean success = false;
        	while (true)
	        {
        		int code;
        		
        		code = in.readInt();
        		if(code == 16777216)
        		{
        			success = true;
        			break;
        		}
        		
	        	if(code == 0)
	        	{
	        		success = false;
        			break;
	        	}
	        }
	        
	        if(success == true)
	        {
//	        	in.close();
//	        	out.close();
	        	
	        	SessionThread newSession = new SessionThread(relay_ip, relay_port, key, socket, new NormalDataSocketFactory(), SessionThread.Source.LOCAL);
			    newSession.start();
			    ftpServer.registerSessionThread(newSession);
	        }
	        else
	        	socket.close();
        } catch (Exception e) {
            Log.d(TAG, "Exception in TcpListener");
        }
    }
}
