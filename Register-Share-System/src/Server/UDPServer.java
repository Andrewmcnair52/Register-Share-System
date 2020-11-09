package Server;

import java.io.DataInputStream;
import java.util.Random;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;



public class UDPServer extends Thread {											//internal server class
	   
	private int BUFF_SIZE = 1066;	//input buffer size
	
	public boolean isServing = false;		//boolean to toggle whether server is serving, leave false until sync'd
	public boolean dualServerSync = false;	//boolean to indicate whether server has been sync'd with other server
	
	public InetAddress otherServerIP;
	public int otherServerPort;
	
	DatagramSocket serverSocket;				//server listens on this socket
	byte[] inputBuffer, outputBuffer;			//network io buffers
	DatagramPacket dpReceive, dpSend;			//datagram packets
	
	Timer serverSwitchTimer = new Timer();
	TimerExec serverSwitchExec = new TimerExec();
	Random rand = new Random();
	
	ArrayList<String> registeredUsers;
	
	
    public UDPServer(int localPort) {
    	inputBuffer = new byte[BUFF_SIZE];
    	registeredUsers = new ArrayList<String>();
    	try { serverSocket = new DatagramSocket(localPort); }	//create datagram socket and bind to port
		catch (SocketException e) { 
			e.printStackTrace(); 
			System.out.println("socketException while creating DatagramSocket");
		}
    	
    }
    
    public void run() {
    	
    	System.out.println("starting UDP server");

		server_User user1 = new server_User();
		//Vector<server_User> listOfUsers = new Vector<server_User>();
  
    	
    	
		
    	while(true) {	//loop for receiving/parsing/handling incoming data
	     	
    		dpReceive = new DatagramPacket(inputBuffer,inputBuffer.length);
    		
    	    try { serverSocket.receive(dpReceive); }			//wait till data is received
    	    catch (IOException e) { e.printStackTrace(); System.out.println("socketException while recieving data");}
    	    
    	    
    	    
    	    //----------------------------
    	    // server input parser/handler
    	    //----------------------------
    	    
    	    /*	0: test case
    	     * 	1: registration request
    	     * 	100: server sync
    	     * 	101: server sync confirmation
    	     * 	101: server switch
    	     * 	102: server switch
    	     */
    	    
    	    isServing=true;
    	    if(isServing)				//socket input handlers which run while serving
    	    switch(inputBuffer[0]) {
    	    	
    	    case 0:	// a test case, print message to console, and respond with 'message received'
    	    	server_app.display("data recieved from client: "+parseString(inputBuffer,1));	//convert data to string, then send to main for displaying
    	    	sendString("message recieved", 0, dpReceive.getAddress(), dpReceive.getPort());	//send response
    	    	break;
    	    	
    	    case 1: //registration request
    	    	String registration = parseString(inputBuffer,1);
    	    	String[] registrationArray = registration.split(", ", 5);
    	    	user1.setName(registrationArray[1]);
    	    	user1.setIpAddress(registrationArray[2]);
    	    	user1.setSocketNumber(Integer.parseInt(registrationArray[3]));
    	    	//listOfUsers.add(user1);
    	    	System.out.println("Successful Registration: [Request 1],["+user1.getName()+"],["+user1.getIpAddress()+"],["+user1.getSocketNumber()+"]");
    	    	break;
    	    case 3: //Update request
    	    	String update = parseString(inputBuffer,1);
    	    	//System.out.println(update);
    	    	String[] updateArray = update.split(", ", 5);
    	    	if(updateArray[1].equals(user1.getName())) {
    	    	user1.setName(updateArray[2]);
    	    	user1.setIpAddress(updateArray[3]);
    	    	user1.setSocketNumber(Integer.parseInt(updateArray[4]));
    	    	System.out.println("Successful Update: [Request 3],["+user1.getName()+"],["+user1.getIpAddress()+"],["+user1.getSocketNumber()+"]");
    	    	}
    	    	else System.out.println("Update failed: The user's name " + updateArray[1]+" does not exist");
    	    	break;
    	    case 4: //Update List of Subject request
    	    	String subject = parseString(inputBuffer,1);
    	    	System.out.println("Successful List of Subject Update : ["+subject+"]");
    	    	break;
    	    }
    	    
    	    switch(inputBuffer[0]) {	//socket input handler that runs even when not serving
    	    
    	    case 100:	//server sync: 
    	    	
    	    	//save info for other server
    	    	otherServerIP = dpReceive.getAddress();
    	    	otherServerPort = dpReceive.getPort();
    	    	
    	    	if(Objects.equals(parseString(inputBuffer,1),"s1")) {	//we are server 1
        	    	dualServerSync = true;	//set server as sync'd
        	    	isServing = true;		//server 1 serves first
        	    	System.out.println("this server is serving");
        	    	sendServer("s2",100);	//respond to sync other server
    	    	} else {	//we are server 2
    	    		dualServerSync = true;	//set server as sync'd but do not set isServing
    	    		sendServer("s",101);	//respond with sync confirmation to start server1's timer
    	    		System.out.println("this server is not serving");
    	    		serverSwitchTimer = new Timer();		//initialize server 2's timer
        	    	serverSwitchExec = new TimerExec();		//initialize server2's timerTask
    	    	}
    	    	break;
    	    	
    	    case 101: //server sync confirmation
    	    	
    	    	//if we're here, then both servers are now sync'd and this is server 1
    	    	//start serverSwitchTimer
    	    	startTimer();
    	    	break;
    	    
    	    case 102: //server switch
    	    	//other server has notified us that of a server switch
    	    	isServing = true;	//start serving
    	    	startTimer();		//start timer for next server switch
    	    	break;
    	    }
    	    
    	    
    	    
    	    inputBuffer = new byte[BUFF_SIZE]; 	// Clear the buffer after every message. 

	    }
    	
	}
    
    String parseString(byte[] data, int start) { 	//function to convert byte array to string
    	
        if (data == null) return null; 
        String out = new String(); 
        for(int i=start; data[i]!=0; i++)
        	out += ((char) data[i]); 
        return out; 
    } 
    
//==================================================
// Data Send Functions  
//==================================================
    
    //sends a string with message code, to a given ip address and port
    public void sendString(String message, int op, InetAddress ip, int destPort) {
		
		//convert string to byte array
		byte[] tmpBuff = message.getBytes();		//get message as a byte array
		outputBuffer = new byte[tmpBuff.length+1];
		outputBuffer[0] = (byte) op;				//append a zero to beginning for server side command handler
		for(int i=0; i<tmpBuff.length; i++)			//copy message byte array to output buffer
		outputBuffer[i+1] = tmpBuff[i];
		
    	dpSend = new DatagramPacket(outputBuffer, outputBuffer.length, ip, destPort); 	//create datagram packet 

    	try { serverSocket.send(dpSend); }	//send data
    	catch(IOException e) { e.printStackTrace(); server_app.display("message could not be sent"); }
    	
	}
    
    //sends a string with message code, to the other server
    public void sendServer(String message, int op) {
		
		//convert string to byte array
		byte[] tmpBuff = message.getBytes();		//get message as a byte array
		outputBuffer = new byte[tmpBuff.length+1];
		outputBuffer[0] = (byte) op;				//append a zero to beginning for server side command handler
		for(int i=0; i<tmpBuff.length; i++)			//copy message byte array to output buffer
		outputBuffer[i+1] = tmpBuff[i];
		
    	dpSend = new DatagramPacket(outputBuffer, outputBuffer.length, otherServerIP, otherServerPort); 	//create datagram packet 

    	try { serverSocket.send(dpSend); }	//send data
    	catch(IOException e) { e.printStackTrace(); server_app.display("message could not be sent"); }
    	
	}
    
 //==================================================
 // Timer Stuff
 //==================================================
    
   class TimerExec extends TimerTask {
	   public void run() { 
		   System.out.println("timer triggered, switching servers");
		   
		   //notify registered clients, this can only be done after there are registered clients(FRANK)
		   
		   sendServer("",102);	//notify other server first
		   isServing = false;	//stop serving
		   
	   }
   }
   
   public void startTimer() {
	   
	   //reset timer
	   serverSwitchTimer.cancel();
	   serverSwitchTimer.purge();
	   serverSwitchTimer = new Timer();
	   serverSwitchExec = new TimerExec();

	   //project description says to "pick a random value say 5m"
	   int val = 240000 + rand.nextInt(360000); //pick a value between 240,000 and 360,000 (4m-6m)
	   //int val = 10000 + rand.nextInt(2000);	//10-12 seconds, left here for debuggin purposes
	   System.out.println("server switch in: " + val);
   	
	   //schedule(TimerTask task, Date time)
	   // task: task to run
	   // time: time to wait before running task in milliseconds
	   serverSwitchTimer.schedule(serverSwitchExec, val);
   	
   }
    
 //==================================================
    
    private int registerUser() {
    	
    	//if i am not the active server, do nothing 
    	//check if user is already registerd
    	//check if user name is available
    	// if its all good, register them
    	// then send the other server a message about what happened
    	// then send the user back a message about what happened
    	
    	return 0;
    }
	   
	      
}