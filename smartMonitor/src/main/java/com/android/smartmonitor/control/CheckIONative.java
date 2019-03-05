package com.android.smartmonitor.control;

import java.io.OutputStream;


public class CheckIONative {
//	static {
//		try{
//			System.loadLibrary("fb1");
//		} catch (UnsatisfiedLinkError error){
//			String path = "/mnt/sdcard/debug.txt";
//		    File file = new File(path);
//		    FileOutputStream fos = null;
//		    String text = "loadLibrary fb failed !!!\n";
//		    
//		    try{
//		    	fos = new FileOutputStream(file);
//		    	fos.write(text.getBytes());
//				fos.close();
//		    } catch (Exception e) {
//		    	e.printStackTrace();
//		    }
//		}
//    }
	
	private static void sendCommand(String command, int param)
	{
//		try
//		{
//			DatagramSocket clientSocket = new DatagramSocket();
//			InetAddress addr = InetAddress.getLocalHost();
//			
//			String toSend = command + "|" + param + "|";
//			byte[] buffer = toSend.getBytes();
//	
//			DatagramPacket question = new DatagramPacket(buffer, buffer.length,	addr, 13134);
//			clientSocket.send(question);
//		}
//		catch (Exception e) {
//			
//		}
	}
	
	public static void clearFBStart()
	{
		//sendCommand("~ION_clearFBStart", 0);
	}
	
	public static void clearFBStop()
	{
		//sendCommand("~ION_clearFBStop", 0);
	}
	
//	public static void keyEvent(int key)
//	{
//		try
//		{
//			sendCommand("~ION_keyDown", key);
//			Thread.sleep(150);
//			sendCommand("~ION_keyUp", key);
//		}
//		catch (Exception e)
//		{
//			
//		}
//	}
	
	public static void keyDown(int key)
	{
//		sendCommand("~ION_keyDown", key);
	}
	
	public static void keyUp(int key)
	{
//		sendCommand("~ION_keyUp", key);
	}
	
	public static void initInput()
	{
//		sendCommand("~ION_initInput", 0);
	}
	
	public static void cleanupInput()
	{
//		sendCommand("~ION_cleanupInput", 0);
	}
	
	private static void writeCommand(OutputStream os, String command) throws Exception {
//		os.write((command + "\n").getBytes("ASCII"));
	}
	
	public static void startIONative()
	{
//		try
//		{
//			Process sh;
//			sh = Runtime.getRuntime().exec("su\n");
//			OutputStream os = sh.getOutputStream();
//				
//			writeCommand(os, "cd /data/data/org.onaips.vnc/lib" + "\n");
//			String IONative_exec = "./libfb.so";
//			writeCommand(os, IONative_exec+"\n");
//		} catch (Exception e) {
//		}
	}
}
