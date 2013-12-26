    1.Copy source package and compile java files
	  javac aos/prj1/*.java
	2.Create new File : Manifest.txt And add following line(Note: Line must end with new line character)
	  Main-Class: aos/prj1/NodeStarter
	3.Create Jar file, with Manifest.txt
	  jar cfm  TermDetAlgo.jar Manifest.txt aos/prj1/*.class
	4.Create file "input.txt" for series of events to execute
	5.Create File "HostConfig.txt", each line of the file represents configuration details for each node
	  denoted by a tuple <NodeId, Machine Name, Port Number>; where NodeId is id of node, 
	  Machine Name is name of the server, Port Number is port number through which this pocess communicate
	  e.g
		0 net01.utdallas.edu 1300
		1 net02.utdallas.edu 1301
		2 net03.utdallas.edu 1302
		3 net04.utdallas.edu 1303
		4 net05.utdallas.edu 1304
	6. Run jar file with following Command
	   java -jar TermDetAlgo.jar <NodeId> <input.txt> <HostConfig.txt>
	   e.g 	java -jar TermDetAlgo.jar 0 input.txt HostConfig.txt
	  (Note: all parametes <NodeId> <input.txt> <HostConfig.txt> are optional and default values will be
	   1. <NodeId> = NodeId of localhost from HostConfig file
	   2. <input.txt> = "input.txt"
	   3. <HostConfig.txt> = "HostConfig.txt"


Sample run for given sample "input.txt" and 
		0 net01.utdallas.edu 1300
		1 net02.utdallas.edu 1301
		2 net03.utdallas.edu 1302
		3 net04.utdallas.edu 1303
		4 net05.utdallas.edu 1304
		4 net05.utdallas.edu 1304
		
************ Process No 0 ******************************
{net01:~/AOS} java -jar TermDetAlgo.jar 0
0 : 0 : Listner Started at port No:1300
0 : 0 : Next Command Tag To Process is  INIT
0 : 1 : INIT Happened
0 : 1 : Next Command Tag To Process is  SEND
0 : 1 : Connection created with Node 1:net02.utdallas.edu@1301
0 : 2 : Started Listner for Channel 1
0 : 2 : C = 0 : D =1
0 : 2 : Message Sent 0 1 2 SEND
0 : 2 : Next Command Tag To Process is  TICK
0 : 3 : Clock Ticked : Delay Time :10
0 : 3 : Next Command Tag To Process is  IDLE
0 : 4 : Status Changed To Idle
0 : 4 : C = 0 : D = 1
0 : 24 : Message Received :1 0 23 ACK
0 : 24 : C = 0 : D =0
0 : 24 : Termination Detected !!

************ Process No 1 ******************************
{net02:~/AOS} java -jar TermDetAlgo.jar 1
1 : 0 : Listner Started at port No:1301
1 : 0 : Waiting For Clock Time 3
1 : 0 : New Connection Accepted From :0
1 : 0 : Started Listner for Channel 0
1 : 0 : Waiting For Clock Time 3
1 : 3 : Message Received :0 1 2 SEND
1 : 3 : C = 1 : D =0
1 : 3 : Joined Tree With Parent0
1 : 3 : Next Command Tag To Process is  SEND
1 : 3 : Connection created with Node 2:net03.utdallas.edu@1302
1 : 4 : C = 1 : D =1
1 : 4 : Message Sent 1 2 4 SEND
1 : 4 : Next Command Tag To Process is  TICK
1 : 4 : Started Listner for Channel 2
1 : 5 : Clock Ticked : Delay Time :10
1 : 5 : Next Command Tag To Process is  SEND
1 : 5 : Connection created with Node 3:net04.utdallas.edu@1303
1 : 6 : C = 1 : D =2
1 : 6 : Message Sent 1 3 6 SEND
1 : 6 : Started Listner for Channel 3
1 : 6 : New Connection Accepted From :4
1 : 6 : Started Listner for Channel 4
1 : 12 : Message Received :4 1 11 SEND
1 : 12 : C = 2 : D =2
1 : 12 : Sending Instant Reply
1 : 13 : C = 1 : D =2
1 : 13 : Message Sent 1 4 13 ACK
1 : 20 : Message Received :3 1 19 ACK
1 : 20 : C = 1 : D =1
1 : 20 : Next Command Tag To Process is  IDLE
1 : 21 : Status Changed To Idle
1 : 21 : C = 1 : D = 1
1 : 22 : Message Received :2 1 21 ACK
1 : 22 : C = 1 : D =0
1 : 22 : Last Pending ACK For This Process Has Been Received While Idle, Sending Final ACK To Parent Node: 0
1 : 23 : C = 0 : D =0
1 : 23 : Message Sent 1 0 23 ACK
1 : 23 : Detached from tree !!

************ Process No 2 ******************************
{net03:~/AOS} java -jar TermDetAlgo.jar 2
2 : 0 : Listner Started at port No:1302
2 : 0 : Waiting For Clock Time 5
2 : 0 : New Connection Accepted From :1
2 : 0 : Started Listner for Channel 1
2 : 0 : Waiting For Clock Time 5
2 : 5 : Message Received :1 2 4 SEND
2 : 5 : C = 1 : D =0
2 : 5 : Joined Tree With Parent1
2 : 5 : Next Command Tag To Process is  TICK
2 : 6 : Clock Ticked : Delay Time :100
2 : 6 : Next Command Tag To Process is  SEND
2 : 6 : Connection created with Node 4:net05.utdallas.edu@1304
2 : 7 : C = 1 : D =1
2 : 7 : Message Sent 2 4 7 SEND
2 : 7 : Started Listner for Channel 4
2 : 7 : Next Command Tag To Process is  IDLE
2 : 8 : Status Changed To Idle
2 : 8 : C = 1 : D = 1
2 : 20 : Message Received :4 2 19 ACK
2 : 20 : C = 1 : D =0
2 : 20 : Last Pending ACK For This Process Has Been Received While Idle, Sending Final ACK To Parent Node: 1
2 : 21 : C = 0 : D =0
2 : 21 : Message Sent 2 1 21 ACK
2 : 21 : Detached from tree !!

************ Process No 3 ******************************
{net04:~/AOS} java -jar TermDetAlgo.jar 3
3 : 0 : Listner Started at port No:1303
3 : 0 : Waiting For Clock Time 7
3 : 0 : New Connection Accepted From :1
3 : 0 : Started Listner for Channel 1
3 : 7 : Message Received :1 3 6 SEND
3 : 7 : C = 1 : D =0
3 : 7 : Joined Tree With Parent1
3 : 7 : Next Command Tag To Process is  TICK
3 : 8 : Clock Ticked : Delay Time :10
3 : 8 : Next Command Tag To Process is  SEND
3 : 8 : Connection created with Node 4:net05.utdallas.edu@1304
3 : 9 : C = 1 : D =1
3 : 9 : Message Sent 3 4 9 SEND
3 : 9 : Waiting For Clock Time 17
3 : 9 : Started Listner for Channel 4
3 : 17 : Message Received :4 3 16 ACK
3 : 17 : C = 1 : D =0
3 : 17 : Next Command Tag To Process is  IDLE
3 : 18 : Status Changed To Idle
3 : 18 : C = 1 : D = 0
3 : 18 : Process Becames Idle And No Pending ACK For This, Hence sending Final ACK Message To Parent :3 1 18 ACK
3 : 19 : C = 0 : D =0
3 : 19 : Message Sent 3 1 19 ACK
3 : 19 : Detached from tree !!

************ Process No 4 ******************************
{net05:~/AOS} java -jar TermDetAlgo.jar 4
4 : 0 : Listner Started at port No:1304
4 : 0 : Waiting For Clock Time 10
4 : 0 : New Connection Accepted From :3
4 : 0 : Started Listner for Channel 3
4 : 0 : Waiting For Clock Time 10
4 : 10 : Message Received :3 4 9 SEND
4 : 10 : C = 1 : D =0
4 : 10 : Joined Tree With Parent3
4 : 10 : Next Command Tag To Process is  SEND
4 : 10 : Connection created with Node 1:net02.utdallas.edu@1301
4 : 10 : Started Listner for Channel 1
4 : 11 : C = 1 : D =1
4 : 11 : Message Sent 4 1 11 SEND
4 : 11 : Waiting For Clock Time 14
4 : 14 : Message Received :1 4 13 ACK
4 : 14 : C = 1 : D =0
4 : 14 : Next Command Tag To Process is  IDLE
4 : 15 : Status Changed To Idle
4 : 15 : C = 1 : D = 0
4 : 15 : Process Becames Idle And No Pending ACK For This, Hence sending Final ACK Message To Parent :4 3 15 ACK
4 : 16 : C = 0 : D =0
4 : 16 : Message Sent 4 3 16 ACK
4 : 16 : Detached from tree !!
4 : 16 : Waiting For Clock Time 17
4 : 16 : New Connection Accepted From :2
4 : 16 : Started Listner for Channel 2
4 : 16 : Waiting For Clock Time 17
4 : 17 : Message Received :2 4 7 SEND
4 : 17 : C = 1 : D =0
4 : 17 : Joined Tree With Parent2
4 : 17 : Next Command Tag To Process is  IDLE
4 : 18 : Status Changed To Idle
4 : 18 : C = 1 : D = 0
4 : 18 : Process Becames Idle And No Pending ACK For This, Hence sending Final ACK Message To Parent :4 2 18 ACK
4 : 19 : C = 0 : D =0
4 : 19 : Message Sent 4 2 19 ACK
4 : 19 : Detached from tree !!