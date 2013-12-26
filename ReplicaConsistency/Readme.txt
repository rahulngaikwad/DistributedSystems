 
    1.Copy source package aos and compile java files
	  javac aos/prj3/*.java
	2.Create new File : Manifest.txt And add following line(Note: Line must end with new line character)
	  Main-Class: aos/prj3/NodeStarter
	3.Create Jar file, with Manifest.txt
	  jar cfm  PRJ3.jar Manifest.txt aos/prj3/*.class
	4.Create file "input.txt" to execute
	5. Start all nodes in increasing order. 
	   Run jar file with following Command
	   java -jar PRJ3.jar <NodeId> <input.txt>
	   e.g 	java -jar PRJ3.jar 0 input.txt
	   	    OR 
			java -jar PRJ3.jar
	  (Note: all parametes <NodeId> <input.txt> are optional and default values will be
	   1. <NodeId> = NodeId of localhost
	   2. <input.txt> = "input.txt"   
	6. For each nodes check output file  log<NodeId>.txt

Some usefull commands:
 rm -rf log* aos PRJ3.jar
 ps | grep java | awk '{ print $1 }' | xargs kill -9
 java -jar PRJ3.jar	

 tail -f log0.txt log1.txt log2.txt log3.txt log4.txt log5.txt log6.txt
 tail -n 15 -f log7.txt log8.txt log9.txt log10.txt log11.txt
 
 dos2unix -o log*
 scp log* rxg122630@neto1.utdallas.edu:/home/004/r/rx/rxg122630/AOS/PRJ3/Output/RE/5.0
 
  scp log* rxg122630@neto1.utdallas.edu:"/people/cs/h/hxv071000/student/project3/RahulGaikwad(rxg122630)"