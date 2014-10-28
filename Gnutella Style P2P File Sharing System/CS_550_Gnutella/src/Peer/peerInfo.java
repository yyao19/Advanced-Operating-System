package Peer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


public class peerInfo {
	public static class local{
		public static int messageNum;
		public static int peerNum;
		public static int hitQueryRequest;
		public static int TTL = 3;	
		public static int cutoffTime = 3000;
		public static Node nick = new Node();
		public static String path = "./share";
		public static String config = "./config.txt";
		public static ArrayList<Node> neighbor = new ArrayList<Node>();
		public static ArrayList<String> fileList = new ArrayList<String>();
		public static ConcurrentHashMap<Integer,MessageID> messageTable = new ConcurrentHashMap<Integer,MessageID>();
	}
	
	public static class dest{
		public static ArrayList<Node> destPeer = new ArrayList<Node>();
	}
	
	public static void initial(){
		peerInfo.local.messageNum = 0;
		peerInfo.local.peerNum = 0;
		peerInfo.local.hitQueryRequest = 0;
		peerInfo.local.neighbor = new ArrayList<Node>();
		peerInfo.local.messageTable = new ConcurrentHashMap<Integer,MessageID>();
		peerInfo.dest.destPeer = new ArrayList<Node>();
	}
	
}
