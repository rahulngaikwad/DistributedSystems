package aos.prj2;

import aos.prj2.Message.MSGTYPE;

/**
 * BroadcastSignal put BROADCAST message in node's msgQueue after given time delay
 * @author Rahul Gaikwad
 */
public class  BroadcastSignal extends Thread{
	private ProcessNode node;
	private int delay;
	
	public BroadcastSignal(ProcessNode processNode, int delay) 
	{
		this.node = processNode;
		this.delay = delay;
	}
	
	@Override
	public void run() {
		if(delay > 0)
			try { Thread.sleep(delay);} catch (InterruptedException e) {e.printStackTrace();}
		
		synchronized (node.msgQueue) {
			 node.msgQueue.addFirst(new Message(node.getNodeId(),node.getNodeId(),0,MSGTYPE.BROADCAST));
			 node.msgQueue.notify();
		}
		return;
	}
}