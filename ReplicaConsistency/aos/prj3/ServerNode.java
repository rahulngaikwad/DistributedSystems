package aos.prj3;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.swing.Timer;

import aos.prj3.Message.MSGTYPE;

/**
 * ServerNode Class Simulate the Actual Physical server
 * which exchange messages to grant the access foe data object. 
 * Maintains Own Connection pool.
 * @author Rahul Gaikwad
 */
public class ServerNode extends Node implements ActionListener{

	enum STATUS { ACTIVE,DEACTIVE,FREEZING,RECOVERING};
	STATUS processStatus;
	
    int noOfRequest,freezedBy;
	Data[] DataObj; 
	int[] lastReqRecvd;
	ArrayList<Message> bufferQue;
	int noOfREQUESTrecvd,noOfCOMMITrecvd,noOfWITHDRAWrecvd,noOfGRANTsent,noOfACKsent;
	int lastAck;
	
	Logger logger = Logger.getLogger(ServerNode.class.getName());
	
	
	/**
	 * Constructor which accepts following parameters
	 * @param NodeId : nodes id
	 * @param noOFServers : total no of Servers
	 * @param noOFClients : total no of Clients
	 * @param noOfRequest : total no of request
	 * @param timeUnit :  time unit
	 * @param config : configuration 
	 */
	public ServerNode(int nodeId, int noOFServers, int noOFClients,int noOfRequest,int timeUnit,int[] failingNodes,HashMap<Integer,String> config){
		super(nodeId,noOFServers,noOFClients,timeUnit,failingNodes,config);
		this.noOfRequest = noOfRequest;
		DataObj = new Data[4];
		lastReqRecvd = new int[noOFClients];
		bufferQue = new ArrayList<Message>();
	}

	/**
	 * Handles all received message
	 * Update clock time
	 */
	@Override
	public  synchronized void handleMessage(Message msg){
		
    	
		if(processStatus == STATUS.DEACTIVE && msg.getMessageType() != MSGTYPE.REACTIVATE)
			return;
		
		if(processStatus == STATUS.FREEZING && msg.getMessageType() == MSGTYPE.REQUEST) //ignore all new Request
			return;
			
		if(msg.getMessageType() == MSGTYPE.REQUEST)
    			handleRequestMsg(msg);
		else if(msg.getMessageType() == MSGTYPE.WITHDRAW)
       			handleWithdrawMsg(msg);
		else if(msg.getMessageType() == MSGTYPE.COMMIT)
				handleCommitMsg(msg);
    	else if(msg.getMessageType() == MSGTYPE.REACTIVATE)
    			handleReactivateMsg(msg);
    	else if(msg.getMessageType() == MSGTYPE.DEACTIVATE)
    			handleDeactivateMsg(msg);
    	else if(msg.getMessageType() == MSGTYPE.UPD_REQ)
    			handleUpdateRequestMsg(msg);//handleInfoMsg(msg);
    	else if(msg.getMessageType() == MSGTYPE.UPD_REPLY)
    			handleUpdateReplyMsg(msg);
    	else if(msg.getMessageType() == MSGTYPE.FREEZ)
				handleFreezMsg(msg);
    	else if(msg.getMessageType() == MSGTYPE.UNFREEZ)
    			handleUnFreezMsg(msg);
    	else
    		 log("Warnning !!!!!!!! Wrong Message Received "+msg); 
		
		 synchronized (msgQueue){
			 msgQueue.notifyAll();
		 }
    }

	private void handleUnFreezMsg(Message msg) {
		processStatus = STATUS.ACTIVE;
		
	}

	private void handleFreezMsg(Message msg) {
		processStatus = STATUS.FREEZING;
		
	}

	/**
	 * Handles Request message.
	 * If a server has not granted any request for requested data object
	 * then the  send a GRANT message to client and enters a blocked
	 * state corresponding to this object
	 * @param msg
	 */
	private synchronized void handleRequestMsg(Message msg) {
		
		noOfREQUESTrecvd++;
		
		int absClientId = msg.getSourceId()-noOfServers;
		
		if( msg.getReqId()>lastReqRecvd[absClientId])
			lastReqRecvd[absClientId] = msg.getReqId();
		
		log.println("lastReqRecvd[" + (msg.getSourceId()-noOfServers) +"] = "+ lastReqRecvd[msg.getSourceId()-noOfServers] );
		
		Data dataObject = DataObj[msg.getObjectNo()];
		
		dataObject.getQueue().add(msg);
		
		if(!dataObject.isLocked())
		{
			dataObject.setLocked(true);
			dataObject.lockedBy = msg.getSourceId();
			Message newMsg = new Message(getNodeId(),msg.getSourceId(),MSGTYPE.GRANT,vectorClock,msg.getData());
			sendMsg(newMsg,true);
		}
	}
	
	/**
	 * Handles COMMIT message.
	 * Removes senders request from waitingQueue.
	 * sends  message to next eligible process
	 * @param msg
	 */
	private synchronized void handleCommitMsg(Message msg) {
		
		noOfCOMMITrecvd++;
		Data dataObject = DataObj[msg.getObjectNo()];
		
		dataObject.incrVersionNumber();
		dataObject.incrDataBy(msg.getDataValue());
		dataObject.setLastUpdatedByMsg(msg);
		
		log.println("dataObject"+msg.getObjectNo() +" " +dataObject);

		//Send ACK to client
		Message newMsg = new Message(getNodeId(),msg.getSourceId(),MSGTYPE.ACK,vectorClock,msg.getData());
		sendMsg(newMsg,true);
		
		//Removes client request from Que
		LinkedList<Message>  removeList = new LinkedList<Message>();
		for(Message queMsg : dataObject.getQueue()){ //removes corresponding request message from Que
			if(queMsg.getSourceId() == msg.getSourceId())
				removeList.add(queMsg);
		}		
		dataObject.getQueue().removeAll(removeList);

		//Unblock and grant next request.
		if(dataObject.lockedBy == msg.getSourceId()){ // grants next permission if necessary
			dataObject.locked = false;
			grantNextPermission(dataObject);
		}
		
	
	}

	/**
	 * Handles Withdraw message.
	 * Removes senders request from waitingQueue.
	 * sends  message to next eligible process
	 * @param msg
	 */
	private synchronized void handleWithdrawMsg(Message msg) {
		
		noOfWITHDRAWrecvd++;
		Data dataObject = DataObj[msg.getObjectNo()];
		
		//Send ACK to client
		Message newMsg = new Message(getNodeId(),msg.getSourceId(),MSGTYPE.ACK,vectorClock,msg.getData());
		sendMsg(newMsg,true);
		
		//Removes client request from Que
		LinkedList<Message>  removeList = new LinkedList<Message>();
		for(Message queMsg : dataObject.getQueue()){ //removes corresponding request message from Que
			if(queMsg.getSourceId() == msg.getSourceId())
				removeList.add(queMsg);
		}		
		dataObject.getQueue().removeAll(removeList);

		//Unblock and grant next request.
		if(dataObject.lockedBy == msg.getSourceId()){ // grants next permission if necessary
			dataObject.locked = false;
			grantNextPermission(dataObject);
		}
		
	}

	private void grantNextPermission(Data dataobj) {
		
		Data dataObject = dataobj;
		
		dataObject.locked = false;
		dataObject.lockedBy = -1;
		
		if(dataObject.getQueue().isEmpty())
			return;
		
		if(processStatus == STATUS.FREEZING) //Don't send new Grant msg while Freeze.
			return;
		
		Message msg = dataObject.getQueue().getFirst();
		dataObject.locked = true;
		dataObject.lockedBy = msg.getSourceId();
		Message newMsg = new Message(getNodeId(),msg.getSourceId(),MSGTYPE.GRANT,vectorClock,msg.getData());
		sendMsg(newMsg, true);
	}

	private void handleUpdateReplyMsg(Message msg) {
		
		StringTokenizer str = new StringTokenizer( msg.getData(), ",");
		
		int vNumber,data;
		
		for(int i = 0; i < DataObj.length; i++)
		{
			 vNumber =  Integer.parseInt(str.nextToken());
			 data =  Integer.parseInt(str.nextToken());
			 
			if( DataObj[i].getVersionNumber() < vNumber ){
					DataObj[i].setVersionNumber(vNumber);
					DataObj[i].setData(data);	
			}
			
			
		}
	}

	private void handleUpdateRequestMsg(Message msg) {
		
		String data = "";
		for(int i = 0; i < DataObj.length; i++)
			data += DataObj[i].getVersionNumber() + "," + DataObj[i].getData() + ",";
		
		for (int i = 0; i < lastReqRecvd.length-1; i++) 
			data += lastReqRecvd[i] + ",";
		
		data += lastReqRecvd[lastReqRecvd.length-1];
	
		sendMsg(new Message(getNodeId(),msg.getSourceId(),MSGTYPE.UPD_REPLY,vectorClock,data), true);
	}

	private void handleDeactivateMsg(Message msg) {
		processStatus = STATUS.DEACTIVE;
		for(Data dataObj :  DataObj)
			dataObj.getQueue().clear();	
	}

	private void handleReactivateMsg(Message msg) {
		processStatus = STATUS.RECOVERING;
		
		senMsgToActiveServers(MSGTYPE.FREEZ);

		
		Timer timer1 = new Timer(25*timeUnit,this);
		timer1.setActionCommand("UPD_REQ");
		timer1.setRepeats(false);
		timer1.start();
		
		
		Timer timer2 = new Timer(35*timeUnit,this);
		timer2.setActionCommand("UNFREEZ");
		timer2.setRepeats(false);
		timer2.start();
		
	}

	/**
	 * Time calls this function when specified time elapse.
	 * @param e : contains event info
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getActionCommand().equals("UPD_REQ")){
			senMsgToActiveServers(MSGTYPE.UPD_REQ);
		}
		
		if(e.getActionCommand().equals("UNFREEZ")){
			senMsgToActiveServers(MSGTYPE.UNFREEZ);
			processStatus = STATUS.ACTIVE;
		}
		
	}
	
	private void senMsgToActiveServers(MSGTYPE msgType) {
		for(int i = 0; i < noOfServers; i++ )
		{
			if(isFailedNode(i))
				continue;
			
			sendMsg(new Message(getNodeId(),i,msgType,vectorClock), true);
			
		}
	}
	
	private boolean isFailedNode(int i) {
		
		for(int failedNode : failingNodes)
			if(i == failedNode)
				return true;
		
		return false;
	}

	private boolean allRequstRecvd() {
		
		log.print("lastReqRecvd = ");
		for(int i=0; i < noOfClients; i++)
			log.print(lastReqRecvd[i]);
		log.println();
		log.flush();
		
		for(int i=0; i < noOfClients; i++)
			if(lastReqRecvd[i] != noOfRequest)
				return false;
			
		return true;
	}

	/**
	 * Prints all counters
	 * @param msg
	 */
	private synchronized void printSummary() {
		
		log.println("\n**************** NodeId : "+getNodeId() +" *****************");
		for(int i=0; i < DataObj.length; i++)
			log.println("dataObject"+i+" " +DataObj[i]);
	
		log.println("noOfREQUESTrecvd     : " + noOfREQUESTrecvd);
		log.println("noOfCOMMITrecvd      : " + noOfCOMMITrecvd);
		log.println("noOfWITHDRAWrecvd    : " + noOfWITHDRAWrecvd);
		log.println("noOfGRANTsent        : " + noOfGRANTsent);
		log.println("\n***************************************");

	}



    /**
     * Logger for current Node
     * @param logText : Log message
     */
    public synchronized void log(String logText)
    {
    	//logger.log(Level.INFO,getNodeId()+" : "+clock.Time+" : "+logText);
    	log.println(getNodeId()+" : "+vectorClock[getNodeId()]+" : "+logText);
    	log.flush();
    }
    

    /**
     * prints debug info
     * @param debugText
     */

    
    public synchronized void debug2(String debugText)
    {
    	log.print(getNodeId()+" : "+vectorClock[getNodeId()]+" : "+"_DEBUG_"+debugText+" " +Thread.currentThread().getStackTrace()[2]);
    	log.println();
    	log.flush();
    }

    /**
     * Sends message to destination node.
     * Update counters.
     */
	@Override
	public  void sendMsg(Message msg, boolean clockTick) {
		
    	if(msg.destId == getNodeId()){ // local msg
    		synchronized (msgQueue) {
    			log("Local  Sent :"+msg);
    			msgQueue.addFirst(msg);
	    		msgQueue.notify();
			}
    		return;
    	}
    	
 
    	if(msg.getMessageType() == MSGTYPE.GRANT)
    		noOfGRANTsent++;
    	if(msg.getMessageType() == MSGTYPE.ACK)
    	{
    		noOfACKsent++;
    		
    		if(msg.getReqId() == noOfRequest)
    			lastAck++;
    			
    		if( lastAck == noOfClients) 
    			printSummary(); 
    	}
    	
		super.sendMsg(msg, clockTick);

	}


	
	/**
	 * Performs Initialization 
	 */
    private synchronized void init() {
     	
    	processStatus = STATUS.ACTIVE;
    	
    	for(int  serverId=0; serverId < getNodeId(); serverId++ )
				createConnection(serverId );
    	
			try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace();}
			
			for(int i = 0; i < DataObj.length; i++){
				DataObj[i] = new Data(this);
	    		DataObj[i].setData(3-i);
	    		DataObj[i].setVersionNumber(1);
	    	}
			
			
	}

	
    
	@Override
	public void run() {
		 init();

		 while(true)
		 {
			 if(msgQueue.isEmpty())
				 synchronized (msgQueue){
					 try { 	msgQueue.wait(1000); } catch (InterruptedException e) { e.printStackTrace(); }
				 }
			 
				if(msgQueue.isEmpty())
					 continue;
				
				Message msg = msgQueue.remove();
				
				if(processStatus == STATUS.DEACTIVE && msg.getMessageType() != MSGTYPE.REACTIVATE)
					continue;
				
				if(msg.getSourceId() == getNodeId())
					log("Local  Recv :"+ msg);
				else
				{	
					int[] otherVectorClock = msg.getVectorClock();
					for(int i = 0; i < vectorClock.length; i++)
						if(otherVectorClock[i] > vectorClock[i])
							vectorClock[i] = otherVectorClock[i]; 
						
					vectorClock[getNodeId()]++;
				    log("Remote Recv :"+ msg);
				}	 
				
			    if(canDelivered(msg)){
			    	handleMessage(msg);
			    	deleverBufferedMsg();
			    }
			    else{
			    	bufferQue.add(msg);
			    	log("Buffered Message  :"+msg);
			    }
			    
		 }
	}

	private void deleverBufferedMsg() {
		
		boolean newMsgFound = false;
		Message msg = null;
		
		while(true)
		{
			 newMsgFound = false;
			 msg = null;
			 
			for(Message queEntry : bufferQue)
				if(canDelivered(queEntry)){
					newMsgFound = true;
					msg = queEntry;
					break;
				}
				
		    if(newMsgFound){
		    	log("Delivering Buffered Message  :"+msg);
		    	handleMessage(msg);
		    	bufferQue.remove(msg);
		    }
		    else
		    	break;
		}
	
	
		
	}

	private boolean canDelivered(Message msg) {
		
		int msgSrc = msg.getSourceId();
		int[] msgVectorClock = msg.getVectorClock();
		
		
		//if(vectorClock[src] != msgVectorClock[src]-1)
		//	return false;
		
		for(int i = noOfServers; i < noOfClients+noOfServers; i++){
			if(msgSrc != i && vectorClock[i] < msgVectorClock[i]  )
				return false;
		}
		
		return true;
	}		
		
	

}
