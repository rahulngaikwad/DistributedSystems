package aos.prj2;

public class LamportClock {
	volatile int Time;
	int delta; // Incremental value 

	public LamportClock(){ 
		Time = 0;
		delta = 1;
	}
	
	public LamportClock(int delta){ 
		Time =0;
		this.delta = delta;
	}
	
	/**
	 * set clock to given time
	 * @param time
	 */
	public synchronized void setTime(int time){ 
		this.Time = time;
	}
	/**
	 * Compare Local Time With Remote Hosts/Senders Time and set clock to correct time
	 * @param remoteTime : Time of Remote/Sender Node
	 * @return
	 */
	public synchronized int compareAndSetTime(int remoteTime){ 
		this.Time = remoteTime > this.Time? remoteTime : this.Time;
		Time = Time +1;
		return Time;
	}
	
	/**
	 * Increments Time by delta and notify all waiting threads
	 * @return new Time
	 */
	public synchronized int Tick(){ 
		Time += delta;
		this.notifyAll();
		return Time;
	}
	
	/**
	 * Increments Time by delta after waitTime and notify all waiting threads
	 * @return new Time
	 */
	public synchronized int Tick(int waitTime){ 
		
		try {
			Thread.sleep(waitTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Time += delta;
		this.notifyAll();
		return Time;
	}

	/**
	 * Just Notify All Waiting Threads
	 */
	public synchronized void myNotify()
	{
		this.notifyAll();
	}
	
}
