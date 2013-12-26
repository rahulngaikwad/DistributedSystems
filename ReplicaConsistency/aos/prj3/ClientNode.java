package aos.prj3;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;

import javax.swing.Timer;

import aos.prj3.Message.MSGTYPE;

/**
 * ClientNode Class Simulate the Actual Physical client
 * which exchange messages to access data object. 
 * Maintains Own Connection pool.
 * @author Rahul Gaikwad
 */
public class ClientNode extends Node implements ActionListener {
	
	float HOLD_TIME = 1;
	boolean failureRecoveryMode = false;
	
    int noOfRequest;
	private volatile boolean timeOut = false;
	private boolean[] grantRecvd;
	
	
	int reqId = 0;
	long requestIssueTime;
	Message lastRequestMsg;
	
	int noOfREQUESTsent,noOfCOMMITsent,noOfWITHDRAWsent,noOfGRANTrecvd,noOfACKrecvd;
	private  int successfullAccess=0,unsuccessfullAccess=0;
	ArrayList<Integer> timeList = new ArrayList<Integer>();	
	
	int lastAck;
	
	Logger logger = Logger.getLogger(ClientNode.class.getName());
	
	
	/**
	 * Constructor which accepts following parameters
	 * @param NodeId : nodes id
	 * @param noOFServers : total no of Servers
	 * @param noOFClients : total no of Clients
	 * @param noOfRequest : total no of request
	 * @param timeUnit :  time unit
	 * @param config : configuration 
	 */
	public ClientNode(int nodeId, int noOFServers, int noOFClients,int noOfRequest,int timeUnit,int[] failingNodes, HashMap<Integer,String> config){
		super(nodeId,noOFServers,noOFClients,timeUnit,failingNodes,config);
		this.noOfRequest = noOfRequest;
		grantRecvd = new boolean[noOfServers];
	}

	/**
	 * Handles all received message
	 * Update clock time
	 */
	@Override
	public  synchronized void handleMessage(Message msg){

		if(msg.getMessageType() == MSGTYPE.GRANT)
			handleGrantMsg(msg);
		else if(msg.getMessageType() == MSGTYPE.ACK)
			handleAckMsg(msg);
		else if(msg.getMessageType() == MSGTYPE.START)
			handleStartMsg(msg);	
    	else
    		 log("Warnning !!!!!!!! Wrong Message Received "+msg); 

    }


	
	/**
	 * Sends REQUEST/COMMIT message to all Servers immediately
	 * @param delay : delay time
	 */
	private synchronized void  sendBroadcastMessage(MSGTYPE msgType) {
		
		vectorClock[getNodeId()]++;
		
		Message msg;
		
		if(msgType == MSGTYPE.REQUEST){
			
			reqId++;
			timeOut = false;
		    msg = new Message(getNodeId(),-1,MSGTYPE.REQUEST,vectorClock);
		    lastRequestMsg = msg;
			int dataObjectNo = new Random().nextInt(4);
			//int dataObjectNo = 1;
			msg.setDataObjectNo(dataObjectNo);
			msg.setDataObjectValue(2);
			msg.setReqId(reqId);
			clearGrantRecvd();
			
			requestIssueTime=System.currentTimeMillis();
			timer = new Timer(20*timeUnit,this);
			timer.setActionCommand("TIMEOUT");
			timer.setRepeats(false);
			timer.start();
		}
		else //COMMIT or WITHDRAW
			msg = new Message(getNodeId(),-1,msgType,vectorClock,lastRequestMsg.getData());
		
		
			for(int  serverId=0; serverId < noOfServers; serverId++ ){
				msg.setDestId(serverId);
				sendMsg(msg,false);
			}	
	}

	/**
	 * Clears grantRecvd array
	 */
	private void clearGrantRecvd() {
	
		for(int  serverId=0; serverId < noOfServers; serverId++ )
			grantRecvd[serverId]=false;
	}

	/**
	 * Handles Grant message received from server.
	 * If Request has been granted by the tree then sends COMMIT only after HOLD Time.
	 * @param msg
	 */
	private synchronized void handleGrantMsg(Message msg) {
		
		noOfGRANTrecvd++;
		
	  if(reqId==msg.getReqId()){// if grant received for currently issued request
		  
		   grantRecvd[msg.sourceId]	= true;
		   
		   if(!timeOut && requestGrantedByTree(0) ){
			   timer.stop(); // Stops Awaiting Grant timer
			   long currentTime = System.currentTimeMillis();
			   int timeTaken = (int) (currentTime - requestIssueTime);
			   timeList.add(timeTaken);
			   
			   clearGrantRecvd();
		   	   successfullAccess++;
		   	   
			    timer = new Timer( (int)HOLD_TIME*timeUnit,this); 
				timer.setActionCommand("COMMIT");
				timer.setRepeats(false);
				timer.start();
		   }
	  }
		
	}
	
	/**
	 * Checks whether request is Granted By The Tree
	 * @param r : root node
	 * @return : true if request is Granted By The Tree, otherwise false
	 */
	private boolean  requestGrantedByTree(int r) {
		boolean root = false, leftSubTree = false, rightSubTree = false;
		   		
			if(isleafNode(r))
				return(grantRecvd[r]);
			
				root = grantRecvd[r];
				
				leftSubTree = requestGrantedByTree(2*r+1);
			
				rightSubTree = requestGrantedByTree(2*r+2);
				
			if(root && (leftSubTree || rightSubTree) || leftSubTree && rightSubTree)
				return true;
			
	  return false;
	}
	
	private boolean isleafNode(int i) {
		if(2*i+1 >= noOfServers)
			return true;
		
		return false;
	}

	/**
	 * Handles  ACK message.
	 * Just ignores any Ack message received from client
	 * @param msg
	 */
	private synchronized void handleAckMsg(Message msg) {
		noOfACKrecvd++;	
		
		
			if(msg.getReqId() == noOfRequest)
				lastAck = lastAck+1;
			
			log.println("lastAck =" + lastAck);
			log.println("noOfServers " + noOfServers);
			
			if(lastAck > 0)
				printSummary();
			
			
	}

	/**
	 * Handles START message,
	 * which act as a triggering event for the client
	 * @param msg
	 */
	private synchronized void handleStartMsg(Message msg) {
		scheduleFutureRequest();
	}
	
	/**
	 * Prints all counters
	 * @param msg
	 */
	private synchronized void printSummary() {
		
		int MinTime = Integer.MAX_VALUE,MaxTime = Integer.MIN_VALUE;
		long sum=0;
		for(int time : timeList)
		{
			if(time < MinTime)
				MinTime = time;
			
			if(time > MaxTime)
				MaxTime = time;
			
			sum += time;
		}
		
		double AvgTime = sum/(double)successfullAccess;
		
		double sumOfSquare = 0;
		
		for(int time : timeList)
			sumOfSquare += (AvgTime-time)*(AvgTime-time);

		double standardDeviation = Math.sqrt(sumOfSquare)/successfullAccess;
		
		
		
		log.println("\n**************** NodeId : "+getNodeId() +" *****************");
		log.println("successfullAccess   : " + successfullAccess);
		log.println("unsuccessfullAccess : " + unsuccessfullAccess);
		log.println("noOfREQUESTMsgsent  : " + noOfREQUESTsent);
		log.println("noOfCOMMITMsgsent   : " + noOfCOMMITsent);
		log.println("noOfWITHDRAWMsgsent : " + noOfWITHDRAWsent);
		log.println("noOfGRANTMsgrecvd   : " + noOfGRANTrecvd);
		log.println("noOfACKMsgrecvd     : " + noOfACKrecvd);
		log.println("Total Msg Exchanged : " + (noOfREQUESTsent+noOfCOMMITsent+noOfWITHDRAWsent+noOfGRANTrecvd+noOfACKrecvd));
		log.println("Minimum Time		 : " + MinTime);
		log.println("Maximum Time		 : " + MaxTime);
		log.println("Average Time		 : " + AvgTime);
		log.println("Standard Deviation	 : " + standardDeviation);
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
	public synchronized void sendMsg(Message msg, boolean clockTick) {
		
    	if(msg.destId == getNodeId()){ // local msg
    		synchronized (msgQueue) {
    			log("Local  Sent :"+msg);
    			msgQueue.addFirst(msg);
	    		msgQueue.notify();
			}
    		return;
    	}
    	
 
    	if(msg.getMessageType() == MSGTYPE.REQUEST)
    		noOfREQUESTsent++;
    	if(msg.getMessageType() == MSGTYPE.COMMIT)
    		noOfCOMMITsent++;
    	if(msg.getMessageType() == MSGTYPE.WITHDRAW)
    		noOfWITHDRAWsent++;

		super.sendMsg(msg, clockTick);
	}

	/**
	 * schedule Request Message to sent after 5-10 unit time 
	 */
	private void scheduleFutureRequest() {
		
		if(reqId == noOfRequest)
			return;
		
		
		if(failureRecoveryMode && getNodeId() == noOfServers)//if Client 0
		{
			if(5*reqId == noOfRequest) 
				for(int failServer : failingNodes)
					sendMsg(new Message(getNodeId(),failServer,MSGTYPE.DEACTIVATE,vectorClock), true);
						
			if(5*reqId == 2*noOfRequest) 
				for(int failServer : failingNodes)
					sendMsg(new Message(getNodeId(),failServer,MSGTYPE.REACTIVATE,vectorClock), true);
		}
		
		
		int time = (new Random().nextInt(6)+5)*timeUnit;	
		timer = new Timer(time,this);
		timer.setActionCommand("REQUEST");
		timer.setRepeats(false);
		timer.start();
	}
	
	/**
	 * Time calls this function when specified time elapse.
	 * @param e : contains event info
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getActionCommand().equals("REQUEST")){
			sendBroadcastMessage(MSGTYPE.REQUEST);
		}
		else 
		if(e.getActionCommand().equals("TIMEOUT")){
			timeOut = true;
			clearGrantRecvd();
			sendBroadcastMessage(MSGTYPE.WITHDRAW);
			unsuccessfullAccess++;
			scheduleFutureRequest();
		}
		else
		if(e.getActionCommand().equals("COMMIT")){
		   	   sendBroadcastMessage(MSGTYPE.COMMIT);
		   	   scheduleFutureRequest();
		}
		
	}
	
	/**
	 * Performs Initialization 
	 * Create connection to all servers
	 */
    private synchronized void init() {
     	
    	for(int  serverId=0; serverId < noOfServers; serverId++ )
				createConnection(serverId );

			try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace();}
			
		if(getNodeId() == noOfServers+noOfClients-1){//last Client has additional responsibility to send trigger to all clients 
			for(int  clientId=noOfServers; clientId < noOfServers + noOfClients -1 ; clientId++ ){
				createConnection(clientId );
				sendMsg(new Message(getNodeId(),clientId,MSGTYPE.START,vectorClock), false);	
			}
			
			handleStartMsg(new Message(getNodeId(),getNodeId(),MSGTYPE.START,vectorClock));
		}
			
	}

	
    /**
     * run method for ClientNode Class
     * removes any received message from msgQueue and process it
     */
	@Override
	public void run() {
		 init();

		 while(true)
		 {
			 
			 if(msgQueue.isEmpty()){
				 synchronized (msgQueue){
					 try { 	msgQueue.wait(500); } catch (InterruptedException e) { e.printStackTrace(); }
				 }
			 }
				 
			 
			if(msgQueue.isEmpty())
				 continue;
			
			Message msg = msgQueue.remove();
			
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
			
			 handleMessage(msg);

		 }
	}

}
