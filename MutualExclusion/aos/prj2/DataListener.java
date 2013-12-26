package aos.prj2;

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
						String msgString =  channel.In.readLine(); //Blocking Call 
						
						if(msgString == null)// Checks If Connection terminated by remote node
						{   node.log("Channel Has been Closed By Remote Node "+channel.connectionId);
							node.closeConnection(channel.connectionId);
							break;
						}
						msg = new Message(msgString);
						
					} catch (IOException e) {
						e.printStackTrace();
					}	
		

					synchronized (node.msgQueue) {
						 try {
							node.msgQueue.put(msg);
							node.msgQueue.notify();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				
			}
	}
}
