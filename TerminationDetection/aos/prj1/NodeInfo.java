package aos.prj1;

public class NodeInfo {

	private int NodeId;
	int C; //Sum of Deficits of Incoming edge;
	int D; //Sum of Deficits of outgoing edge;
	int status; // 0=Idle, 1=Active
	int ParentId; // First msg sender If any
	boolean initFlag; //Termination Detection Initiator flag 
	int totalNoOfProcess;
	
	NodeInfo(int NodeId){
		this.NodeId = NodeId;
		C = 0;
		D = 0;
		status = 0;
		ParentId = -1;
		initFlag = false;
	}

	/**
	 * Getters And Setters Section
	 */

	public synchronized int getC() {
		return C;
	}

	public synchronized void setC(int c) {
		C = c;
	}

	public synchronized int getD() {
		return D;
	}

	public synchronized void setD(int d) {
		D = d;
	}

	public synchronized int getStatus() {
		return status;
	}

	public synchronized void setStatusToIdle() {
		this.status = 0;
	}
	
	public synchronized boolean isIdle() {
		return status == 0;
	}
	
	public synchronized void setStatusToActive() {
		this.status = 1;
	}
	public synchronized int getParentId() {
		return ParentId;
	}

	public synchronized void setParentId(int parent) {
		ParentId = parent;
	}

	public synchronized boolean isInitNode() {
		return initFlag == true;
	}

	public  int getNodeId() {
		return NodeId;
	}

	public synchronized void setInitFlag(boolean initFlag) {
		this.initFlag = initFlag;
	}



}
