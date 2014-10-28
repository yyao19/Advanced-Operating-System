package Peer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class Peer {
	
	/*
	 *  Read configure file: config.txt
	 *  Set up IP address and port of local peer
	 */
	public static void readConfig(String config){
		FileWriter writer = null;
		String s = new String();
		try{
			writer = new FileWriter("./peerLog.txt",true);
			
			File file = new File(config);
			if(file.isFile() && file.exists()){
				InputStreamReader read = new InputStreamReader(new FileInputStream(file));
				BufferedReader bufferReader = new BufferedReader(read);
				while((s = bufferReader.readLine()) != null){
					peerInfo.local.peerNum++;
					String info[] = s.split(" ");
					if(peerInfo.local.nick.peerName.equals(info[0])){
						peerInfo.local.nick.IP = info[1];
						peerInfo.local.nick.port = Integer.parseInt(info[2]);
						if(info.length>2) 
						for(int i = 3; i < info.length; i++){
							BufferedReader reader = new BufferedReader(new FileReader(file));
							while((s = reader.readLine()) != null){
							String temp[] = s.split(" ");
							if(info[i].equals(temp[0])){
							Node node = new Node(temp[0], temp[1], Integer.parseInt(temp[2]));
							peerInfo.local.neighbor.add(node);
							writer.write(peerInfo.local.nick.peerName + " neighor peer information:");
							writer.write(node.peerName + " ");
							}
							}
						}
						writer.write("\t\n");
						System.out.println("Local peer information:");
						peerInfo.local.nick.NodeInfo();
						System.out.println("Neighbor peers information:");
						
						for(int i = 0; i<peerInfo.local.neighbor.size(); i++){
							peerInfo.local.neighbor.get(i).NodeInfo();
						}
					}
				}
			}else{
				System.out.println("Configure file is not exist!");
				writer.write("Configure file is not exist!\r\n");
			}		
			writer.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/*
	 *  File read function
	 *  Scan the file folder 
	 *  and register all files in the local file list.
	 */
	public void fileMonitor(String path){
		File file = new File(path);
		if(!file .exists() && !file.isDirectory()){        
		    file .mkdir();    
		} 
		String test[];
		test = file.list();
		if(test.length!=0){
			for(int i = 0; i<test.length; i++){
//				register(test[i]);
			}
		}
	}
	
	/*  Register function
	 *  Set up a socket connection to the index server
	 *  Register the file to the index server
	 */ 
	public static void register(String fileName){
		FileWriter writer = null;

		try{		
			File file = new File(peerInfo.local.path + File.separator +
					fileName);
			if(!file.exists()){
				System.out.println(fileName+" is not exist!");
				
			}else{
				writer = new FileWriter("./peerLog.txt",true);	
				
				peerInfo.local.fileList.add(fileName);
				System.out.println("File "+fileName + " is registered!");
				
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String time = df.format(new Date());
				writer.write(time + "\t\tFile "+fileName + " is registered on the local peer!\r\n");
				writer.close();	
			}	

		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/*  Unregister function
	 *  Set up a socket connection to the index server
	 *  Unregister the file to the index server
	 */ 
	public static void unregister(String fileName){
		FileWriter writer = null;
		try{	
			writer = new FileWriter("./peerLog.txt",true);
			peerInfo.local.fileList.remove(fileName);		
			System.out.println("File "+fileName + " is unregistered!");
			
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time = df.format(new Date());
			writer.write(time + "\t\tFile "+fileName + " is unregistered on the index server!\r\n");
			writer.close();	

		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
public void talk(peerFunction peerfunc)throws IOException{
		
		boolean exit = false;
		// Store file name
		String fileName = null;
		
		BufferedReader localReader = new BufferedReader(new InputStreamReader(System.in));
		
		// Usage Interface
		while(!exit){
			System.out.println("\n1 Set up peer name\n2 Register a file\n3 Search a file\n4 Exit");
			switch(Integer.parseInt(localReader.readLine())){
			case 1:{					
				System.out.println("Enter the peer name:");
				peerInfo.local.nick.peerName = localReader.readLine();
				peerInfo.initial();
				readConfig(peerInfo.local.config);
				ServerSocket server = null;
			    try{
			    	server = new ServerSocket(peerInfo.local.nick.port);
			    	System.out.println("\nServer started!");
			    	new PeerThread(server, peerfunc);
			    }catch(IOException e){
			    	e.printStackTrace();
			    }
				break;
			}
			
			case 2:{
				System.out.println("Enter the file name:");
				fileName = localReader.readLine();
				// Register file to the index server
				register(fileName);			
				break;	
			}
			case 3:{			
//				peerInfo.local.messageTable = new HashMap<Integer,MessageID>();
				peerInfo.dest.destPeer = new ArrayList<Node>();
				peerInfo.local.hitQueryRequest = 0;
				System.out.println("Enter the file name:");
				fileName = localReader.readLine();
				System.out.println("\nStart processing...\n");
				// Assemble messageID
				int num = peerInfo.local.messageNum + 1;
				MessageID messageID = new MessageID(num, peerInfo.local.nick);
            	
            	// Send query message to the neighbors
				peerfunc.query(messageID, peerInfo.local.TTL-1, fileName);
				
				long runtime = 0;
				long start = System.currentTimeMillis();
				// Set cutoff time = 3s
				while(runtime<peerInfo.local.cutoffTime){
					long end = System.currentTimeMillis();
					runtime = end - start;
				}

				
				if(peerInfo.dest.destPeer.size()!=0){
					int index = 0;
					int indexNum = 0;
					System.out.println(fileName + " was found on peers!");
					System.out.println("\n1 Download the file\n2 Cancel and back");
					switch(Integer.parseInt(localReader.readLine())){
					case 1:	
						System.out.println("The destination peer list is:");
						for(int i=0; i<peerInfo.dest.destPeer.size(); i++){
							index = i + 1;
							System.out.println(index + ":" + peerInfo.dest.destPeer.get(i).IP + " " + peerInfo.dest.destPeer.get(i).port);
						}
						System.out.println("Chose which peer to download the file:");
						indexNum = Integer.parseInt(localReader.readLine());

						new DThread(peerInfo.local.nick.port+1,fileName);
						peerfunc.downLoad(fileName, indexNum, peerInfo.local.nick.IP, peerInfo.local.nick.port+1);					
						break;
					case 2:
						break;
					default:
						break;			
					}
				}else{
					System.out.println(fileName + " was not found on peers!");
				}
				break;
			}
			
			case 4:{
				exit = true;
				System.exit(0);
				break;
			}
			default:
				break;
			}
			
		}	
	}
	
	public static void main(String args[]){
		Peer peer = new Peer();
		peerFunction peerfunc = new peerFunction();
		peer.fileMonitor(peerInfo.local.path);
		new WThread(peerInfo.local.path);
		try {
			peer.talk(peerfunc);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}

/*
 *  Watch file
 *  Listening to the local file folder
 *  When there is a change, register or 
 *  unregister file in the local list.
 */
class WThread extends Thread {
	String path = null;
//	peerInfo.local.fileList
	public WThread(String path){
		this.path = path;
		// Record the original file list in the folder
		start();
	}
	
	public void run(){
		Timer timer = new Timer();
		timer.schedule(new TimerTask(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(peerInfo.local.fileList.size()!=0){
					for(int i = 0; i < peerInfo.local.fileList.size(); i++){
						File file = new File(path + File.separator +
								peerInfo.local.fileList.get(i));
						if(!file.exists()){
							System.out.println(peerInfo.local.fileList.get(i)+" was removed!");
							Peer.unregister(peerInfo.local.fileList.get(i));
							
						}
					}
				}
			}
			
		}, 1000, 100);
		     
	}
}


/*
 *   Used to receive file from file client
 *   Step 1. Set up a server socket
 *   Step 2. Waiting for input data 
 */
class DThread extends Thread{
	int port;
	String fileName;
	public DThread(int port,String fileName){
		this.port = port;
		this.fileName = fileName;
		start();
	}
	
	public void run(){
		try {
			ServerSocket server = new ServerSocket(port);
			//while(true){
				Socket socket = server.accept();  
                receiveFile(socket,fileName);  
                socket.close();
                server.close();
			//}
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	// Receive function used to receive file and save the file in the local machine
	public static void receiveFile(Socket socket, String fileName) throws IOException{
		byte[] inputByte = null;  
        int length = 0;  
        DataInputStream dis = null;  
        FileOutputStream fos = null;  
        String filePath = "./share/" + fileName;  
        try {  
            try {  
                dis = new DataInputStream(socket.getInputStream());  
                File f = new File("./share");  
                if(!f.exists()){  
                    f.mkdir();    
                }  

                fos = new FileOutputStream(new File(filePath));      
                inputByte = new byte[1024];     
                System.out.println("\nStart receiving..."); 
                System.out.println("display file " + fileName);
                while ((length = dis.read(inputByte, 0, inputByte.length)) > 0) {  
                    fos.write(inputByte, 0, length);  
                    fos.flush();      
                }  
                System.out.println("Finish receive:"+filePath);  
            } finally {  
                if (fos != null)  
                    fos.close();  
                if (dis != null)  
                    dis.close();  
                if (socket != null)  
                    socket.close();   
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
	}
}




