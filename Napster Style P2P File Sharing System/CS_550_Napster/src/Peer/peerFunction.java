package Peer;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class peerFunction {

	public void intialize(){
		try{
			InetAddress addr = InetAddress.getLocalHost();
			peerInfo.local.IP = addr.getHostAddress();
			peerInfo.local.clientPort = 8010;
			peerInfo.local.serverPort = 9010;
			peerInfo.local.downloadPort = 10010;
			peerInfo.local.name = addr.getHostName();
			peerInfo.local.ID = peerInfo.local.IP + ":" + peerInfo.local.serverPort;
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/*
	 *  Register file to local list
	 */
	public void register(String ID, String filename){
		FileWriter writer = null;

		try{
		
			writer = new FileWriter("./peerLog.txt",true);
			
			peerInfo.local.fileList.add(filename);
			System.out.println("File "+filename + " is registered!");
			
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time = df.format(new Date());
			writer.write(time + "\t\tFile "+filename + " is registered on the index server!\r\n");
			writer.close();		

		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/*
	 *  Unregister file from local list
	 */
	public void unregister(String ID, String filename){
		FileWriter writer = null;
		try{
		
			writer = new FileWriter("./peerLog.txt",true);
			peerInfo.local.fileList.remove(filename);		
			System.out.println("File "+filename + " is unregistered!");
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time = df.format(new Date());
			writer.write(time + "\t\tFile "+filename + " is unregistered on the index server!\r\n");
			writer.close();	

		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public boolean search(String filename){
		boolean found = false;
		FileWriter writer = null;
		try{
			writer = new FileWriter("./peerLog.txt",true);
			
			if(peerInfo.dest.destList.size()!=0){
				
				for(int i=0; i<peerInfo.dest.destList.size(); i++){
					String destination = peerInfo.dest.destList.get(i);
					
//					System.out.println(destination);
					
					// Get file ID and IP 
					String[] info = destination.split("\\:");
					String IP = info[0];
					String port = info[1];
					peerInfo.dest.destination = destination + ":share";
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String time = df.format(new Date());
					writer.write(time + "\t\tFile "+filename + " is found on "+IP+" !\r\n");
//					writer.close();
					peerInfo.dest.destPath.add(peerInfo.dest.destination);
				}
								
				found = true;
			}else{
				System.out.println("File "+filename + " is not found!");
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String time = df.format(new Date());
				writer.write(time + "\t\tFile "+filename + " is not found!\r\n");
//				writer.close();
			}
			writer.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		return found;
	}
	
	public PrintWriter getWriter(Socket socket)throws IOException{
		OutputStream socketOut = socket.getOutputStream();
		return new PrintWriter(socketOut, true);
		
	}
	
	public BufferedReader getReader(Socket socket)throws IOException{
		InputStream socketIn = socket.getInputStream();
		return new BufferedReader(new InputStreamReader(socketIn));
		
	}
	
}
