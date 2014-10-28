package Peer;

import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;

import Peer.peerFunction.clientThread;

public class PeerThread extends Thread{
	private ServerSocket serversocket;
	private Socket socket;
	private BufferedReader br;
	public peerFunction peerfunc;
	
	public PeerThread(ServerSocket serversocket, peerFunction peerfunc)throws IOException{
		super();
		this.serversocket = serversocket;	
		this.peerfunc = peerfunc;
		start();
	}
	
	public void run(){  
	    Socket socket = null;         
		try{
			while(true){	
				socket = serversocket.accept();	
				invoke(socket, peerfunc);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				if(socket!=null){
					socket.close();
				}
			}catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	
	private static void invoke(final Socket socket, final peerFunction peerfunc) throws IOException {
		new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				ObjectInputStream is = null;  
                ObjectOutputStream os = null; 
                String command;
                MessageID messageID;
                int TTL;
                String fileName;
                String IP = null;
                int port = -1;
                boolean flag = false;
                // Record the number of the hitQuery message
                
                DateFormat df;
                String time;
                FileWriter writer = null;
                try {  
                	writer = new FileWriter("./peerLog.txt",true);
                	
                    is = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));  
                    os = new ObjectOutputStream(socket.getOutputStream());  
  
                    Object obj = is.readObject();  
                    Message message = (Message)obj;  
                    
                    command = message.getCommand();
  
                    if("query".equals(command)){
                    	
                    	df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            			time = df.format(new Date());
            			writer.write(time + "\t\tReceive: "+command + " " + message.getMessageID().getSequenceNumber() + " " +message.getfileName()+" "
            					+ message.getMessageID().getPeerID().peerName + "\t\n");
                    	
//                    	System.out.print("Receive:"+command + " " + message.getMessageID().getSequenceNumber() + " " +message.getfileName()+" ");
//                    	System.out.println(message.getMessageID().getPeerID().peerName);
                    	
                    	messageID = message.getMessageID();
                    	TTL = message.getTTL();
                    	fileName = message.getfileName();
                    	
                    	if(TTL>0){
                    		TTL = TTL - 1;                   		
                    		
                    		peerfunc.query(messageID, TTL, fileName);
                    		flag = peerfunc.search(fileName);
                    		if(flag){
                    			System.out.println(fileName+" is on "+peerInfo.local.nick.peerName);
                    			peerfunc.hitQuery(messageID, TTL, fileName, peerInfo.local.nick.IP, peerInfo.local.nick.port);
                    			
                    			df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    			time = df.format(new Date());
                    			writer.write(time + "\t\tFile "+fileName + " is found on " + peerInfo.local.nick.peerName+"\r\n");
                    				
                    			
                    		}else{
                    			peerfunc.hitQuery(messageID, TTL, fileName, IP, port);
                    		}
                    	}else{
                    		flag = peerfunc.search(fileName);
                    		if(flag){
                    			// hitQuery messageID = messageID
                    			// hitQuery keep messageID num unchanged
                    			
                    			df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    			time = df.format(new Date());
                    			writer.write(time + "\t\tFile "+fileName + " is found on " + peerInfo.local.nick.peerName+"\r\n");
                    			
                    			System.out.println(fileName+" is on "+peerInfo.local.nick.peerName);
                    			peerfunc.hitQuery(messageID, TTL, fileName, peerInfo.local.nick.IP, peerInfo.local.nick.port);            			
                    		}else{
                    			peerfunc.hitQuery(messageID, TTL, fileName, IP, port);
                    		}
                    	}
                    	
                    }else if("hitQuery".equals(command)){
                    	
                    	df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            			time = df.format(new Date());
            			writer.write(time + "\t\tReceive: "+command + " " + message.getMessageID().getSequenceNumber() + " " +message.getfileName()+" "
            					+ message.getMessageID().getPeerID().peerName + "\t\n");
                    	
//                    	System.out.print("Receive:"+command + " " + message.getMessageID().getSequenceNumber() + " " +message.getfileName()+" ");
//                    	System.out.println(message.getMessageID().getPeerID().peerName);
                    	
                    	TTL = message.getTTL();
                    	
                    	if(TTL>=0){
                    		TTL = TTL - 1;
                    		peerfunc.searchMessage(message, TTL);
                        		
                        }
                    	
                    }else if("download".equals(command)){
                    	// The peer who needs to down load the file
                    	// fileName IP and Port 
                    	fileName = message.getfileName();
                    	String peerip = message.getPeerIP();
                    	int peerport = message.getPort();
                    	peerfunc.sendFile(fileName, peerip, peerport);
                    }
//                    os.writeObject(user);  
//                    os.flush();  
                    writer.close();
                } catch (IOException e) {  
                    e.printStackTrace();
                } catch(ClassNotFoundException e) {  
                    e.printStackTrace(); 
                } finally {  
                    try {  
                        is.close();  
                    } catch(Exception ex) {}  
                    try {  
                        os.close();  
                    } catch(Exception ex) {}  
                    try {  
                        socket.close();  
                    } catch(Exception ex) {}  
                }  
			}
			
		}).start();
	}
}


/*
 *  Receive Thread
 *  Waiting for file
 *  Receiving file from other client
 */
class RThread extends Thread{
	String fileName = null;
	String IP = null;
	int port = 0;
	public RThread(String fileName, String IP, int port){
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
     
        FileWriter writer = null;
        
        try {  
        	writer = new FileWriter("./peerLog.txt",true);
        	
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
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    			String time = df.format(new Date());
    			writer.write(time + "\t\tFile "+fileName + " is successfully received!\r\n");
            } 
            
            writer.close();	
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

