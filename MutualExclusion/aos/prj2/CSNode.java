package aos.prj2;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import aos.prj2.Message.MSGTYPE;

/**
 * Class CSNode extends Node to Simulate the Actual Physical node for 
 * critical section  execution.
 * Maintains Own Connection pool.
 * @author Rahul Gaikwad
 */
public class CSNode extends Node{

	Logger logger = Logger.getLogger(CSNode.class.getName());
	
	CSNode(HashMap<Integer,String> config) {
		super(-1, config);
	}
	
	@Override		
    public synchronized void handleMessage(Message msg){
    	
    	log("Remote Recv :"+ msg);	
    	
    	if(msg.getMessageType() == MSGTYPE.CS_ENTER )
    		handleCsEnter(msg);
    	else
    		log("Wrong msg received :" + msg);
    	
		 synchronized (msgQueue){
			 msgQueue.notifyAll();
		 }
    }

	/**
	 * Simulate Critical section execution 
	 * Log <SenderId,RequestId,Current System Time
	 * sleep for sleepTime
	 * Log <SenderId,RequestId,Current System Time
	 * sends CS_EXIT message to sender
	 * @param msg
	 */
	private synchronized void handleCsEnter(Message msg) {
		int reqId = msg.getReqId();
		
		long sleepTime =  msg.getCsTime();
		
		long timeBeforeSleep = System.currentTimeMillis();

		log(msg.getSourceId()+","+reqId+","+timeBeforeSleep);
		
		long sleepDuration = sleepTime;
		
		do{
			try { Thread.sleep(sleepDuration);} 
			catch (InterruptedException e) { e.printStackTrace();}
			
			long timeAfterSleep = System.currentTimeMillis();
			
			sleepDuration = timeAfterSleep - timeBeforeSleep;
		}while(sleepDuration < sleepTime);
			
		
		log(msg.getSourceId()+","+reqId+","+System.currentTimeMillis());
		
		sendMsg(new Message(getNodeId(), msg.getSourceId(), 0, MSGTYPE.CS_EXIT),false);
	}
    

    /**
     * Logger for current Node
     * @param logText : Log message
     */
    public void log(String logText)
    {
    	//logger.log(Level.INFO,getNodeId()+" : "+clock.Time+" : "+logText);
    	
    	log.println("CSNode : "+logText);
    	log.flush();
    }

	@Override
	public void run() {
        
		 while(true)
		 {
			 if(msgQueue.isEmpty())
				 synchronized (msgQueue){
					 try { 	msgQueue.wait(); } catch (InterruptedException e) { e.printStackTrace(); }
				 }
			 if(msgQueue.isEmpty())
				 continue;
			 log.println("Current msgQueue");
			 Iterator<Message> iterator = msgQueue.iterator();
			 while(iterator.hasNext())
				 log.print(iterator.next() + ",");
			 log.println();
			 
			 Message msg = msgQueue.remove();
			 log.println("removed Message : "+msg );
			 handleMessage(msg);

		 }
		
	}
    

}
