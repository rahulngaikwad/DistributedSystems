package aos.prj3;

import java.io.IOException;

/**
 * ReceiverThread Listens InputDataSteam for any incoming message
 * once receives message,it put into nodes message handling Queue
 * and notify node
 * @author Rahul Gaikwad
 */
public class DataListener extends Thread {
	Connection channel;
	
	/**
	 * @param connection : Connection/Channel to Listen
	 */
	public DataListener(Connection connection) {
		this.channel = connection;
	}

	@Override
	public void run() {
		 Node node = channel.node; 
		 node.log("Started Listner for Channel "+channel.connectionId);
		  
			while(!channel.IsClosed())	//Listens till Channel is not closed
			{
				Message msg = null;
				 try {
					 	msg =  (Message) channel.In.readObject(); //Blocking Call 
						
						if(msg == null)// Checks If Connection terminated by remote node
						{   node.log("Channel Has been Closed By Remote Node "+channel.connectionId);
							node.closeConnection(channel.connectionId);
							break;
						}
						
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}	
		

					synchronized (node.msgQueue) {
						 try {
							node.msgQueue.put(msg);
							node.msgQueue.notifyAll();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				
			}
	}
}
