package aos.prj2;

/**
 * Request queue stores QueEntry 
 * @author Rahul Gaikwad
 */
public class QueEntry implements Comparable<QueEntry> {
	int time;
    int nodeId;
	
	public QueEntry(int time,int nodeid) {
		this.time = time;
		this.nodeId = nodeid;
		
	}

	public synchronized Integer getTime() {
		return time;
	}

	public synchronized Integer getNodeId() {
		return nodeId;
	}
	
	public synchronized void setTime(int time) {
		 this.time = time;
	}

	public synchronized void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}
	
	/**
	 * compare current QueEntry object with other QueEntry object using 
	 * time < other.time OR time == other.time and nodeId <  other.nodeId
	 */
	public int compareTo(QueEntry other) {
		
		int res = 0;
		if(this.time > other.time)
			res = 1;
		else if(this.time < other.time)
			res = -1;
		else
			res = 0;

		if (res == 0 && nodeId != other.nodeId )
			res = (nodeId > other.nodeId ? 1 : -1);
		
		return res;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this.time == ((QueEntry)obj).time && this.nodeId == ((QueEntry)obj).nodeId)
			return true;
		return false;
	}

	@Override
	public String toString() {
		return time+","+nodeId;
	}
}
