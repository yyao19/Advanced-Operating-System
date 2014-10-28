package Peer;

import java.io.Serializable;

public class MessageID implements Serializable{
	private static final long serialVersionUID = 1L;
	private int sequenceNumber;
	private Node peerID;
	
	public MessageID(int sequenceNumber, Node peerID){
		this.sequenceNumber = sequenceNumber;
		this.peerID = peerID;
	}
	
	public void setSequenceNumber(int sequenceNumber){
		this.sequenceNumber = sequenceNumber;
	}

	public void setPeerID(Node peerID){
		this.peerID = peerID;
	}
	
	public int getSequenceNumber(){
		return sequenceNumber;
	}
	
	public Node getPeerID(){
		return peerID;
	}
}
