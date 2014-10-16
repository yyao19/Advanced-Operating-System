package Server;

public class fileInfo {
	
	private String peerID;
	private String fileName;
	
	public fileInfo(){
		
	}
	
	public fileInfo(String peerID,String fileName){
		this.peerID = peerID;
		this.fileName = fileName;
	}
	
	public String getID(){
		return peerID;
	}
	
	public String getName(){
		return fileName;
	}
	
	public void setID(String peerID){
		this.peerID = peerID;
	}
	
	public void setName(String fileName){
		this.fileName = fileName;
	}

}
