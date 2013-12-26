package aos.prj1;

import java.io.IOException;

/**
 * ReceiverThread Listens InputDataSteam for incoming message
 * @author Rahul Gaikwad
 */
public class ReceiverThread extends Thread {
	Connection channel;
	
	/**
	 * @param connection : Connection/Channel to Listen
	 */
	public ReceiverThread(Connection connection) {
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
							node.closeChannel(channel.connectionId);
							break;
						}
						msg = new Message(msgString);
						
					} catch (IOException e) {
						e.printStackTrace();
					}	
				 
				//first process any message from node starter and then process received message.  
				node.clock.myNotify();
				//try { Thread.sleep(50); } catch (InterruptedException e) { e.printStackTrace();}
				
				synchronized (node) 
					{ 
					//set the correct time but do not notify to other waiting thread till you process this message 	 	
					node.clock.compareAndSetTime(msg.getclockTime()); 
					
					node.log("Message Received :"+msg);	
					
					if(msg.getMessageType().equalsIgnoreCase("SEND")  ) // if first incomming connection
					{
						node.setC(node.getC()+1);
						node.channel[channel.getConId()].sendCounter++;
						node.setStatusToActive();
						node.log("C = "+node.getC()+" : D ="+node.getD());
						
						if(node.getC() == 1 && node.getD() == 0){ // if first incomming connection
							 node.setParentId(msg.getSourceId());
							 node.log("Joined Tree With Parent"+msg.getSourceId());	
						}
						else { // send ACK to sender
							Message newMsg = new Message(node.getNodeId(),msg.getSourceId(),node.clock.Time,"ACK");
							node.log("Sending Instant Reply ");
							node.sendMsg(newMsg);
						
						}
						
					}
					else if(msg.getMessageType().equalsIgnoreCase("ACK") ){
						
							node.setD(node.getD()-1);
							node.channel[channel.getConId()].ackCounter++;
							node.log("C = "+node.getC()+" : D ="+node.getD());
							
							if(node.isInitNode() && node.isIdle() && node.getC() == 0 && node.getD() == 0 ) // Init Node and last pending ack received then flag Termination
								node.terminate();
							else if(!node.isInitNode() && node.isIdle() && node.getC() == 1 && node.getD() == 0 )// Not Init Node and last pending ack received, 
							{																					 // then send ack to parent, and Detach from tree
								Message msg1 = new Message(node.getNodeId(),node.getParentId(),node.clock.Time,"ACK");
								node.log("Last Pending ACK For This Process Has Been Received While Idle, Sending Final ACK To Parent Node: " + msg1.destId);
								node.sendMsg(msg1);
								node.setParentId(node.getNodeId());
							}	
							
					}
					
				}
				node.clock.myNotify(); // notify other waiting thread as message processing has been completed
			}
	}
}
