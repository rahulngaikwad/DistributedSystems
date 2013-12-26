package aos.prj2;

import java.io.Serializable;

/**
 * Message Class gives you function to create and set/get various 
 * parts of message.  message get exchanged between two processes
 * @author Rahul Gaikwad
 *
 */
public class Message implements Serializable{


	 enum MSGTYPE { REQUEST,REPLY,RELEASE,FAILED,INQUIRE,YIELD,START,INFO,BROADCAST,CS_EXIT,CS_ENTER,CONNECT};
	 
	int sourceId,destId, clockTime;
    MSGTYPE type;
	String data; // INIT, COMP or ACK
    int reqId,csTime;
    private static final long serialVersionUID = 1L;

    int noOfREQUESTs,noOfREPLYs,noOfRELEASEs,noOfFAILs,noOfENQUIREs,noOfYEILDs;
    
    // Empty Constructor
    public Message(){}
    

    public Message(int srcNodeId, int dstNodeId, int clockval, MSGTYPE type) {
		this.sourceId = srcNodeId;
		this.destId = dstNodeId;
		this.clockTime = clockval;
		this.type = type;
		this.data = null;
	}
    
    public Message(int srcNodeId, int dstNodeId, int clockval, MSGTYPE type,String data) {
		this.sourceId = srcNodeId;
		this.destId = dstNodeId;
		this.clockTime = clockval;
		this.type = type;
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
		this.clockTime = Integer.parseInt(msgParts[2]);;
		this.type = getmsgType(msgParts[3]);
		if(msgParts.length > 4)
			this.data = msgParts[4];
		else
			this.data = null;
	}

    /**
     * Convert String type too MSGTYPE
     * @param type
     * @return
     */
    private MSGTYPE getmsgType(String type) {
    	
    	if(type.trim().equalsIgnoreCase("REQUEST"))
    			return MSGTYPE.REQUEST;
    	else if(type.trim().equalsIgnoreCase("REPLY"))
    			return MSGTYPE.REPLY;
    	else if(type.trim().equalsIgnoreCase("RELEASE"))
    			return MSGTYPE.RELEASE;
    	else if(type.trim().equalsIgnoreCase("FAILED"))
    			return MSGTYPE.FAILED;
    	else if(type.trim().equalsIgnoreCase("INQUIRE"))
    			return MSGTYPE.INQUIRE;
    	else if(type.trim().equalsIgnoreCase("YEILD"))
    			return MSGTYPE.YIELD;
    	else if(type.trim().equalsIgnoreCase("CS_EXIT"))
    			return MSGTYPE.CS_EXIT;
    	else if(type.trim().equalsIgnoreCase("START"))
			return MSGTYPE.START;
    	else if(type.trim().equalsIgnoreCase("INFO"))
    			return MSGTYPE.INFO;
    	else if(type.trim().equalsIgnoreCase("BROADCAST"))
    			return MSGTYPE.BROADCAST;
    	else if(type.trim().equalsIgnoreCase("CS_ENTER"))
    			return MSGTYPE.CS_ENTER;
    	else if(type.trim().equalsIgnoreCase("CONNECT"))
			return MSGTYPE.CONNECT;
   
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
	
	public synchronized int getclockTime() {
		return clockTime;
	}

	public synchronized void setclockTime(int clockTime) {
		this.clockTime = clockTime;
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

	
	public synchronized int getReqId() {
		String[] dataPart = getData().split(",");
		 this.reqId = Integer.parseInt(dataPart[0]);	
		return reqId;
	}


	public synchronized void setReqId(int reqId) {
		this.reqId = reqId;
		data = new Integer(reqId).toString();
	}


	public synchronized int getCsTime() {
		String[] dataPart = getData().split(",");
		 this.csTime = Integer.parseInt(dataPart[1]);		
		return csTime;
	}


	public synchronized void setCsTime(int csTime) {
		this.csTime = csTime;
		data += "," + new Integer(csTime).toString();
	}


	public synchronized int getNoOfREQUESTs() {
		String[] dataPart = getData().split(",");
		this.noOfREQUESTs = Integer.parseInt(dataPart[0]);		
		return noOfREQUESTs;
	}

	public synchronized int getNoOfREPLYs() {
		String[] dataPart = getData().split(",");
		this.noOfREPLYs = Integer.parseInt(dataPart[1]);
		return noOfREPLYs;
	}
	
	public synchronized int getNoOfRELEASEs() {
		String[] dataPart = getData().split(",");
		this.noOfRELEASEs = Integer.parseInt(dataPart[2]);
		return noOfRELEASEs;
	}

	public synchronized int getNoOfFAILs() {
		String[] dataPart = getData().split(",");
		this.noOfFAILs = Integer.parseInt(dataPart[3]);		
		return noOfFAILs;
	}

	public synchronized int getNoOfENQUIREs() {
		String[] dataPart = getData().split(",");
		this.noOfENQUIREs = Integer.parseInt(dataPart[4]);
		return noOfENQUIREs;
	}

	public synchronized int getNoOfYEILDs() {
		String[] dataPart = getData().split(",");
		this.noOfYEILDs = Integer.parseInt(dataPart[5]);
		return noOfYEILDs;
	}


	public synchronized void setNoOfREQUESTs(int noOfREQUESTs) {
		this.noOfREQUESTs = noOfREQUESTs;
		data = new Integer(noOfREQUESTs).toString();
		
	}
	public synchronized void setNoOfREPLYs(int noOfREPLYs) {
		this.noOfREPLYs = noOfREPLYs;
		data += "," + new Integer(noOfREPLYs).toString();
	}

	public synchronized void setNoOfRELEASEs(int noOfRELEASEs) {
		this.noOfRELEASEs = noOfRELEASEs;
		data += "," + new Integer(noOfRELEASEs).toString();
	}

	public synchronized void setNoOfFAILs(int noOfFAILs) {
		this.noOfFAILs = noOfFAILs;
		data += "," + new Integer(noOfFAILs).toString();
	}

	public synchronized void setNoOfENQUIREs(int noOfENQUIREs) {
		this.noOfENQUIREs = noOfENQUIREs;
		data += "," + new Integer(noOfENQUIREs).toString();
	}

	public synchronized void setNoOfYEILDs(int noOfYEILDs) {
		this.noOfYEILDs = noOfYEILDs;
		data += "," + new Integer(noOfYEILDs).toString();
	}


	@Override
	public String toString() {
			return  sourceId + " " + destId + " " + clockTime + " " + type + " " + data;
	}

}
