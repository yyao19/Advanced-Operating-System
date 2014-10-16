package Peer;

import java.util.ArrayList;

public class peerInfo {
	// Store local infomation
	public static class local{
		// Store peer server port
		public static int serverPort = 0;
		// Store peer client port
		public static int clientPort = 0;
		// Stroe download port
		public static int downloadPort = 0;
		public static String IP = "";	
		public static String name = "";
		public static String ID = "";
		public static String path = "./share";
		public static ArrayList<String> fileList = new ArrayList<String>();
	}
	
	// Store destination information
	public static class dest{
		//store ID: IP and port
		public static ArrayList<String> destList = new ArrayList<String>();
		public static String destination = "";
		//store address: IP and "/share"
		public static ArrayList<String> destPath = new ArrayList<String>();
		public static String path = "./share";
	}
	
	// Initialization
	public void initial(){
		peerInfo.dest.destination = "";
		peerInfo.dest.destList = new ArrayList<String>();
		peerInfo.dest.destPath = new ArrayList<String>();
	}
	
}
