package aos.prj2;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Logger;

import aos.prj2.Message.MSGTYPE;

/**
 * Node Class Simulate the Actual Physical node
 * which exchange messages to enter in critical section. 
 * Maintains Own Connection pool.
 * @author Rahul Gaikwad
 */
public class ProcessNode extends Node {

	private int noOfProcess,requestToSent,IRTime,CsTime,CsNodeId;
	HashSet<Integer> quorumSet;
	PriorityBlockingQueue<QueEntry>  waitingQueue;
	HashSet<Integer> inquiredQueue;
	
	QueEntry lockedBy;
	boolean  locked = false,inquireSent=false,failReceived = false, yeildSent = false, inCS = false;
	private volatile int reqId=1,replyReceived = 0,lastReleaseSentTS=0;
	private volatile int noOfREQUESTsent,noOfREPLYrecv,noOfRELEASEsent,noOfFAILrecv,noOfENQUIRErecv,noOfYEILDsent; 

	//Debug purpose only
	boolean  old_locked = false,old_inquireSent=false,old_failReceived = false, old_inCS = false;
	private volatile int old_reqId=1,old_replyReceived = 0,old_lastReleaseSentTS=0;

	Logger logger = Logger.getLogger(ProcessNode.class.getName());
	
	
	/**
	 * Constructor which accepts following parameters
	 * @param NodeId : nodes id
	 * @param noOfProcess : total no of processes
	 * @param requestToSent : total no of requests to sent
	 * @param IRTime : Request time after exiting from critical section and before sending new request
	 * @param CSTime : Critical Section time
	 * @param quorumSet : nodes quorum
	 * @param config : configuration 
	 */
	public ProcessNode(int NodeId, int noOfProcess, int requestToSent,int IRTime, int CSTime,HashSet<Integer> quorumSet,HashMap<Integer,String> config) {
		super(NodeId,config);
		this.noOfProcess = noOfProcess;
		this.requestToSent = requestToSent;
		this.IRTime = IRTime;
		this.CsTime = CSTime;
		this.quorumSet = quorumSet;
		this.waitingQueue  = new PriorityBlockingQueue<QueEntry>();
		this.inquiredQueue = new HashSet<Integer>();
		this.CsNodeId = -1;
	   
	}

	/**
	 * Handles all received message
	 * Update counter
	 * Update clock time
	 * 
	 */
	@Override
	public  synchronized void handleMessage(Message msg){
		
		debug("enter");
		
    	if(msg.getMessageType() == MSGTYPE.START ){
    		log("Remote Recv :"+ msg);
    		sendBroadcastMessage(MSGTYPE.REQUEST);
    		return;
    	}
    	
		if(msg.getSourceId() == getNodeId())
			log("Local  Recv :"+ msg);
		else{	
	    	clock.compareAndSetTime(msg.getclockTime()); 	
	    	log("Remote Recv :"+ msg);
	    	
	    	if(msg.getMessageType() == MSGTYPE.REPLY)
	    		noOfREPLYrecv++;
	    	if(msg.getMessageType() == MSGTYPE.FAILED)
	    		noOfFAILrecv++;
	    	if(msg.getMessageType() == MSGTYPE.INQUIRE)
	    		noOfENQUIRErecv++;
		}


		if(msg.getMessageType() == MSGTYPE.REQUEST)
    			handleRequestMsg(msg);
    	else if(msg.getMessageType() == MSGTYPE.REPLY)
				handleReplyMsg(msg);
		else if(msg.getMessageType() == MSGTYPE.RELEASE)
       			handleReleaseMsg(msg);
		else if(msg.getMessageType() == MSGTYPE.FAILED)
   				handleFailMsg(msg);
    	else if(msg.getMessageType() == MSGTYPE.INQUIRE)
    			handleInquireMsg(msg);
    	else if(msg.getMessageType() == MSGTYPE.YIELD)
    			handleYieldMsg(msg);
    	else if(msg.getMessageType() == MSGTYPE.CS_EXIT)
    		  	handleCsExitMsg(msg);   	
    	else if(msg.getMessageType() == MSGTYPE.INFO)
    			handleInfoMsg(msg);
    	else if(msg.getMessageType() == MSGTYPE.BROADCAST)
    			handleBroadcastMessage(0);
    	else
    		 log("Warnning !!!!!!!! Wrong Message Received "+msg); 
		
		debug("exit");
		
		 synchronized (msgQueue){
			 msgQueue.notifyAll();
		 }
    }

	/**
	 * Sends REQUEST message to process's entire quorum after delay time
	 * @param delay : delay time
	 */
	private synchronized void handleBroadcastMessage(int delay) {
		debug("enter");

		if(delay > 0){ //  scheduled for future i.e after delay time
			Thread broadcast = new Thread(new BroadcastSignal(this,delay));
			broadcast.start();
		}
		else
			sendBroadcastMessage(MSGTYPE.REQUEST);
		
		debug("exit");

	}
	
	/**
	 * Sends REQUEST message to process's entire quorum immediately
	 * @param delay : delay time
	 */
	private synchronized void  sendBroadcastMessage(MSGTYPE msgType) {
		debug("enter");
		
		int sentTime = clock.Tick();

		sendMsg(new Message(getNodeId(),getNodeId(),sentTime,msgType),false);
		
			for(int  member : quorumSet ){
				if(member!=getNodeId())
					sendMsg(new Message(getNodeId(),member,sentTime,msgType),false);
			}

		debug("exit");
	}

	/**
	 * Handles Fail message.
	 * sends Yield to all pending inquire requests
	 * @param msg
	 */
	private synchronized void handleFailMsg(Message msg) {

		failReceived = true;
		debug("enter");
		for(int nodeid : inquiredQueue){
				replyReceived--;
				yeildSent = true;
				sendMsg(new Message(getNodeId(),nodeid,clock.Time,MSGTYPE.YIELD),true);		
		}
		inquiredQueue.clear();
		debug("exit : cleared inquiredQueue");
	}
	
	/**
	 * Handles Reply message.
	 * If all reply received then enter in Critical Section
	 * @param msg
	 */
	private synchronized void handleReplyMsg(Message msg) {
		replyReceived++;
		debug("enterX");
		if(replyReceived == quorumSet.size() ){//&& lockedBy.getNodeId() == getNodeId()){//all reply received and itself is first in Q
			debug("Entering in CS");
			enterInToCS();
		}
			
		debug("exit");
	}

	/**
	 * Handles Info message.
	 * Print all counters recived in message
	 * Info meesages are sent to node 0 by all remaining node.
	 * @param msg
	 */
	private synchronized void handleInfoMsg(Message msg) {
		printSummary(msg);
	}

	/**
	 * Handles Critical Section Exit message.
	 * Broadcast RELEASE message to entire quorum   
	 * @param msg
	 */
	private synchronized void handleCsExitMsg(Message msg) {
		
		debug("enter");
		inCS = false;
		sendBroadcastMessage(MSGTYPE.RELEASE);
		
		reqId++;
		debug("exit");
		if(reqId <= requestToSent)
			 handleBroadcastMessage(IRTime);
		else
			 createSummary();
		
	}
	
	/**
	 * Handles Yield message.
	 * After receiving yield, send Reply message to next eligible process
	 * @param msg
	 */
	private synchronized void handleYieldMsg(Message msg) {
		
		debug("enter");
		if(locked && inquireSent && lockedBy.getNodeId() == msg.getSourceId() )
		{	
			//waitingQueue.remove(lockedBy);
			//QueEntry currLocker = lockedBy;
			//currLocker.setTime(clock.Time);
			//waitingQueue.put(currLocker);	
			debug2("calling grantNextPermission");
			grantNextPermission();
		}
		debug2("Exit");
	}

	/**
	 * Handles Inquire message.
	 * if process has received Fail or already sent Yield to other then
	 * send yield message to sender,after receiving Inquire message.
	 * else add this sender to inquiredQueue.
	 * @param msg
	 */
	private synchronized void handleInquireMsg(Message msg) {

		debug("enter");
		
		//  last release TS > msg.TS then ignore this inquire message
		if(lastReleaseSentTS > 0 && lastReleaseSentTS > msg.getclockTime())
			return;
		
		debug2("No late Inquire");
		
		if(failReceived || yeildSent){
			debug2("Sending Yeild/returning permission to " + msg.getSourceId());
			replyReceived--;
			sendMsg(new Message(getNodeId(),msg.getSourceId(),clock.Time, MSGTYPE.YIELD),true);
			debug("sent Yeild to "+msg.getSourceId());
			return;
		}
		debug2("aading to inquiredQueue "+msg.getSourceId());
		inquiredQueue.add(msg.getSourceId());
		debug2("Exit");
	}

	/**
	 * Handles Release message.
	 * Removes senders request from waitingQueue.
	 * sends Reply message to next eligible process
	 * @param msg
	 */
	private synchronized void handleReleaseMsg(Message msg) {
		
		debug("enter");
		
		if(getNodeId() == msg.getSourceId()){
			lastReleaseSentTS = msg.getclockTime();
			debug2("updated  to lastReleaseSentTS "+ lastReleaseSentTS);
		}
		//if INQUIRE was sent and not receive yield then  
		//else if(inquireSent && lockedBy.getNodeId() == msg.getSourceId())
			//replyReceived++;
	
		debug2("lockedBy = "+lockedBy+" and Release Received from "+ msg.getSourceId());
		
		if(lockedBy.getNodeId() != msg.getSourceId())
			debug2("Error!!!!!!!!!!!!!!!!!   lockedBy = "+lockedBy+" and Release Received from "+ msg.getSourceId());
		
		if(locked && lockedBy.getNodeId() == msg.getSourceId() ){
			if(waitingQueue.remove(lockedBy))
				debug2("removed as Release received"); 
			else
				debug2("Error!!!!!!!!!!!!!!!!!  Release received but Request not deleted "); 
			
			grantNextPermission();
		}
		debug2("exit");
	}

	/**
	 * sends Reply message to next eligible process
	 * and do necessary bookkeeping
	 */
	private synchronized void grantNextPermission() {
		debug("enter");
		
		locked = false;
		inquireSent = false;
		//failReceived = false;
		lockedBy = null;
		inquiredQueue.clear();
		
		if(!waitingQueue.isEmpty()){
			locked =true;
			lockedBy = waitingQueue.element();
			debug2(" sending Reply/Granting perm to "+lockedBy.getNodeId());
			sendMsg(new Message(getNodeId(),lockedBy.getNodeId(),clock.Time,MSGTYPE.REPLY),true);
		}
		debug2("exit");
	}

	/**
	 * Handles Request message.
	 * Add received Request in waitingQueue.
	 * If waitingQueue already contains high priority request
	 * sends Fail massage.
	 * If received Request has higher priority and reply was 
	 * already sent other node, then send inquire to that node
	 * only if already inquire has no send.
	 * @param msg
	 */
	private synchronized void handleRequestMsg(Message msg) {
		
		debug("enter");
		
		QueEntry entry = new QueEntry(msg.clockTime, msg.getSourceId());
		waitingQueue.put(entry);
		
		if(!locked){
			grantNextPermission();
			return;
		}
		
		Iterator<QueEntry> iterator = waitingQueue.iterator();
		
		while(iterator.hasNext())
		{
			if( iterator.next().compareTo(entry) < 0){
				if(msg.getSourceId() != getNodeId()) // dont send fail to self
					sendMsg(new Message(getNodeId(),msg.getSourceId(),clock.Time,MSGTYPE.FAILED),true);
				 return;
			}
		}
			
		if(!inquireSent)
		{
			inquireSent = true;
			debug2(" INQUIRE send to " + lockedBy.getNodeId() +","+ waitingQueue.element().getNodeId() );
			sendMsg(new Message(getNodeId(),lockedBy.getNodeId(),clock.Time,MSGTYPE.INQUIRE),true);
		}
		
		debug2("exit");
	}

	/**
	 * Simulate Critical section execution by 
	 * sending CS_ENTER message to CSNode
	 * Perform book keeping
	 */
	private synchronized void enterInToCS() {
		debug("Entering into CS");
		inCS = true;
		failReceived = false; // since all reply has been received
		yeildSent = false;
		Message newMsg = new Message(getNodeId(),CsNodeId,clock.Time,MSGTYPE.CS_ENTER);
		newMsg.setReqId(reqId);
		newMsg.setCsTime(CsTime);
		replyReceived = 0;
		sendMsg(newMsg,false); //msg to CSNode	
		debug2("exiting");
	}
	

	/**
	 * Creates Message and add all set all
	 * send message to node 0
	 */
	private synchronized void createSummary() {
		
		Message newMsg = new Message(getNodeId(),0,clock.Time,MSGTYPE.INFO);
		newMsg.setNoOfREQUESTs(noOfREQUESTsent);
		newMsg.setNoOfREPLYs(noOfREPLYrecv);
		newMsg.setNoOfRELEASEs(noOfRELEASEsent);
		newMsg.setNoOfFAILs(noOfFAILrecv);
		newMsg.setNoOfENQUIREs(noOfENQUIRErecv);
		newMsg.setNoOfYEILDs(noOfYEILDsent);
		printSummary(newMsg);
		
		if(getNodeId() != 0)
			sendMsg(newMsg,true);
	}
	
	/**
	 * Prints all counters
	 * @param msg
	 */
	private synchronized void printSummary(Message msg) {
		
		log.println("\n**************** NodeId : "+ msg.sourceId +" *****************");
		log.println("noOfREQUESTsent : " + msg.getNoOfREQUESTs());
		log.println("noOfREPLYrecv   : " + msg.getNoOfREPLYs());
		log.println("noOfRELEASEsent : " + msg.getNoOfRELEASEs());
		log.println("noOfFAILrecv    : " + msg.getNoOfFAILs());
		log.println("noOfENQUIRErecv : " + msg.getNoOfENQUIREs());
		log.println("noOfYEILDsent   : " + msg.getNoOfYEILDs());
		log.println("\n***************************************");
	}


    /**
     * Logger for current Node
     * @param logText : Log message
     */
    public synchronized void log(String logText)
    {
    	//logger.log(Level.INFO,getNodeId()+" : "+clock.Time+" : "+logText);
    	log.println(getNodeId()+" : "+clock.Time+" : "+logText);
    	log.flush();
    }
    

    /**
     * prints debug info
     * @param debugText
     */
    public synchronized void debug(String debugText)
    {
    	log.print(getNodeId()+" : "+clock.Time+" : "+"_DEBUG_"+debugText+" " +Thread.currentThread().getStackTrace()[2]);
    	log.print("|waitingQueue: ");
    	
    	Iterator<QueEntry>iterator = waitingQueue.iterator();
    	while(iterator.hasNext()){
    		QueEntry e =iterator.next();
    		log.print(e.getTime()+"/"+e.getNodeId()+", ");
    	}
    	log.print("|");
    	
    	if(replyReceived!=old_replyReceived || locked!=old_locked || inquireSent!=old_inquireSent || failReceived!=old_failReceived || inCS!=old_inCS || reqId!=old_reqId || lastReleaseSentTS!=old_lastReleaseSentTS){
    		//log.println("New Values :"+" replyReceived="+replyReceived+" locked="+locked+" inquireSent="+inquireSent+" failReceived="+failReceived+" inCS="+inCS+" reqId="+reqId+" lastReleaseSentTS="+lastReleaseSentTS);
    		//log.println("OLD Values :"+" replyReceived="+old_replyReceived+" locked="+old_locked+" inquireSent="+old_inquireSent+" failReceived="+old_failReceived+" inCS="+old_inCS+" reqId="+old_reqId+" lastReleaseSentTS="+old_lastReleaseSentTS);
    		if(replyReceived!=old_replyReceived){
    			log.print("Changed replyReceived :"+ replyReceived+"/"+old_replyReceived );
    			old_replyReceived = replyReceived;
    		}
    		if(locked!=old_locked){
    			log.print("|Changed locked :"+locked+"/"+old_locked);
    			old_locked = locked;
    		}
    		if(inquireSent!=old_inquireSent){
    			log.print("|Changed inquireSent :"+inquireSent+"/"+old_inquireSent);
    			old_inquireSent=inquireSent;
    		}
    		if(failReceived!=old_failReceived){
    			log.print("|Changed failReceived :"+failReceived+"/"+old_failReceived);
    			old_failReceived=failReceived;
    		}
    		if(inCS!=old_inCS){
    			log.print("|Changed inCS :"+inCS+"/"+old_inCS);
    			old_inCS=inCS;
    		}
    		if(reqId!=old_reqId){
    			log.print("|Changed reqId :"+reqId+"/"+old_reqId);
    			old_reqId=reqId;
    		}
    			
    		if(lastReleaseSentTS!=old_lastReleaseSentTS){
    			log.print("|Changed lastReleaseSentTS :"+lastReleaseSentTS+"/"+old_lastReleaseSentTS);
    			old_lastReleaseSentTS=lastReleaseSentTS;
    		}
    			
    	}
    	//else
    	//	log.println("Values :"+" replyReceived="+replyReceived+" locked="+locked+" inquireSent="+inquireSent+" failReceived="+failReceived+" inCS="+inCS+" reqId="+reqId+" lastReleaseSentTS="+lastReleaseSentTS);
    	log.println();
    	log.flush();
    }
    
    public synchronized void debug2(String debugText)
    {
    	log.print(getNodeId()+" : "+clock.Time+" : "+"_DEBUG_"+debugText+" " +Thread.currentThread().getStackTrace()[2]);
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
    	if(msg.getMessageType() == MSGTYPE.RELEASE)
    		noOfRELEASEsent++;
    	if(msg.getMessageType() == MSGTYPE.YIELD)
    		noOfYEILDsent++;

		super.sendMsg(msg, clockTick);
	}

	/**
	 * Performs Initialization 
	 */
    private synchronized void init() {
     	
    	createConnection(CsNodeId );


		for(Integer memberNodeId : quorumSet )
			if( memberNodeId > getNodeId()  )
				createConnection(memberNodeId );
		
		if (getNodeId() == 0) {
			for (int i = 1; i < noOfProcess; i++)
				if (!connPool.containsKey(i))
					createConnection(i);

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		
			for (int i = 0; i < noOfProcess; i++){
				sendMsg(new Message(getNodeId(), i, clock.Time, MSGTYPE.START), false);
		
			}
		}

	}
    
	@Override
	public void run() {
		 init();

		 while(true)
		 {
			 if(msgQueue.isEmpty())
				 synchronized (msgQueue){
					 try { 	msgQueue.wait(); } catch (InterruptedException e) { e.printStackTrace(); }
				 }
			 if(msgQueue.isEmpty())
				 continue;
			 log.print(getNodeId()+" : "+clock.Time+" : " +"Current msgQueue :");
			 Iterator<Message> iterator = msgQueue.iterator();
			 while(iterator.hasNext())
				 log.print(iterator.next() + ",");
			 log.print("| ");
			 
			 Message msg = msgQueue.remove();
			 log.print("removed Message : "+msg );
			 log.println();
			 handleMessage(msg);

		 }
	}
}
