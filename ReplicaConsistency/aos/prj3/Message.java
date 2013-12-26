package aos.prj3;

import java.io.Serializable;

/**
 * Message Class gives you function to create and set/get various 
 * parts of message.  message get exchanged between two processes
 * @author Rahul Gaikwad
 *
 */
public class Message implements Serializable{


	 enum MSGTYPE { REQUEST, GRANT, COMMIT, ACK, WITHDRAW, REACTIVATE, DEACTIVATE, BROADCAST, UPD_REQ, UPD_REPLY, CONNECT, START, FREEZ, UNFREEZ};
	 
	int sourceId,destId;
	int[] vectorClock;
    MSGTYPE type;
	String data; 
	//Data[] dataObject = new Data[4];
    private int dataObjectNo,dataObjectValue,reqId;
    private static final long serialVersionUID = 1L;
    
    // Empty Constructor
    public Message(){}
    

    public Message(int srcNodeId, int dstNodeId, MSGTYPE type, int[] clockval) {
		this.sourceId = srcNodeId;
		this.destId = dstNodeId;
		this.type = type;
		this.vectorClock = clockval;
		this.data = null;
	}
    
    public Message(int srcNodeId, int dstNodeId, MSGTYPE type, int[] clockval, String data) {
		this.sourceId = srcNodeId;
		this.destId = dstNodeId;
		this.type = type;
		this.vectorClock = clockval;
		this.data = data;
	}
    
    /**
     * Construct Message from given message string
     * @param message 
     */
    
    public Message(String message) {
    	String[] msgParts = message.split(" ");
		this.sourceId = Integer.parseInt(msgParts[0]);
		this.destId = Integer.parseInt(msgParts[1]);
		this.type = getmsgType(msgParts[2]);
		this.vectorClock = conStringToVector(msgParts[3]);;
		if(msgParts.length > 4)
			this.data = msgParts[4];
		else
			this.data = null;
	}
    
    

    private int[] conStringToVector(String string) {
    	
    	String[] strParts = string.replace("[", "").replace("]", "").split(",");
    	int[] vectorClock = new int[strParts.length];
    	
    	for(int i = 0; i < strParts.length; i++)
    		vectorClock[i] = Integer.parseInt(strParts[i]);
    	
		return vectorClock;
	}
    
    
   private String conVectorToString() {
    	
    	String str="[";
    	
    	if(vectorClock.length>0)
    		str = str + String.valueOf(vectorClock[0]) ;
    	
    	for(int i = 1; i < vectorClock.length; i++)
    		str = str+","+vectorClock[i];
    	
    	str = str + "]";
    	
		return str;
	}


	/**
     * Convert String type too MSGTYPE
     * @param type
     * @return
     */
    private MSGTYPE getmsgType(String type) {
    	//REQUEST, GRANT, COMMIT, ACK, WITHDRAW, REACTIVATE, DEACTIVATE, BROADCAST, UPD_REQ, UPD_REPLY,CONNECT, START
    	if(type.trim().equalsIgnoreCase("REQUEST"))
    		return MSGTYPE.REQUEST;
    	else if(type.trim().equalsIgnoreCase("GRANT"))
    		return MSGTYPE.GRANT;
    	else if(type.trim().equalsIgnoreCase("COMMIT"))
    		return MSGTYPE.COMMIT;
    	else if(type.trim().equalsIgnoreCase("ACK"))
    		return MSGTYPE.ACK;
    	else if(type.trim().equalsIgnoreCase("WITHDRAW"))
    		return MSGTYPE.WITHDRAW;
    	else if(type.trim().equalsIgnoreCase("REACTIVATE"))
			return MSGTYPE.REACTIVATE;
    	else if(type.trim().equalsIgnoreCase("DEACTIVATE"))
			return MSGTYPE.DEACTIVATE;
    	else if(type.trim().equalsIgnoreCase("UPD_REQ"))
			return MSGTYPE.UPD_REQ;
    	else if(type.trim().equalsIgnoreCase("UPD_REPLY"))
			return MSGTYPE.UPD_REPLY;
    	else if(type.trim().equalsIgnoreCase("BROADCAST"))
			return MSGTYPE.BROADCAST;
    	else if(type.trim().equalsIgnoreCase("CONNECT"))
			return MSGTYPE.CONNECT;
    	else if(type.trim().equalsIgnoreCase("START"))
			return MSGTYPE.START;
    	else if(type.trim().equalsIgnoreCase("FREEZ"))
			return MSGTYPE.FREEZ;
    	else if(type.trim().equalsIgnoreCase("UNFREEZ"))
			return MSGTYPE.UNFREEZ;
   
		return null;
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
	
	public synchronized int[] getVectorClock() {
		return vectorClock;
	}

	public synchronized void setVectorClock(int[] clockTime) {
		this.vectorClock = clockTime;
	}

	public synchronized MSGTYPE getMessageType() {
		return type;
	}

	public String getData() { 
		return data; 
	}

	public void setdata(String data) { 
		this.data = data; 
	}

	public synchronized int getObjectNo() {
		String[] dataPart = getData().split(",");
		this.dataObjectNo = Integer.parseInt(dataPart[0]);		
		return dataObjectNo;
	}

	public synchronized int getDataValue() {
		String[] dataPart = getData().split(",");
		this.dataObjectValue = Integer.parseInt(dataPart[1]);
		return dataObjectValue;
	}
	
	public synchronized int getReqId() {
		String[] dataPart = getData().split(",");
		this.reqId = Integer.parseInt(dataPart[2]);
		return reqId;
	}
	
	public synchronized void setDataObjectNo(int dataObjectNo) {
		this.dataObjectNo = dataObjectNo;
		data = new Integer(dataObjectNo).toString();
		
	}
	public synchronized void setDataObjectValue(int dataObjectValue) {
		this.dataObjectValue = dataObjectValue;
		data += "," + new Integer(dataObjectValue).toString();
	}
	public synchronized void setReqId(int reqId) {
		this.reqId = reqId;
		data += "," + new Integer(reqId).toString();
	}
	
	@Override
	public String toString() {
			return  sourceId + " " + destId + " " + type + " " + conVectorToString() + " " + data;
	}

}
