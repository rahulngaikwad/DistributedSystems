    1.Copy source package aos and compile java files
	  javac aos/prj2/*.java
	2.Create new File : Manifest.txt And add following line(Note: Line must end with new line character)
	  Main-Class: aos/prj2/NodeStarter
	3.Create Jar file, with Manifest.txt
	  jar cfm  PRJ2.jar Manifest.txt aos/prj2/*.class
	4.Create file "input.txt" for series of events to execute
	5.Create File "HostConfig.txt", each line of the file represents configuration details for each node
	  denoted by a tuple <NodeId, Machine Name, Port Number>; where NodeId is id of node, 
	  Machine Name is name of the server, Port Number is port number through which this pocess communicate
	  e.g
	   -1 net40.utdallas.edu 1400
		0 net01.utdallas.edu 1300
		1 net02.utdallas.edu 1301
		2 net03.utdallas.edu 1302
		3 net04.utdallas.edu 1303
		4 net05.utdallas.edu 1304
	   (Note: NodeId -1 is for CSNode) 
	   
	6. Start CSnode first and then all other nodes in decreasing order. 
	   Run jar file with following Command
	   java -jar PRJ2.jar <NodeId> <input.txt> <HostConfig.txt>
	   e.g 	java -jar PRJ2.jar 0 input.txt HostConfig.txt
	   	    OR 
			java -jar PRJ2.jar
	  (Note: all parametes <NodeId> <input.txt> <HostConfig.txt> are optional and default values will be
	   1. <NodeId> = NodeId of localhost from HostConfig file
	   2. <input.txt> = "input.txt"
	   3. <HostConfig.txt> = "HostConfig.txt")
	   
	7. For each nodes check output file  log<NodeId>.txt
	   
ps | grep java | awk '{ print $1 }' | xargs kill -9