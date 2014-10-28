package Peer;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;

public class peerFunction {
	/*
	 *  search function
	 *  search file on the local peer
	 */
	public boolean search(String fileName){
		if(peerInfo.local.fileList.size()!=0){
			for(int i = 0; i < peerInfo.local.fileList.size(); i++){
				if(fileName.equals(peerInfo.local.fileList.get(i))){
					return true;
				}
			}
		}
		return false;	
	}
	

	/*
	 *  addMessage function
	 *  Add message to the local message table
	 */
	public synchronized void addMessage(int messageNum, MessageID messageID){
		addMessageThread addMessage = new addMessageThread(messageNum, messageID);
		Thread thread = new Thread(addMessage);
		thread.start();
		thread = null;
	}
	
	public synchronized void searchMessage(Message message, int TTL){
		searchMessageThread searchMessage = new searchMessageThread(message, TTL);
		Thread thread = new Thread(searchMessage);
		thread.start();
		thread = null;
	}
	
	/*
	 *  Query function
	 *  Send query message to neighbors
	 */
	public void query(MessageID messageID, int TTL, String fileName){
		queryThread query = new queryThread(messageID, TTL, fileName);
		Thread thread = new Thread(query);
		thread.start();
		thread = null;
	}
	
	/*
	 *  hitQuery function
	 *  Send hitQuery message back
	 */
	public void hitQuery(MessageID messageID, int TTL, String fileName, String IP, int port){
		hitQueryThread hitQuery = new hitQueryThread(messageID, TTL, fileName, IP, port);
		Thread thread = new Thread(hitQuery);
		thread.start();
		thread = null;
	}
	
	public void downLoad(String fileName, int indexNum, String IP, int port){
		downloadThread downLoad = new downloadThread(fileName, indexNum, IP, port);
		Thread thread = new Thread(downLoad);
		thread.start();
		thread = null;
	}
	
	public void sendFile(String fileName, String IP, int port){
		new SThread(fileName, IP, port);
	}
	
	class addMessageThread implements Runnable{

		private int messageNum;
		private MessageID messageID;
		
		public addMessageThread(int messageNum, MessageID messageID){
			this.messageNum = messageNum;
			this.messageID = messageID;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			peerInfo.local.messageTable.put(messageNum, messageID);
//			System.out.println("Add message:"+messageNum+" "+messageID.getSequenceNumber()+" "+messageID.getPeerID().peerName);
		}
		
	}
	
	class searchMessageThread implements Runnable{
		private Message message;
		private MessageID messageID;
		private String fileName;
		private int TTL;
		
		public searchMessageThread(Message message, int TTL){
			this.message = message;
			this.TTL = TTL;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			messageID = message.getMessageID();
        	TTL = message.getTTL();
        	fileName = message.getfileName();
        	
        	int key = messageID.getSequenceNumber();
        	MessageID ID = null;
        	Node node = null;
        	Iterator it = peerInfo.local.messageTable.entrySet().iterator();
        	while(it.hasNext()){
        		Entry entry = (Entry) it.next();
        		if(entry.getKey().equals(key)){
        			ID = (MessageID) entry.getValue();
//        			peerInfo.local.messageTable.remove(entry.getKey(), (MessageID) entry.getValue());
        		}
        	}
        	
        	if(ID != null){
        		int seqNum = ID.getSequenceNumber();
        		node = ID.getPeerID();
        		if(node.equals(peerInfo.local.nick)){

        			peerInfo.local.hitQueryRequest++;	
        			String fileIp = message.getPeerIP();
        			int filePort = message.getPort();
        			if(filePort != -1){                  			
        				Node peer = new Node(fileIp, filePort);
        				boolean b = false;
        				for(int i = 0; i<peerInfo.dest.destPeer.size();i++){
        					if(peerInfo.dest.destPeer.get(i).IP.equals(peer.IP)&&
        							peerInfo.dest.destPeer.get(i).port == peer.port){
        						b = true;
        					}
        				}
        				
        				if(!b){
        					peerInfo.dest.destPeer.add(peer);
        				}	
        			}
        		}else{
        			hitQuery(ID, TTL, fileName, message.getPeerIP(), message.getPort());
        		}                 		
        	}
		}
		
	}
	
	class queryThread implements Runnable{

		private MessageID messageID;
		private int TTL;
		private String fileName;
		private Message message;
		private String command;
		
		public queryThread(MessageID messageID, int TTL, String fileName) {
			this.command = "query";
			this.messageID = messageID;
			this.TTL = TTL;
			this.fileName = fileName;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Node upstream = messageID.getPeerID();
			
			int sequence = messageID.getSequenceNumber();
			
			if(peerInfo.local.neighbor.size()!=0){
				for(int i = 0; i < peerInfo.local.neighbor.size(); i++){
					if(upstream.peerName.equals(peerInfo.local.neighbor.get(i).peerName)
							&& upstream.IP.equals(peerInfo.local.neighbor.get(i).IP)){
						// If the message comes from neighbor i, then do not send query message back
					}else{
						// If the message do not comes from neighbor i, then send query message
						// Add messageNum and local peer information to local message table
						peerInfo.local.messageNum++;
						
						MessageID oldMessage = new MessageID(sequence,upstream);
						
						addMessage(peerInfo.local.messageNum, oldMessage);
						
//						System.out.println(oldMessage.getPeerID().peerName);
						
						MessageID newMessage = new MessageID(peerInfo.local.messageNum,peerInfo.local.nick);
												
						message = new Message(command, newMessage, TTL, fileName);
						
						try {
							FileWriter writer = new FileWriter("./peerLog.txt",true);
							DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String time = df.format(new Date());
							writer.write(time + "\t\tSend:"+command + " " + message.getMessageID().getSequenceNumber() 
									+ " " +message.getfileName()+" "+message.getMessageID().getPeerID().peerName+"\t\n");
							writer.close();	
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
//						System.out.print("Send:"+command + " " + message.getMessageID().getSequenceNumber() + " " +message.getfileName()+" ");
//                    	System.out.println(message.getMessageID().getPeerID().peerName);

						new clientThread(message, peerInfo.local.neighbor.get(i).IP, peerInfo.local.neighbor.get(i).port);
					}
				}
			}		
		}
		
	}
	
	class hitQueryThread implements Runnable{
		
		private String command;
		private MessageID messageID;
		private int TTL;
		private String fileName;
		private Message message;
		String IP;
		int port;

		public hitQueryThread(MessageID messageID, int TTL, String fileName, String IP, int port){
			this.command = "hitQuery";
			this.messageID = messageID;
			this.TTL = TTL;
			this.fileName = fileName;
			this.IP = IP;
			this.port = port;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			// Do not need to change the sequenceNumber
			Node upstream = messageID.getPeerID();
			// Set local peer name to messageID
			MessageID messageid = new MessageID(messageID.getSequenceNumber(), peerInfo.local.nick);
//			messageID.setPeerID(peerInfo.local.nick);
			message = new Message(command, messageid, TTL, fileName, IP, port);
			
			try {
				FileWriter writer = new FileWriter("./peerLog.txt",true);
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String time = df.format(new Date());
				writer.write(time + "\t\tSend:"+command + " " + message.getMessageID().getSequenceNumber() 
						+ " " +message.getfileName()+" "+message.getMessageID().getPeerID().peerName+"\t\n");
				writer.close();	
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			System.out.print("Send:"+command + " " + message.getMessageID().getSequenceNumber() + " " +message.getfileName()+" ");
//        	System.out.println(message.getMessageID().getPeerID().peerName);
			new clientThread(message, upstream.IP, upstream.port);
		}
		
	}
	
	class downloadThread implements Runnable{

		private Message message;
		private String command;
		private String fileName;
		private int indexNum;
		String IP;
		int port;
		
		public downloadThread(String fileName, int indexNum, String IP, int port){
			this.command = "download";
			this.fileName = fileName;
			this.indexNum = indexNum;
			this.IP = IP;
			this.port = port;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			message = new Message(command, fileName, IP, port);
			new clientThread(message, peerInfo.dest.destPeer.get(indexNum-1).IP, peerInfo.dest.destPeer.get(indexNum-1).port);
		}
		
	}
	
	class clientThread extends Thread{
		private String IP;
		private int port;
		private Message message;
		
		public clientThread(Message message, String IP, int port){
			this.message = message;
			this.IP = IP;
			this.port = port;
			start();
		}
		
		public void run(){
			Socket socket = null;
			ObjectOutputStream os = null;  
            
			try {
				socket = new Socket(IP,port);
				os = new ObjectOutputStream(socket.getOutputStream());
				os.writeObject(message);
				os.flush();
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}finally {   
                if (os != null)
					try {
						os.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}  
                if (socket != null)
					try {
						socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}   
            }
		}
	}
	
}


class SThread extends Thread{
	String fileName = null;
	String IP = null;
	int port = 0;
	public SThread(String fileName, String IP, int port){
		this.fileName = fileName;
		this.IP = IP;
		this.port = port;
		start();
	}
	
	public void run(){

		int length = 0;  
        double sumL = 0 ;  
        byte[] sendBytes = null;  
        Socket socket = null;  
        DataOutputStream dos = null;  
        FileInputStream fis = null;  
        boolean bool = false;
     
        try {  
            File file = new File("./share/" + fileName); 
            long l = file.length();   
            socket = new Socket(IP,port);                
            dos = new DataOutputStream(socket.getOutputStream());  
            fis = new FileInputStream(file);        
            sendBytes = new byte[1024];   
            
            while ((length = fis.read(sendBytes, 0, sendBytes.length)) > 0) {  
                sumL += length;               
                System.out.println("Sent:"+((sumL/l)*100)+"%");
                dos.write(sendBytes, 0, length);  
                dos.flush();  
            }   
            //
            if(sumL==l){  
                bool = true;  
                try {
					FileWriter writer = new FileWriter("./peerLog.txt",true);
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String time = df.format(new Date());
					writer.write(time + "\t\tSend " + fileName + "successfully!\t\n");
					writer.close();	
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }  
            
        }catch (Exception e) {  
            System.out.println("error");  
            bool = false;  
            e.printStackTrace();    
        }finally{    
            if (dos != null)
				try {
					dos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  
            if (fis != null)
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}     
            if (socket != null)
				try {
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}      
        }  
        System.out.println(bool?"Success":"Fail");  
        
	}
}
