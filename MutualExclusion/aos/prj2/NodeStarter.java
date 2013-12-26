package aos.prj2;

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
	static int  nodeId, totalNodes,totalRequest,interRequestTime,critSectionTime;
	
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
	
				if (args.length >= 3)
					Initialize(args[2]);
				else
					Initialize("HostConfig.txt"); //default Host Configuration name 

				if(args.length >= 1)
					nodeId = Integer.parseInt(args[0]); //given node id
				
		} catch (Exception e1) {
			e1.printStackTrace();
		}


		quorumSet = new HashSet<Integer>();
		
		String line;
		
		try {
			while ((line = br.readLine()) != null) 
			{			
				if(line.contains("N="))
					totalNodes = Integer.parseInt(line.replace("N=", ""));
				else if(line.contains("M="))
					totalRequest = Integer.parseInt(line.replace("M=", ""));
				else if(line.contains("IA="))
					interRequestTime = Integer.parseInt(line.replace("IA=", ""));
				else if(line.contains("IR="))
					interRequestTime = Integer.parseInt(line.replace("IR=", ""));
				else if(line.contains("CST="))
					critSectionTime = Integer.parseInt(line.replace("CST=", ""));
				else if(line.startsWith("R"+nodeId+"=") && nodeId != -1)
				{
					String corumList =  line.substring(line.indexOf('=')+1);
					String[]  corumMember =  corumList.split(",");
					
					for(int i=0; i< corumMember.length; i++)
						quorumSet.add(Integer.parseInt(corumMember[i]));
				}
			}//while ends

		}catch (IOException e) {
			e.printStackTrace();
		}
		

		Node node;
		
		if(nodeId == -1)
			 node = new CSNode(configuration);
		else
			 node = new ProcessNode(nodeId,totalNodes,totalRequest,interRequestTime,critSectionTime,quorumSet,configuration);
		
		node.log.print("Quorum Set ");
		
		for(int elem : quorumSet)
			node.log.print(elem +",");
		node.log.println();
		
		System.out.println("Node "+ nodeId +" Started..........");
		new Thread(node).start();
		
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
				scn.close();
			}
		}

	}
}
