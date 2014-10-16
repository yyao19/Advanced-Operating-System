package Server;

import java.io.*;
import java.net.*;

public class Server {
	
	public void service() throws IOException{
		serverFunction serverfunction = new serverFunction();
		ServerSocket serverSocket;
		indexServer indexserver = new indexServer();
		serverSocket = indexserver.serversocket;
		try{
			Socket socket = null;
			while(true){
				socket = serverSocket.accept();
//				System.out.println("New connection accepted! ");
				new ServerThread(socket,serverfunction);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args)throws IOException{
		new Server().service();
	}
}

class indexServer{
	public ServerSocket serversocket;
	public int port;
	
	public indexServer()throws IOException{
		port = 8010;
		serversocket = new ServerSocket(port);
		System.out.println("Start Server");
	}
	
	public indexServer(int port)throws IOException{
		this.port = port;
		serversocket = new ServerSocket(port);
		System.out.println("Start Server");
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
