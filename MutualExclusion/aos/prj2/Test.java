package aos.prj2;

import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;

public class Test {

	public static void main(String[] args)  {
		
		PriorityBlockingQueue<QueEntry>  waitingQueue = new PriorityBlockingQueue<QueEntry>();
		waitingQueue.put(new QueEntry(1,0));
		waitingQueue.put(new QueEntry(1,1));
		waitingQueue.put(new QueEntry(3,7));
		waitingQueue.put(new QueEntry(1,10));
		waitingQueue.put(new QueEntry(6,4));
		
		
		QueEntry p = new QueEntry(1,5);
		
    	Iterator<QueEntry>iterator = waitingQueue.iterator();
    	while(iterator.hasNext()){
    		QueEntry e =iterator.next();
    		System.out.println(" Comarision of  " + e +" and " + p +" = " + e.compareTo(p));
 
    	}

	}
}
