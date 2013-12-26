package aos.prj1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectionAcceptor implements Runnable {

	private Node node;
	
	public ConnectionAcceptor(Node node) {
		this.node = node;
	}

	/**
	 * Listens For remote hosts connections, Accepts Remote Connection
	 * Create In/Out Stream, and register this connection as a Channel of Node.
	 * First Message must be CONNECTION REQUEST on this connection.
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
			 
				if(!msg.getMessageType().equalsIgnoreCase("CONNECT"))
					 node.log("Wrong Msg received Expected : CONNECT, received:"+msg.getMessageType());
				else
				{
					Connection con = new Connection(socket, in, out,node,remoteNode);
					con.receiverThread = new Thread(new ReceiverThread(con)); 
					node.channel[remoteNode] = con;
					node.channel[remoteNode].receiverThread.setPriority(Thread.MIN_PRIORITY);
					node.channel[remoteNode].receiverThread.start();
				}
						
			
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
	}

}
