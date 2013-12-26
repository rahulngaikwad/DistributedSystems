package aos.prj1;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Scanner;

/**
 * NodeStarter class Read Commands from Input file and 
 * sends to Node, which executes these commands 
 * @author Rahul Gaikwad
 *
 */
public class NodeStarter {

	static HashMap<Integer, String> configuration;
	static int totalProcess = 0, nodeId;

	/**
	 * Expects following three arguments in args array, 
	 * all arguments are optional.
	 *  1. Node ID by default finds in Host Configuration
	 *  2. Input command File default is input.txt
	 *  3. Host Configuration File default is HostConfig.txt
	 * 
	 * @param args
	 */
	public static void main(String[] args)  {
		
		BufferedReader br = null;

		try {
				if (args.length >= 2)
					br = new BufferedReader(new InputStreamReader( new FileInputStream(args[1])));
				else
					br = new BufferedReader(new InputStreamReader( new FileInputStream("input.txt"))); //default command file name 
	
				totalProcess = Integer.parseInt(br.readLine().trim());
	
				if (args.length >= 3)
					Initialize(args[2]);
				else
					Initialize("HostConfig.txt"); //default Host Configuration name 

				if(args.length >= 1)
					nodeId = Integer.parseInt(args[0]); //given node id
				
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		Node node = new Node(nodeId, totalProcess, configuration);
		
		String command;
		
		try {		
			while ((command = br.readLine()) != null) 
			{
				
				Scanner scn = new Scanner(command);
				if (scn.nextInt() != nodeId)   // command for other node
					continue;

				int time = scn.nextInt();	// read time of command

				while (true) 
				{
					if (time > node.clock.Time) {	 //wait if commands time is less than nodes clock time
						node.log("Waiting For Clock Time " + time);					
						synchronized (node.clock){
							try {node.clock.wait();	} catch (InterruptedException e) {e.printStackTrace();}
						}
					}
					if ( time <= node.clock.Time){  //if clock has passed command time break wait loop
						synchronized (node) 
						{
							String msg = scn.next();	//read tag
							node.log("Next Command Tag To Process is  " + msg);
							
							// sends command to Node to execute
							if (msg.equals("INIT"))
								node.initAction();
							else if (msg.equals("SEND"))
								node.sendMsg(new Message(node.getNodeId(), scn.nextInt(), node.clock.Time, "SEND"));
							else if (msg.equals("IDLE"))
								node.idleAction();
							else if (msg.equals("TICK")) {
								if (scn.hasNextInt())
									node.TickAction(scn.nextInt());
								else
									node.TickAction(0);
							}
						}// synchronized ends
						break;
					}//if ends
				}// while ends
			}//while ends
		}catch (IOException e) {
			e.printStackTrace();
		}

		 //Waiting For Listener Thread To Die
	try { node.listnerThread.join();} catch (InterruptedException e) {e.printStackTrace();}
		
}
	
	/**
	 * Read Host Configuration File and creates configuration for node.
	 * @param ConfigFile : name of config file
	 * @throws IOException
	 */
	public static void Initialize(String ConfigFile) throws IOException {
		BufferedReader br = null;
		configuration = new HashMap<Integer,String>();
		
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream( ConfigFile)));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		String localhostname = java.net.InetAddress.getLocalHost().getHostName();
		String line = null;
		while ((line = br.readLine()) != null) {
			if(line.length() > 0 ){
				Scanner scn = new Scanner(line);
				int processId = scn.nextInt();
				String hostName = scn.next();
				int portNo = scn.nextInt();
				configuration.put(processId, hostName + "@" + portNo);
				if(localhostname.equalsIgnoreCase(hostName)) // id for current node.
					nodeId = processId;
					
			}
		}

	}
}
