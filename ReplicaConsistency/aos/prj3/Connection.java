package aos.prj3;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Connection/Channel for Node to Node Communication
 * @author Rahul Gaikwad
 *
 */
public class Connection {
	
	final static int CLOSED = 0,ACTIVE = 1;
	int sendCounter,ackCounter,connectionId;
	volatile int status;
    
	Thread dataListenerThread;	
	Socket socket;
	ObjectInputStream In;
	ObjectOutputStream Out;
	Node node;
	
	/**
	 * @param socket : Connecting Socket
	 * @param in : Sockets DataInputStreams
	 * @param out : Sockets DataOutputStreams
	 * @param node : The Connection Node
	 * @param conId :  Connection Id
	 */
	public Connection(Socket socket, ObjectInputStream in, ObjectOutputStream out,Node node,int conId) {	
		this.socket = socket;
		this.In = in;
		this.Out = out;
		this.node = node;
		this.connectionId = conId;
		this.status = ACTIVE;
	}
	


	/**
	 * Close Connection and all it resources i.e Socket and its Input/Output Data Stream
	 */
	public void close() {
	
		try {
			dataListenerThread.suspend();
			In.close();
			Out.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		status = CLOSED;
		node.closeConnection(connectionId);
	}
	
	/**
	 *  Getters and Setters Section
	 */
	public synchronized Socket getSocket() {
		return socket;
	}
	public synchronized void setSocket(Socket socket) {
		this.socket = socket;
	}
	public synchronized ObjectInputStream getIn() {
		return In;
	}
	public synchronized void setIn(ObjectInputStream in) {
		In = in;
	}
	public synchronized ObjectOutputStream getOut() {
		return Out;
	}
	public synchronized void setOut(ObjectOutputStream out) {
		Out = out;
	}
	public synchronized boolean IsClosed() {
		return status==0;
	}

	public int getConId() {
		return connectionId;
	}

	public void setConId(int conId) {
		this.connectionId = conId;
	}
	
}
