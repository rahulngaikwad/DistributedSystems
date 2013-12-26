package aos.prj1;

import java.io.Serializable;

public class Message implements Serializable{


	private static final long serialVersionUID = 1L;
	int sourceId,destId, clockTime;
    String msgType; // INIT, COMP or ACK
    
    // Empty Constructor
    public Message(){}
    
    /**
     * Construct Message from given message parameters
     * @param srcNodeId
     * @param dstNodeId
     * @param clockval
     * @param type
     */
    public Message(int srcNodeId, int dstNodeId, int clockval, String type) {
		this.sourceId = srcNodeId;
		this.destId = dstNodeId;
		this.clockTime = clockval;
		this.msgType = type;
	}
    
    /**
     * Construct Message from given message string
     * @param message 
     */
    public Message(String message) {
    	String[] msgParts = message.split(" ");
		this.sourceId = Integer.parseInt(msgParts[0]);
		this.destId = Integer.parseInt(msgParts[1]);
		this.clockTime = Integer.parseInt(msgParts[2]);;
		this.msgType = msgParts[3];
	}

    // Getters And Setters Section
	public synchronized int getSourceId() {
		return sourceId;
	}

	public synchronized void setSourceId(int sourceId) {
		this.sourceId = sourceId;
	}

	public int getDestId() {
		return destId;
	}

	public void setDestId(int destId) {
		this.destId = destId;
	}

	public synchronized int getclockTime() {
		return clockTime;
	}

	public synchronized void setclockTime(int clockTime) {
		this.clockTime = clockTime;
	}

	public synchronized String getMessageType() {
		return msgType;
	}


    
	@Override
	public String toString() {
		return  sourceId + " " + destId + " " + clockTime + " " + msgType;
	}

}
