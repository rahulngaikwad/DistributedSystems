package aos.prj3;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import aos.prj3.Message.MSGTYPE;

public class ConnectionAcceptor implements Runnable {

	private Node node;
	
	public ConnectionAcceptor(Node node) {
		this.node = node;
	}

	/**
	 * Listens For remote hosts connections, Accepts Remote Connection
	 * Create In and Out Stream, and register this connection as a Channel of Node.
	 * First Message received must be CONNECT on this connection.
	 */
	@Override
	public void run() {
		
		while (true) 
		{	
			Socket socket = null;
			
			try {
				 socket = node.serverSocket.accept();
				 
				 ObjectInputStream  in = new ObjectInputStream(socket.getInputStream());
				 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				 
				 Message msg = null;
				 try { msg = (Message)in.readObject();
				 } catch (ClassNotFoundException e) 
				 {e.printStackTrace();}
		
				 int remoteNode = msg.getSourceId();
				 node.log("New Connection Accepted From :"+remoteNode);
			 
				
				if( msg.getMessageType() != MSGTYPE.CONNECT)
					 node.log("Wrong Msg received Expected : CONNECT, received:"+msg);
				else
				{
					Connection con = new Connection(socket,in,out,node,remoteNode);
					con.dataListenerThread = new Thread(new DataListener(con));
					con.dataListenerThread.setPriority(Thread.MIN_PRIORITY);
					con.dataListenerThread.start();
					node.connPool.put(remoteNode,con);
				}
						
			
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
	}

}
