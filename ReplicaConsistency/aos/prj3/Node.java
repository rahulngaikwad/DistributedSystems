package aos.prj3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingDeque;

import javax.swing.Timer;

import aos.prj3.Message.MSGTYPE;

/**
 * Node abstract class implements part of actual physical node 
 * Class which extend this class must implement handleMessage and log methods
 * @author Rahul
 *
 */
public abstract class Node implements Runnable{
	
    private int  NodeId;
	protected int timeUnit;
    int[] vectorClock;
	ServerSocket serverSocket;
	Thread listnerThread;
	HashMap<Integer,String> configuration;
	HashMap<Integer,Connection> connPool;
	LinkedBlockingDeque<Message> msgQueue;
	int noOfServers,noOfClients;
	protected int[] failingNodes;
	Timer timer;
	PrintWriter log;
	//PrintStream log;
	
	
	 Node(int NodeId,int noOfServers,int noOfClients,int timeUnit,int[] failingNodes,HashMap<Integer,String> config)
	 {
		 this.NodeId = NodeId;
		 this.timeUnit = timeUnit;
		 this.configuration =config;
		 this.noOfServers = noOfServers;
		 this.noOfClients = noOfClients;
		 this.failingNodes = failingNodes;
		 this.vectorClock = new int[noOfServers+noOfClients];
		 this.connPool = new HashMap<Integer,Connection>();
		 this.msgQueue = new LinkedBlockingDeque<Message>();
		 
		 
	        try {
				 log = new PrintWriter(new FileWriter(new File("log"+getNodeId()+".txt"), false));
			} catch (IOException e1) {
				e1.printStackTrace();
			} 
	     
	      //  log = System.out;
	        
		 this.startListner();
	 }
	 

	public abstract void handleMessage(Message msg);
	 public abstract void log(String logText);
	 
		/**
	     * Create new connection if  first time sending message to remote node
	     * or old connection has been terminated
	     * @param msg : message to send
	     * @param clockTick : tick clock before sending message
	     */   
	   public  void sendMsg(Message msg, boolean clockTick) {
		   
	    	int destNode = msg.getDestId();

	    	if(clockTick)
	    		vectorClock[getNodeId()]++;
	    		
	    	msg.setVectorClock(vectorClock);
	    	
	    	if( !connPool.containsKey(destNode) )
	    		createConnection(destNode );
	    	
	    	log("Remote Sent :"+msg);
	    	
	    	try {
	    		connPool.get(destNode).Out.reset();
				connPool.get(destNode).Out.writeObject(msg);
			} catch (IOException e){
				e.printStackTrace();
			}
	    }
	    
	/**
	 * Finds Listening port for current node from configuration
	 * Starts new Thread to accept connections from remote node 
	 */
    public void startListner(){
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
     * Creates New Connection/Channel, by sending CONNECT message 
     * to remote node.
     * Initiate Input/Output Data Streams.
     * Add/Register this new  Connection to nodes channel pool.
     * @param destNode
     */
    protected void createConnection(int destNode) {
    	
    	String HostEntry = configuration.get(destNode);
    	
    	String hostName = HostEntry.substring(0, HostEntry.indexOf('@'));
    	int remotPort = Integer.parseInt(HostEntry.substring(HostEntry.indexOf('@')+1));
    	Socket socket = null;
    	
    	boolean connected = false;
    	
    	
    	for(int retryCount=0; retryCount < 10 && connected == false; retryCount++){
    	    try {
				socket = new Socket(hostName,remotPort);
				connected = true;
			}  catch (Exception e) {
				connected = false;
				try { Thread.sleep(500);} catch (InterruptedException e1) {e1.printStackTrace();}
				
				log("Error in connecting to "+HostEntry+ "!!!!!!  Number of Retries left are "+ (10 - retryCount) );	
			}
    	}
    	
    	if(!connected)
    	{
    		log("Error in connecting to "+HostEntry+ "!!!!!!  All Retries Over " );
    		return;
    	}
    	

			try {
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream  in = new ObjectInputStream(socket.getInputStream());
				
				Message msg = new Message(getNodeId(),destNode,MSGTYPE.CONNECT,vectorClock);
				out.writeObject(msg); //sending CONNECT message to remote host
				
				Connection con = new Connection(socket, in, out,this,destNode);
				con.dataListenerThread = new Thread(new DataListener(con)); 
				con.dataListenerThread.setPriority(Thread.MIN_PRIORITY);
				con.dataListenerThread.start();
				connPool.put(destNode,con);
				
			log("Connection created with Node "+ destNode+":"+HostEntry);
			
			} catch (IOException e) {
				e.printStackTrace();
			}

		
	}

     
	/**
     * Close Channel indicated by ChannelId
     * @param coonId
     */
	public synchronized void closeConnection(int coonId){
		connPool.get(coonId).close();
    	connPool.remove(coonId);
    	log("Connection closed with Node "+coonId);	
    }
	
    /**
     * Close All Channel from nodes channel poo
     */
	public synchronized void closeAllConnection(){

		for (int connId :connPool.keySet())
			connPool.get(connId).close();
    }
	

	public int getNodeId() {	
		return NodeId;
	}
	 
	 public int[] getFailingNodes() {
		return failingNodes;
	}


	public void setFailingNodes(int[] failingNodes) {
		this.failingNodes = failingNodes;
	}
	
	/**
	 * finalize method
	 */
	public void finalize() throws Throwable {
			closeAllConnection();
	    	listnerThread.suspend();
	    	serverSocket.close();
	    	log.close();
	    	super.finalize();
	}
	   
}
