package aos.prj2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import aos.prj2.Message.MSGTYPE;

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
				 BufferedReader  in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				 Message msg = new Message(in.readLine());
		
				 int remoteNode = msg.getSourceId();
				 node.log("New Connection Accepted From :"+remoteNode);
			 
				
				if( msg.getMessageType() != MSGTYPE.CONNECT)
					 node.log("Wrong Msg received Expected : CONNECT, received:"+msg);
				else
				{
					Connection con = new Connection(socket, in, out,node,remoteNode);
					con.dataListenerThread = new Thread(new DataListener(con)); 
					node.connPool.put(remoteNode,con);
					node.connPool.get(remoteNode).dataListenerThread.setPriority(Thread.MIN_PRIORITY);
					node.connPool.get(remoteNode).dataListenerThread.start();
				}
						
			
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
	}

}
