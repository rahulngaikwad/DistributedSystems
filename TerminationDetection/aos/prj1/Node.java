package aos.prj1;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Node Class Simulate the Actual Physical node for 
 * termination detection.
 * Maintains Own Connection pool.
 * Join And Detach from Tree and Declare Termination
 * @author Rahul Gaikwad
 *
 */
public class Node extends NodeInfo{

	LamportClock clock;
	ServerSocket serverSocket;
	Thread listnerThread;
	Connection[] channel;
	HashMap<Integer,String> configuration;
	Logger logger = Logger.getLogger(Node.class.getName());
	
	Node(int NodeId, int totalProcess,HashMap<Integer,String> config) {
		super(NodeId);
		ParentId = NodeId;
		clock = new LamportClock();
		channel = new Connection[totalProcess];
		
	    configuration =config;
		this.stratListner();
	}
	
	/**
	 * Termination Detection Initiator must call this method
	 * to set Indicator Flag initFlag
	 */
    public void initAction(){
    	setInitFlag(true); 
    	clock.Tick();
    	log("INIT Happened ");
    }
    
    /**
     * Set Node status to Idle, if all Pending ACK are received then
     * Sends any Pending ACK to parent else 
     */
    public synchronized void idleAction(){
    	setStatusToIdle();	
    	clock.Tick();
    	log("Status Changed To Idle ");
    	log("C = "+getC()+" : D = "+getD());
    	
    	if( getD() == 0) //all pending ACK are received by this node
    	{	
    		// Not Initiator node then send last ACK to parent
    		if(!isInitNode() && getC() == 1 && getD() == 0  ){   
    			Message msg =  new Message(getNodeId(),getParentId(),clock.Time,"ACK");
    			log("Process Becames Idle And No Pending ACK For This, Hence sending Final ACK Message To Parent :" + msg);
    			sendMsg(msg);			
    			setParentId(getNodeId());
    			
    		}
    		else // Initiator node then Declare Termination
    			if(isInitNode() && getC() == 0 && getD() == 0) 
    			terminate();
    	}
    }
    
    /**
     * Initiator node then Declare Termination
     * Others Get Detaches from Tree
     */
    public void terminate(){
   
    	setParentId(getNodeId());
    	if(isInitNode())
    		log("Termination Detected !!");
    	else
    		log("Detached from tree !!");
    	
    	//closeAllChannel();
    }
    
    /**
     * Increment clock Time, Update Counters
     * Create new connection if  first time sending message to remote node
     * or old connection terminated
     * If this is last ACK message to parent node and status is idle then call local termination.
     * If all send message get ACK on current channel then close current channel 
     * @param msg : message to send
     */
    public synchronized void sendMsg(Message msg) {
    	
    	int destNode = msg.getDestId();
    	//Create new connection if sending first time or old connection terminated
    	if( channel[destNode] == null )
    		createConnection(destNode );
    	if(msg.msgType.equalsIgnoreCase("SEND")){
    		setD(getD() + 1);
    		channel[destNode].sendCounter++;
    	}
    	else if(msg.msgType.equalsIgnoreCase("ACK"))
    	{
    		setC(getC() - 1);
    		channel[destNode].ackCounter++;
    	}
    	
    	clock.Tick();
    	msg.setclockTime(clock.Time);
    	channel[destNode].Out.println(msg);
    	
    	log("C = "+getC()+" : D ="+getD());
    	log("Message Sent "+msg);
    	
    	if(getC() == 0 && getD() == 0 && isIdle())  //Checks if last ACK message sent to parent node then terminate
    		terminate(); 
	
    }
     
    /**
     * Ticks Clock after TickDelay time.
     * @param TickDelay
     */
    public synchronized void TickAction(int TickDelay) {
    	if(TickDelay == 0)
    		clock.Tick();
    	else
    		clock.Tick(TickDelay);
    	log("Clock Ticked : Delay Time :"+TickDelay);
    }
    
    /**
     * Creates New Connection/Channel, by sending CONNECTION REQUEST 
     * message to remote node.
     * Initiate Input/Output Data Streams.
     * Add this new  Connection to nodes channel pool.
     * @param destNode
     */
    private void createConnection(int destNode) {
    	
    	String HostEntry = configuration.get(destNode);
    	
    	String hostName = HostEntry.substring(0, HostEntry.indexOf('@'));
    	int remotPort = Integer.parseInt(HostEntry.substring(HostEntry.indexOf('@')+1));
    	
    	try {
			Socket socket = new Socket(hostName,remotPort);
 
			BufferedReader  in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			Message msg = new Message(getNodeId(),destNode,clock.Time,"CONNECT");
			out.println(msg); 
			
			Connection con = new Connection(socket, in, out,this,destNode);
			con.receiverThread = new Thread(new ReceiverThread(con)); 
			con.receiverThread.setPriority(Thread.MIN_PRIORITY);
			con.receiverThread.start();
			channel[destNode]= con;
			
			log("Connection created with Node "+ destNode+":"+HostEntry);	
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

    /**
     * Close Channel indicated by ChannelId
     * @param ChannelId
     */
	public synchronized void closeChannel(int ChannelId){
		//Wait so that receiver read sent  messages
		try {Thread.sleep(500);} catch (InterruptedException e) {	e.printStackTrace();}
		channel[ChannelId].close();
    	channel[ChannelId] = null;
    	log("Connection closed with Node "+ChannelId);	
    }
	
    /**
     * Close All Channel from nodes channel poo
     */
	public synchronized void closeAllChannel(){
		
		//Wait so that receivers read sent messages
		try {Thread.sleep(500);} catch (InterruptedException e) {	e.printStackTrace();}
		
    	for(int i = 0; i <totalNoOfProcess;i++)
    		if(channel[i] != null){
    			channel[i].close();
    			closeChannel(i);
    		}
    }
    
	/**
	 * Finds Listening port for current node from configuration
	 * Starts new Thread to accept connections from remote node 
	 */
    public void stratListner(){
    	try {
    		String HostEntry = configuration.get(getNodeId());
    		int portNo = Integer.parseInt( HostEntry.substring(HostEntry.indexOf('@')+1) );
			serverSocket = new ServerSocket(portNo);
			log("Listner Started at port No:"+portNo);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
        listnerThread = new Thread(new ConnectionAcceptor(this));
    	listnerThread.start();
    }
	
    /**
     * Logger for current Node
     * @param logText : Log message
     */
    public void log(String logText)
    {
    	//logger.log(Level.INFO,getNodeId()+" : "+clock.Time+" : "+logText);
    	System.out.println(getNodeId()+" : "+clock.Time+" : "+logText);
    }
	
    @Override
    protected void finalize() throws Throwable {
    	listnerThread.suspend();
    	serverSocket.close();
    	super.finalize();
    }
}
