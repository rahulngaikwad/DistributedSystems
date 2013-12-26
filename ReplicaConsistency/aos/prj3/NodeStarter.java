package aos.prj3;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

/**
 * NodeStarter class Reads information from Input file and 
 * create Node, and start execution 
 * @author Rahul Gaikwad
 *
 */
public class NodeStarter {

	static HashMap<Integer, String> configuration;
	static HashSet<Integer> quorumSet;
	static int  nodeId, noOFServers,noOFClients,noOfRequest,timeUnit;
	static int[] failingNodes; 
	
	/**
	 * Expects following three arguments in args array, 
	 * all arguments are optional.
	 *  1. Node ID, by default finds in Host Configuration
	 *  2. Input information File default is input.txt
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

				if(args.length >= 1)
					nodeId = Integer.parseInt(args[0]); //given node id
				
		} catch (Exception e1) {
			e1.printStackTrace();
		}


		
		String line;
		
		try {
			while ((line = br.readLine()) != null) 
			{			
				if(line.startsWith("#"))
					continue;
					
				if(line.contains("NS="))
					noOFServers = Integer.parseInt(line.replace("NS=", ""));
				else if(line.contains("NC="))
					noOFClients = Integer.parseInt(line.replace("NC=", ""));
				else if(line.contains("M="))
					noOfRequest = Integer.parseInt(line.replace("M=", ""));
				else if(line.contains("TIME_UNIT="))
					timeUnit = Integer.parseInt(line.replace("TIME_UNIT=", ""));
				else if(line.contains("SOCKINFO"))
					Initialize(br,noOFServers+noOFClients);
				else if(line.contains("FAILINGNODES"))
					failingNodes = getFailingNode(br);
					
			}//while ends

		}catch (IOException e) {
			e.printStackTrace();
		}
		

		Node node;
		
		if(nodeId < noOFServers)
			 node = new ServerNode(nodeId,noOFServers,noOFClients,noOfRequest,timeUnit,failingNodes,configuration);
		else
			 node = new ClientNode(nodeId,noOFServers,noOFClients,noOfRequest,timeUnit,failingNodes,configuration);
		
		
		System.out.println("Node "+ nodeId +" Started..........");
		new Thread(node).start();
		
}
	
	private static int[] getFailingNode(BufferedReader br) throws IOException {
		
		String line = br.readLine();
    	String[] lineParts = line.split(" ");
    	int[] failingNodes = new int[lineParts.length];
    	
    	for(int i = 0; i < lineParts.length; i++)
    		failingNodes[i] = Integer.parseInt(lineParts[i]);
    	
		return failingNodes;
	}

	/**
	 * Read Host Configuration File and creates configuration for node.
	 * @param ConfigFile : name of config file
	 * @throws IOException
	 */
	public static void Initialize(BufferedReader br,int noOfLines) throws IOException {
		
		configuration = new HashMap<Integer,String>();
	
		String localhostname = java.net.InetAddress.getLocalHost().getHostName();
		String line = null;
		
		for(int i = 0; i < noOfLines; i++){
			
			line = br.readLine();
			if(line.length() > 0 ){
				Scanner scn = new Scanner(line);
				int processId = scn.nextInt();
				String hostName = scn.next();
				int portNo = scn.nextInt();
				configuration.put(processId, hostName + "@" + portNo);
				if(localhostname.equalsIgnoreCase(hostName)) // id for current node.
					nodeId = processId;	
				scn.close();
			}
		}

	}
}
