package aos.prj3;

import java.util.LinkedList;


public class Data {

	int versionNumber;
	int data;
	boolean locked = false;
	int lockedBy;
	Message lastUpdatedByMsg;
	

	Node node;
	LinkedList<Message>  queue;

	public Data(Node node) {
		super();
		this.node = node;
		queue = new LinkedList<Message>();
	}
	
	public synchronized boolean isLocked() {
		return locked;
	}
	
	public synchronized void setLocked(boolean locked) {
		this.locked = locked;
	}
		
	public synchronized int getVersionNumber() {
		return versionNumber;
	}
	public synchronized void setVersionNumber(int versionNumber) {
		this.versionNumber = versionNumber;
	}
	public synchronized void incrVersionNumber() {
		this.versionNumber++;
	}
	
	public synchronized int getData() {
		return data;
	}
	public synchronized void setData(int data) {
		this.data = data;
	}
	public synchronized void incrDataBy(int incrValue) {
		this.data += incrValue;
	}
	
	public synchronized LinkedList<Message> getQueue() {
		return queue;
	}
	
	public synchronized Message getLastUpdatedByMsg() {
		return lastUpdatedByMsg;
	}

	public synchronized void setLastUpdatedByMsg(Message lastUpdatedByMsg) {
		this.lastUpdatedByMsg = lastUpdatedByMsg;
	}


	@Override
	public String toString() {
		return " [versionNumber=" + versionNumber + ", data=" + data + "]";
	}

}
