package com.android.smartmonitor.ftp;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.smartmonitor.common.CheckGlobal;

public class SmartFTPServer {
	private static final String TAG = SmartFTPServer.class.getSimpleName();
    
    protected Thread m_serverThread = null;
    protected boolean m_shouldExit = false;

    public static final int BACKLOG = 21;
    public static final int MAX_SESSIONS = 5;
    public static final String WAKE_LOCK_TAG = "WifiFTP";

    protected static List<String> sessionMonitor = new ArrayList<String>();
    protected static List<String> serverLog = new ArrayList<String>();
    protected static int uiLogLevel = Defaults.getUiLogLevel();

    private final List<SessionThread> sessionThreads = new ArrayList<SessionThread>();
    private static SharedPreferences settings = null;
    
    private Context m_context;
    private HashMap<String, String> m_command;
    
    public SmartFTPServer(Context context) {
    	m_command = null;
    	m_context = context;
    	
    	Globals.setContext(m_context);
    	
    	setSettings();
    }
    
    public void destroy() {
    	terminateAllSessions();
        cleanupAndStopService();
    }
    
    public void setCommand(HashMap<String, String> command)
    {
    	m_command = command;
    }

    private boolean setSettings() {
    	settings = PreferenceManager.getDefaultSharedPreferences(m_context);
        
        SharedPreferences.Editor editor = settings.edit();
        
        editor.putString("ftp_username", CheckGlobal.g_FTPUserName);
        editor.putString("ftp_password", CheckGlobal.g_FTPPassword);
        editor.putString("chrootDir", Defaults.chrootDir);
	    
        editor.commit();
        
        validateBlock: {
	        File chrootDirAsFile = new File(Defaults.chrootDir);
	        if (!chrootDirAsFile.isDirectory()) {
	            Log.e(TAG, "Chroot dir is invalid");
	            break validateBlock;
	        }
	        
	        Globals.setChrootDir(chrootDirAsFile);
	        Globals.setUsername(CheckGlobal.g_FTPUserName);
        }

        return true;
    }

    private void terminateAllSessions() {
        Log.i(TAG, "Terminating " + sessionThreads.size() + " session thread(s)");
        synchronized (this) {
            for (SessionThread sessionThread : sessionThreads) {
                if (sessionThread != null) {
                    sessionThread.closeDataSocket();
                    sessionThread.closeSocket();
                }
            }
        }
    }

    public void cleanupAndStopService() {
        // Call the Android Service shutdown function
    }

    public void errorShutdown() {
        cleanupAndStopService();
    }

    public static InetAddress getLocalInetAddress() {
        if (isConnectedToLocalNetwork() == false) {
            Log.e(TAG, "getLocalInetAddress called and no connection");
            return null;
        }
        // @TODO: next if block could probably be removed
        if (isConnectedUsingWifi() == true) {
            Context context = Globals.getContext();
            WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            int ipAddress = wm.getConnectionInfo().getIpAddress();
            if (ipAddress == 0)
                return null;
            return Util.intToInet(ipAddress);
        }
        // This next part should be able to get the local ip address, but in some case
        // I'm receiving the routable address
        try {
            Enumeration<NetworkInterface> netinterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (netinterfaces.hasMoreElements()) {
                NetworkInterface netinterface = netinterfaces.nextElement();
                Enumeration<InetAddress> adresses = netinterface.getInetAddresses();
                while (adresses.hasMoreElements()) {
                    InetAddress address = adresses.nextElement();
                    // this is the condition that sometimes gives problems
                    if (address.isLoopbackAddress() == false
                            && address.isLinkLocalAddress() == false)
                        return address;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isConnectedToLocalNetwork() {
        Context context = Globals.getContext();
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        // @TODO: this is only defined starting in api level 13
        final int TYPE_ETHERNET = 0x00000009;
        return ni != null && ni.isConnected() == true
                && (ni.getType() & (ConnectivityManager.TYPE_WIFI | TYPE_ETHERNET)) != 0;
    }

    public static boolean isConnectedUsingWifi() {
        Context context = Globals.getContext();
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected() == true
                && ni.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static List<String> getSessionMonitorContents() {
        return new ArrayList<String>(sessionMonitor);
    }

    public static List<String> getServerLogContents() {
        return new ArrayList<String>(serverLog);
    }

    public static void log(int msgLevel, String s) {
        serverLog.add(s);
        int maxSize = Defaults.getServerLogScrollBack();
        while (serverLog.size() > maxSize) {
            serverLog.remove(0);
        }
    }

    public static void writeMonitor(boolean incoming, String s) {
    }

    public void registerSessionThread(SessionThread newSession) {
        synchronized (this) {
            List<SessionThread> toBeRemoved = new ArrayList<SessionThread>();
            for (SessionThread sessionThread : sessionThreads) {
                if (!sessionThread.isAlive()) {
                    Log.d(TAG, "Cleaning up finished session...");
                    try {
                        sessionThread.join();
                        Log.d(TAG, "Thread joined");
                        toBeRemoved.add(sessionThread);
                        sessionThread.closeSocket(); // make sure socket closed
                    } catch (InterruptedException e) {
                        Log.d(TAG, "Interrupted while joining");
                    }
                }
            }
            for (SessionThread removeThread : toBeRemoved) {
                sessionThreads.remove(removeThread);
            }

            sessionThreads.add(newSession);
        }
        Log.d(TAG, "Registered session thread");
    }

    static public SharedPreferences getSettings() {
        return settings;
    }
    
    public void executeCommand()
    {
    	if(m_command == null)
    		return;
    	
		TcpListener listener = new TcpListener(m_command, this);
		listener.start();
    }
}
