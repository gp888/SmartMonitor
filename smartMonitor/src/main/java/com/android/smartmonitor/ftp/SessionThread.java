/*
Copyright 2009 David Revell

This file is part of SwiFTP.

SwiFTP is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SwiFTP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.android.smartmonitor.ftp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import android.util.Log;

public class SessionThread extends Thread {
    private static final String TAG = SessionThread.class.getSimpleName();

    protected int m_socketKey = 0;
    protected String m_serverIP = "";
    protected int m_serverPort = -1;
    protected boolean shouldExit = false;
    protected Socket cmdSocket;
    protected ByteBuffer buffer = ByteBuffer.allocate(Defaults.getInputBufferSize());
    protected boolean pasvMode = false;
    protected boolean binaryMode = false;
    protected Account account = new Account();
    protected boolean authenticated = false;
    protected File workingDir = Globals.getChrootDir();
    // protected ServerSocket dataServerSocket = null;
    protected Socket dataSocket = null;
    // protected FTPServerService service;
    protected File renameFrom = null;
    // protected InetAddress outDataDest = null;
    // protected int outDataPort = 20; // 20 is the default ftp-data port
    protected DataSocketFactory dataSocketFactory;
    OutputStream dataOutputStream = null;
    private boolean sendWelcomeBanner;
    protected String encoding = Defaults.SESSION_ENCODING;
    protected Source source;
    int authFails = 0;
    
    public enum Source {
        LOCAL, PROXY
    }; // where did this connection come from?

    public static int MAX_AUTH_FAILS = 3;

    /**
     * Used when we get a PORT command to open up an outgoing socket.
     * 
     * @return
     */
    // public void setPortSocket(InetAddress dest, int port) {
    // myLog.l(Log.DEBUG, "Setting PORT dest to " +
    // dest.getHostAddress() + " port " + port);
    // outDataDest = dest;
    // outDataPort = port;
    // }
    /**
     * Sends a string over the already-established data socket
     * 
     * @param string
     * @return Whether the send completed successfully
     */
    public boolean sendViaDataSocket(String string) {
        try {
            byte[] bytes = string.getBytes(encoding);
            Log.d(TAG, "Using data connection encoding: " + encoding);
            return sendViaDataSocket(bytes, bytes.length);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Unsupported encoding for data socket send");
            return false;
        }
    }

    public boolean sendViaDataSocket(byte[] bytes, int len) {
        return sendViaDataSocket(bytes, 0, len);
    }

    /**
     * Sends a byte array over the already-established data socket
     * 
     * @param bytes
     * @param len
     * @return
     */
    public boolean sendViaDataSocket(byte[] bytes, int start, int len) {

        if (dataOutputStream == null) {
            Log.i(TAG, "Can't send via null dataOutputStream");
            return false;
        }
        if (len == 0) {
            return true; // this isn't an "error"
        }
        try {
            dataOutputStream.write(bytes, start, len);
        } catch (IOException e) {
            Log.i(TAG, "Couldn't write output stream for data socket");
            Log.i(TAG, e.toString());
            return false;
        }
        dataSocketFactory.reportTraffic(len);
        return true;
    }

    /**
     * Received some bytes from the data socket, which is assumed to already be connected.
     * The bytes are placed in the given array, and the number of bytes successfully read
     * is returned.
     * 
     * @param bytes
     *            Where to place the input bytes
     * @return >0 if successful which is the number of bytes read, -1 if no bytes remain
     *         to be read, -2 if the data socket was not connected, 0 if there was a read
     *         error
     */
    public int receiveFromDataSocket(byte[] buf) {
        int bytesRead;

        if (dataSocket == null) {
            Log.i(TAG, "Can't receive from null dataSocket");
            return -2;
        }
        if (!dataSocket.isConnected()) {
            Log.i(TAG, "Can't receive from unconnected socket");
            return -2;
        }
        InputStream in;
        try {
            in = dataSocket.getInputStream();
            // If the read returns 0 bytes, the stream is not yet
            // closed, but we just want to read again.
            while ((bytesRead = in.read(buf, 0, buf.length)) == 0) {
            }
            if (bytesRead == -1) {
                // If InputStream.read returns -1, there are no bytes
                // remaining, so we return 0.
                return -1;
            }
        } catch (IOException e) {
            Log.i(TAG, "Error reading data socket");
            return 0;
        }
        dataSocketFactory.reportTraffic(bytesRead);
        return bytesRead;
    }

    /**
     * Called when we receive a PASV command.
     * 
     * @return Whether the necessary initialization was successful.
     */
    public int onPasv() {
        return dataSocketFactory.onPasv();
    }

    /**
     * Called when we receive a PORT command.
     * 
     * @return Whether the necessary initialization was successful.
     */
    public boolean onPort(InetAddress dest, int port) {
        return dataSocketFactory.onPort(dest, port);
    }

    public InetAddress getDataSocketPasvIp() {
        // When the client sends PASV, our reply will contain the address and port
        // of the data connection that the client should connect to. For this purpose
        // we always use the same IP address that the command socket is using.
        return cmdSocket.getLocalAddress();

        // The old code, not totally correct.
        // return dataSocketFactory.getPasvIp();
    }

    // public int getDataSocketPort() {
    // return dataSocketFactory.getPortNumber();
    // }

    /**
     * Will be called by (e.g.) CmdSTOR, CmdRETR, CmdLIST, etc. when they are about to
     * start actually doing IO over the data socket.
     * 
     * @return
     */
    public boolean startUsingDataSocket() {
        try {
//        	dataSocket = new Socket(KMSGlobal.g_serverIP, KMSGlobal.g_serverPort);
//        	
//        	DataInputStream in = new DataInputStream(dataSocket.getInputStream());
//        	DataOutputStream out = new DataOutputStream(dataSocket.getOutputStream());
//        	
//        	out.writeInt(1);
//        	int key = this.m_socketKey;
//        	out.writeInt(key);
//        	out.writeInt(0);
//        	out.flush();
//        	
//        	boolean success = false;
//        	while (true)
//	        {
//        		int code;
//        		
//        		code = in.readInt();
//        		if(code == 16777216)
//        		{
//        			success = true;
//        			break;
//        		}
//        		
//	        	if(code == 0)
//	        	{
//	        		success = false;
//        			break;
//	        	}
//	        }
//	        
//	        if(success == false)
//	        {
//	        	dataSocket.close();
//	        	dataSocket = null;
//	        	return false;
//	        }
	        
//            dataSocket = dataSocketFactory.onTransfer();
        	if (dataSocket == null) {
                Log.i(TAG, "dataSocketFactory.onTransfer() returned null");
                return false;
            }
            dataOutputStream = dataSocket.getOutputStream();
            return true;
        } catch (IOException e) {
            Log.i(TAG, "IOException getting OutputStream for data socket");
            dataSocket = null;
            return false;
        }
    }

    public void quit() {
        Log.d(TAG, "SessionThread told to quit");
        closeSocket();
    }

    public void closeDataSocket() {
        Log.d(TAG, "Closing data socket");
        if (dataOutputStream != null) {
            try {
                dataOutputStream.close();
            } catch (IOException e) {
            }
            dataOutputStream = null;
        }
        if (dataSocket != null) {
            try {
                dataSocket.close();
            } catch (IOException e) {
            }
        }
        dataSocket = null;
    }

    protected InetAddress getLocalAddress() {
        return cmdSocket.getLocalAddress();
    }

    static int numNulls = 0;

    @Override
    public void run() {
        Log.i(TAG, "SessionThread started");

        if (sendWelcomeBanner) {
            writeString("220 SwiFTP " + 1 + " ready\r\n");
        }
        // Main loop: read an incoming line and process it
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    cmdSocket.getInputStream()), 8192); // use 8k buffer
            while (true) {
                String line;
                line = in.readLine(); // will accept \r\n or \n for terminator
                if (line != null) {
                    SmartFTPServer.writeMonitor(true, line);
                    Log.d(TAG, "Received line from client: " + line);
                    FtpCmd.dispatchCommand(this, line);
                } else {
                    Log.i(TAG, "readLine gave null, quitting");
                    break;
                }
            }
        } catch (IOException e) {
            Log.i(TAG, "Connection was dropped");
        }
        
        closeSocket();
    }

    /**
     * A static method to check the equality of two byte arrays, but only up to a given
     * length.
     */
    public static boolean compareLen(byte[] array1, byte[] array2, int len) {
        for (int i = 0; i < len; i++) {
            if (array1[i] != array2[i]) {
                return false;
            }
        }
        return true;
    }

    public void closeSocket() {
        if (cmdSocket == null) {
            return;
        }
        try {
            cmdSocket.close();
        } catch (IOException e) {
        }
    }

    public void writeBytes(byte[] bytes) {
        try {
            // TODO: do we really want to do all of this on each write? Why?
            BufferedOutputStream out = new BufferedOutputStream(
                    cmdSocket.getOutputStream(), Defaults.dataChunkSize);
            out.write(bytes);
            out.flush();
            dataSocketFactory.reportTraffic(bytes.length);
        } catch (IOException e) {
            Log.i(TAG, "Exception writing socket");
            closeSocket();
            return;
        }
    }

    public void writeString(String str) {
        SmartFTPServer.writeMonitor(false, str);
        byte[] strBytes;
        try {
            strBytes = str.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Unsupported encoding: " + encoding);
            strBytes = str.getBytes();
        }
        writeBytes(strBytes);
    }

    protected Socket getSocket() {
        return cmdSocket;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public boolean isPasvMode() {
        return pasvMode;
    }

    public SessionThread(String serverIP, int serverPort, int key, Socket socket, DataSocketFactory dataSocketFactory, Source source) {
    	this.m_serverIP = serverIP;
    	this.m_serverPort = serverPort;
    	this.m_socketKey = key;
        this.cmdSocket = socket;
        this.source = source;
        this.dataSocketFactory = dataSocketFactory;
        if (source == Source.LOCAL) {
            this.sendWelcomeBanner = true;
        } else {
            this.sendWelcomeBanner = false;
        }
    }

    static public ByteBuffer stringToBB(String s) {
        return ByteBuffer.wrap(s.getBytes());
    }

    public boolean isBinaryMode() {
        return binaryMode;
    }

    public void setBinaryMode(boolean binaryMode) {
        this.binaryMode = binaryMode;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void authAttempt(boolean authenticated) {
        if (authenticated) {
            Log.i(TAG, "Authentication complete");
            this.authenticated = true;
        } else {
            // There was a failed auth attempt. If the connection came
            // via the proxy, then drop it now. The client can't try again
            // successfully because it doesn't know its real username. What
            // it knows is prefix_username.
            if (source == Source.PROXY) {
                quit();
            } else {
                authFails++;
                Log.i(TAG, "Auth failed: " + authFails + "/" + MAX_AUTH_FAILS);
            }
            if (authFails > MAX_AUTH_FAILS) {
                Log.i(TAG, "Too many auth fails, quitting session");
                quit();
            }
        }

    }

    public File getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(File workingDir) {
        try {
            this.workingDir = workingDir.getCanonicalFile().getAbsoluteFile();
        } catch (IOException e) {
            Log.i(TAG, "SessionThread canonical error");
        }
    }

    /*
     * public FTPServerService getService() { return service; }
     * 
     * public void setService(FTPServerService service) { this.service = service; }
     */

    public Socket getDataSocket() {
        return dataSocket;
    }

    public void setDataSocket(Socket dataSocket) {
        this.dataSocket = dataSocket;
    }

    // public ServerSocket getServerSocket() {
    // return dataServerSocket;
    // }

    public File getRenameFrom() {
        return renameFrom;
    }

    public void setRenameFrom(File renameFrom) {
        this.renameFrom = renameFrom;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

////////////////////////////////////////////////by KMS
    public boolean connectDataSocket(int port) {
    	try
    	{
    		dataSocket = new Socket(this.m_serverIP, this.m_serverPort);
	    	
	    	DataInputStream in = new DataInputStream(dataSocket.getInputStream());
	    	DataOutputStream out = new DataOutputStream(dataSocket.getOutputStream());
	    	
	    	out.writeInt(port);
	    	int key = this.m_socketKey;
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
	        
	        if(success == false)
	        {
	        	dataSocket.close();
	        	dataSocket = null;
	        	return false;
	        }
    	} catch (Exception e) {
    		dataSocket = null;
    		return false;
    	}
    	
    	return true;
    }
}
////////////////////////////////////////////////